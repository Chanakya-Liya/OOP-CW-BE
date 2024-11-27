package com.example.Api_Server.service;

import CLI.DataGenerator;
import com.example.Api_Server.entity.Customer;
import com.example.Api_Server.entity.Event;
import com.example.Api_Server.entity.Ticket;
import com.example.Api_Server.entity.TicketStatus;
import com.example.Api_Server.repository.*;
import com.example.Api_Server.simulation.CustomerSimulation;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Autowired
    private CustomerSimulation customerSimulation;

    private static final Logger logger = Logger.getLogger(CustomerService.class.getName());

    @Autowired
    private DataGenerator dataGenerator;

    public void addCustomer(Customer customer){
        customerRepository.save(customer);
    }

    public List<Customer> getAll(){
        return customerRepository.findAll();
    }

    public void performTicketRetrieval(Customer customer) {
        customerSimulation.performTicketRetrieval(customer);
    }

    @Transactional
    public void saveAllCustomers(List<Customer> customers){
        customerRepository.saveAll(customers);
    }


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