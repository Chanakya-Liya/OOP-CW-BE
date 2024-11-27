package com.example.Api_Server.simulation;

import CLI.DataGenerator;
import com.example.Api_Server.entity.Customer;
import com.example.Api_Server.entity.Event;
import com.example.Api_Server.service.CustomerService;
import com.example.Api_Server.service.EventService;
import com.example.Api_Server.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

@Component
public class CustomerSimulation {

    @Autowired
    private DataGenerator dataGenerator;
    @Autowired
    private EventService eventService;
    @Autowired
    private TicketService ticketService;
    private final Lock lock = new ReentrantLock();
    private static final Logger logger = Logger.getLogger(CustomerSimulation.class.getName());


    public void simulationTicketRetrieval(Customer customer, int totalTickets){
        try {
            for (int i = 0; i < customer.getRetrievalRate(); i++) {
                boolean flag = true;
                while (flag && totalTickets > 0) {
                    int eventId = dataGenerator.generateRandomInt(1, eventService.getCount() + 1);
                    Optional<Event> selectedEventOptional = eventService.getEventById(eventId);
                    if (selectedEventOptional.isPresent()) {
                        synchronized (lock) {
                            Event selectedEvent = selectedEventOptional.get();
                            if (!selectedEvent.getPoolTickets().isEmpty()) {
                                eventService.giveTicket(customer, selectedEvent);
                                flag = false;
                            }
                        }
                    } else {
                        logger.info("Event not found for ID: " + eventId);
                    }
                }
            }
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            logger.warning("Customer " + customer.getId() + " thread interrupted: " + e.getMessage());
        }
    }


    public void performTicketRetrieval(Customer customer) {
        int totalTickets = ticketService.getTotalTickets();
        while (totalTickets > 0) {
            try{
                simulationTicketRetrieval(customer, totalTickets);
                Thread.sleep(1000L * customer.getFrequency());
                totalTickets--;
                if(totalTickets <= 0){
                    totalTickets = ticketService.getTotalTickets();
                }
            }catch(InterruptedException e){
                Thread.currentThread().interrupt();
                logger.warning("Customer " + customer.getId() + " thread interrupted: " + e.getMessage());
            }
        }
        System.out.println("Customer " + customer.getId() + " thread ended.");
    }
}
