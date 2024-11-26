package com.example.Api_Server.service;

import com.example.Api_Server.entity.Customer;
import com.example.Api_Server.entity.Event;
import com.example.Api_Server.entity.Ticket;
import com.example.Api_Server.entity.TicketStatus;
import com.example.Api_Server.repository.*;
import jakarta.persistence.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class EventService {
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private TicketService ticketService;
    @Autowired
    private VendorEventAssociationRepository vendorEventAssociationRepository;
    @Autowired
    private VendorRepository vendorRepository;

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock writeLock = readWriteLock.writeLock();
    private final Lock readLock = readWriteLock.readLock();

    public Event addEvent(Event event) {
        return eventRepository.save(event);
    }

    public List<Event> getAll() {
        return eventRepository.findAllWithTickets();
    }

    public Event getLast() {
        return eventRepository.getById(eventRepository.count());
    }

    public int getCount() {
        return (int) eventRepository.count();
    }

    @Transactional
    public Optional<Event> getEventById(int id){
        Optional<Event> event = eventRepository.findById((long) id);
        if(event.isPresent()){
            return event;
        }else{
            return Optional.empty();
        }
    }

    @Transactional
    public void giveTicket(Customer customer, Event event){
        try{
            writeLock.lock();
            Optional<Ticket> ticketOptional = ticketService.getPoolTicket(event);
            if(ticketOptional.isPresent()){
                Ticket ticket = ticketOptional.get();
                    if(ticket.getStatus() != TicketStatus.POOL) return;
                    ticket.setStatus(TicketStatus.SOLD);
                    ticket.setCustomer(customer);
                    ticketService.saveTicket(ticket);
                    String message = String.format("Customer %d purchased ticket %d from event %d", customer.getId(), ticket.getId(), event.getId());
                    customer.logInfo(message);
            }
        }catch (Exception e){
            System.err.println("Error buying ticket: " + e.getMessage());
        }finally {
            writeLock.unlock();
        }
    }


}
