package com.example.Api_Server.service;

import CLI.DataGenerator;
import com.example.Api_Server.entity.*;
import com.example.Api_Server.repository.*;
import jakarta.persistence.Entity;
import com.example.Api_Server.DTO.EventDTO;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
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
    @Autowired
    private BuyTicketService buyTicketService;

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock writeLock = readWriteLock.writeLock();
    private final Lock readLock = readWriteLock.readLock();

    @Transactional
    public Event addEvent(Event event) {
        synchronized (this) {
            return eventRepository.save(event);
        }
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

    public void createEvent(String name, String description, MultipartFile photo) throws IOException {
        Event event = new Event();
        event.setName(name);
        eventRepository.save(event);
    }

    public List<EventDTO> getAllEvents() {
        List<EventDTO> events = new ArrayList<>();
        for(Event event : eventRepository.findAll()){
            EventDTO eventDTO = new EventDTO(event);
            events.add(eventDTO);
        }
        return events;
    }

    @Transactional
    public void buyTicket(int eventId, int userId) {
        Optional<Event> eventOptional = eventRepository.findById((long) eventId);
        Optional<Customer> customerOptional = customerRepository.findById((long) userId);
        if(eventOptional.isPresent() && customerOptional.isPresent()){
            Event event = eventOptional.get();
            Customer customer = customerOptional.get();
            buyTicketService.giveTicket(customer, event);
        }else{
            throw new EntityNotFoundException("Event or customer not found");
        }
    }

    public int getEventCount() {
        return eventRepository.findAll().size();
    }
}
