package com.example.Api_Server.service;

import com.example.Api_Server.entity.Event;
import com.example.Api_Server.repository.*;
import jakarta.persistence.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventService {
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

    public Event addEvent(Event event){
        return eventRepository.save(event);
    }

    public List<Event> getAll(){
        return eventRepository.findAll();
    }

    public Event getLast(){
        return eventRepository.getById(eventRepository.count());
    }

    public int getCount(){
        return (int) eventRepository.count();
    }
}
