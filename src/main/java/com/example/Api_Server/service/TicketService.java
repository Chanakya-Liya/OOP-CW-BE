package com.example.Api_Server.service;

import com.example.Api_Server.entity.Ticket;
import com.example.Api_Server.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
