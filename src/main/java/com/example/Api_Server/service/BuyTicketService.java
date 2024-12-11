package com.example.Api_Server.service;

import com.example.Api_Server.entity.Customer;
import com.example.Api_Server.entity.Event;
import com.example.Api_Server.entity.Ticket;
import com.example.Api_Server.entity.TicketStatus;
import jakarta.persistence.OptimisticLockException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class BuyTicketService{

    @Autowired
    private TicketService ticketService;

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock writeLock = readWriteLock.writeLock();
    private final Lock readLock = readWriteLock.readLock();

    // This method is used to give a ticket to a customer
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
                try{
                    ticketService.saveTicket(ticket);
                    String message = String.format("Customer %d purchased ticket %d from event %d", customer.getId(), ticket.getId(), event.getId());
                    customer.logInfo(message);
                }catch(OptimisticLockException e){
                    System.err.println("Ticket has already been sold");
                    customer.logWarning("Ticket has already been sold");
                }
            }
        }catch (Exception e){
            System.err.println("Error buying ticket: " + e.getMessage());
            customer.logWarning("Error buying ticket: " + e.getMessage());
        }finally {
            writeLock.unlock();
        }
    }
}
