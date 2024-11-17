package com.example.Api_Server.service;

import CLI.DataGenerator;
import CLI.Util;
import com.example.Api_Server.entity.Customer;
import com.example.Api_Server.entity.Event;
import com.example.Api_Server.repository.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    private DataGenerator dataGenerator;
    private static final Logger logger = Logger.getLogger(CustomerService.class.getName());
    @Autowired
    private EventService eventService;

    public Customer addCustomer(Customer customer){
        return customerRepository.save(customer);
    }

    public List<Customer> getAll(){
        return customerRepository.findAll();
    }

    public int getTotalTickets() {
        int totalTickets = 0;
        List<Event> events = eventRepository.findAll();
        for (Event event : events) {
            totalTickets += event.getAvailableTickets().size() + event.getPoolTickets().size(); //Sum the available and pool tickets
        }
        return totalTickets;
    }

    public void performTicketRetrieval(Customer customer) {
        int totalTickets = getTotalTickets();
        while (totalTickets > 0) {
            try {
                for (int i = 0; i < customer.getRetrievalRate(); i++) {
                    boolean flag = true;
                    while (flag && totalTickets > 0) {
                        int eventId = dataGenerator.generateRandomInt(0, eventService.getCount());
                        Optional<Event> selectedEventOptional = eventRepository.findById((long) eventId);

                        if (selectedEventOptional.isPresent()) {
                            Event selectedEvent = selectedEventOptional.get();
                            synchronized (selectedEvent) {
                                if (!selectedEvent.getPoolTickets().isEmpty()) {
                                    selectedEvent.removeTicketFromPool();

                                    String messageTemplate = "event id: %d ticket sold customer with id: %d, available tickets: %d";
                                    String message = String.format(messageTemplate, selectedEvent.getId(), customer.getId(), selectedEvent.getPoolTickets().size());
                                    logger.info(message);
                                    eventRepository.save(selectedEvent);

                                    flag = false;
                                }
                            }

                        } else {
                            logger.info("Event not found for ID: " + eventId);
                            //handle not found case
                        }
                    }
                    totalTickets = getTotalTickets(); // Efficiently update total tickets after they've been modified

                }
                totalTickets = getTotalTickets(); //Update ticket counts to allow thread to end.
                Thread.sleep(customer.getFrequency() * 1000L);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warning("Customer " + customer.getId() + " thread interrupted: " + e.getMessage());
                break; // Exit the loop if interrupted
            }
        }
        System.out.println("Customer " + customer.getId() + " thread ended.");
    }

    @PostConstruct
    public void init() {
        List<Customer> customers = customerRepository.findAll();
        for (Customer customer : customers) {
            if (customer.isSimulated()) {
                Thread customerThread = new Thread(() -> performTicketRetrieval(customer));
                customerThread.start();
            }
        }
    }
}
