package com.example.Api_Server.service;

import com.example.Api_Server.entity.Event;
import com.example.Api_Server.entity.Ticket;
import com.example.Api_Server.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TicketService {
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

    public Ticket addTicket(Ticket ticket){
        return ticketRepository.save(ticket);
    }

    public void updateTicket(Ticket ticket){
        ticketRepository.save(ticket);
    }

    public List<Ticket> getAll(){
        return ticketRepository.findAll();
    }

    @Transactional(readOnly = true)
    public int getTotalTickets() {
        int totalTickets = 0;
        try{
            List<Event> events = eventRepository.findAll();
            for (Event event : events) {
                totalTickets += event.getRemainingTicketCount();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return totalTickets;
    }

    @Transactional
    public Optional<Ticket> getPoolTicket(Event event){
        Optional<Ticket> ticket  = ticketRepository.findPoolTicketByEvent(event);
        if(ticket.isPresent()){
            return ticket;
        }else{
            return Optional.empty();
        }
    }

    public void saveTicket(Ticket ticket){
        ticketRepository.save(ticket);
    }

    public int getSoldTicketCount() {
        return ticketRepository.findSoldTicket().get();
    }
}