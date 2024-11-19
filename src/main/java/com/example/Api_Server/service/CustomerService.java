package com.example.Api_Server.service;

import CLI.DataGenerator;
import com.example.Api_Server.entity.Customer;
import com.example.Api_Server.entity.Event;
import com.example.Api_Server.entity.Ticket;
import com.example.Api_Server.entity.TicketStatus;
import com.example.Api_Server.repository.*;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Transient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

@Service
public class CustomerService {
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private VendorEventAssociationRepository vendorEventAssociationRepository;
    @Autowired
    private VendorRepository vendorRepository;
    @Autowired
    private EventService eventService;
    @Autowired
    private TicketService ticketService;

    private static final Logger logger = Logger.getLogger(CustomerService.class.getName());

    private DataGenerator dataGenerator;

    @Autowired
    public void setDataGenerator(DataGenerator dataGenerator) {
        this.dataGenerator = dataGenerator;
    }

    public Customer addCustomer(Customer customer){
        return customerRepository.save(customer);
    }

    public List<Customer> getAll(){
        return customerRepository.findAll();
    }

    public void updateTicket(Ticket ticket){
        ticketRepository.save(ticket);
    }

    @Transactional
    public void performTicketRetrieval(Customer customer) {
        while (true) {
            try {
                for (int i = 0; i < customer.getRetrievalRate(); i++) {
                    boolean flag = true;
                    while (flag && ticketService.getTotalTickets() > 0) {
                        int eventId = dataGenerator.generateRandomInt(1, eventService.getCount() + 1);
                        synchronized (eventRepository){
                            Optional<Event> selectedEventOptional = eventRepository.findById((long) eventId);

                            if (selectedEventOptional.isPresent()) {
                                Event selectedEvent = selectedEventOptional.get();
                                synchronized (selectedEvent) {
                                    if (!selectedEvent.getPoolTickets().isEmpty()) {
                                        Ticket ticket;
                                        do{
                                            ticket = selectedEvent.getPoolTickets().removeFirst();
                                        }while(ticket.getStatus() == TicketStatus.SOLD);

                                        ticket.setStatus(TicketStatus.SOLD);
                                        ticket.setCustomer(customer);
                                        updateTicket(ticket);

                                        // Log and update state
//                                        String messageTemplate = "event id: %d ticket(id: %d) sold to customer with id: %d, available tickets: %d";
//                                        String message = String.format(messageTemplate, selectedEvent.getId(), ticket.getId(), customer.getId(), selectedEvent.getPoolTickets().size());
                                        String message = String.format("Customer %d purchased ticket %d from event %d", customer.getId(), ticket.getId(), selectedEvent.getId());
                                        customer.logInfo(message);

                                        flag = false;
                                    }
                                }
                            } else {
                                logger.info("Event not found for ID: " + eventId);
                            }
                        }
                    }
                }
                Thread.sleep(customer.getFrequency() * 1000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warning("Customer " + customer.getId() + " thread interrupted: " + e.getMessage());
                break;
            }
        }
        System.out.println("Customer " + customer.getId() + " thread ended.");
    }

    @PostConstruct
    public void init() {
        List<Customer> customers = customerRepository.findAll();
        for (Customer customer : customers) {
            if (customer.isSimulated()) {
                customer.setCustomerService(this);
                Thread customerThread = new Thread(customer);
                customerThread.start();
            }
        }
    }
}