package com.example.Api_Server.service;

import CLI.DataGenerator;
import com.example.Api_Server.DTO.UserRegisterDTO;
import com.example.Api_Server.entity.Customer;
import com.example.Api_Server.entity.Event;
import com.example.Api_Server.entity.Ticket;
import com.example.Api_Server.entity.TicketStatus;
import com.example.Api_Server.repository.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

@Service
public class CustomerService {
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private EventService eventService;
    @Autowired
    private TicketService ticketService;
    @Autowired
    private BuyTicketService buyTicketService;

    private static final Logger logger = Logger.getLogger(CustomerService.class.getName());

    private DataGenerator dataGenerator;

    private final Lock lock = new ReentrantLock();

    @Autowired
    public void setDataGenerator(DataGenerator dataGenerator) {
        this.dataGenerator = dataGenerator;
    }

    public void addCustomer(Customer customer){
        customerRepository.save(customer);
    }

    @Transactional
    public List<Customer> getAll(){
        return customerRepository.findAll();
    }


    public void simulationTicketRetrieval(Customer customer, int totalTickets){
        try {
            for (int i = 0; i < customer.getRetrievalRate(); i++) {
                boolean flag = true;
                while (flag && totalTickets > 0) {
                    int eventId = dataGenerator.generateRandomInt(1, eventService.getCount() + 1);
                    Optional<Event> selectedEventOptional = eventService.getEventById(eventId);
                    if (selectedEventOptional.isPresent()) {
                        Event selectedEvent = selectedEventOptional.get();
                        if (!selectedEvent.getPoolTickets().isEmpty()) {
                            buyTicketService.giveTicket(customer, selectedEvent);
                            flag = false;
                            totalTickets--;
                        }
                    } else {
                        customer.logInfo("Event not found for ID: " + eventId);
                    }
                }
            }
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            customer.logWarning("Customer " + customer.getId() + " thread interrupted: " + e.getMessage());
        }
    }


    public void performTicketRetrieval(Customer customer) {
        int totalTickets = ticketService.getTotalTickets();
        while (totalTickets > 0) {
            try{
                simulationTicketRetrieval(customer, totalTickets);
                Thread.sleep(1000L * customer.getFrequency());
                if(totalTickets <= 0){
                    totalTickets = ticketService.getTotalTickets();
                }
            }catch(InterruptedException e){
                Thread.currentThread().interrupt();
                customer.logWarning("Customer " + customer.getId() + " thread interrupted: " + e.getMessage());
            }
        }
        customer.logInfo("Customer " + customer.getId() + " thread ended.");
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

    public boolean checkCustomer(String email, String password) {
        List<Customer> customers = customerRepository.findAll();
        for (Customer customer : customers) {
            if (customer.getEmail().equals(email) && customer.getPassword().equals(customer.hashPassword(password))) {
                return true;
            }
        }
        return false;
    }

    public void addCustomerFromUI(UserRegisterDTO userRegisterDTO) {
        Customer customer = new Customer(userRegisterDTO.getFirstName(), userRegisterDTO.getLastName(), userRegisterDTO.getUsername(), userRegisterDTO.getPassword(), userRegisterDTO.getEmail());
        addCustomer(customer);
    }

    public Customer getCustomerFromEmail(String email) {
        List<Customer> customers = customerRepository.findAll();
        for (Customer customer : customers) {
            if (customer.getEmail().equals(email)) {
                return customer;
            }
        }
        return null;
    }

    public boolean checkCustomerExists(String email) {
        List<Customer> customers = customerRepository.findAll();
        for (Customer customer : customers) {
            if (customer.getEmail().equals(email)) {
                return true;
            }
        }
        return false;
    }

    public int getCustomerCount() {
        return customerRepository.findAll().size();
    }
}