package com.example.Api_Server.service;

import CLI.DataGenerator;
import com.example.Api_Server.entity.Event;
import com.example.Api_Server.entity.Vendor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Service
public class CreateEventService {
    @Autowired
    private EventService eventService;
    @Autowired
    private DataGenerator dataGenerator;

    // Create a new event for simulation
    @Transactional
    public Event createNewEventForSimulation(Vendor vendor, int poolSizeMin, int poolSizeMax, int totalTicketsMin, int totalTicketsMax) throws IOException {
        Event event = new Event(dataGenerator.generateRandomString("event"),dataGenerator.generateRandomInt(poolSizeMin, poolSizeMax), dataGenerator.generateRandomInt(totalTicketsMin, totalTicketsMax));
        event.setVendor(vendor);
        event.setDescription(dataGenerator.generateRandomString("description"));
        event.setEventDateTime(DataGenerator.generateRandomDateTime());
        event.setPhoto(dataGenerator.generateImageByte());
        return eventService.addEvent(event);
    }

    // Create a new event for simulation
    @Transactional
    public Event createNewEventForSimulation(Vendor vendor, int poolSize, int totalTickets) throws IOException {
        Event event = new Event(dataGenerator.generateRandomString("event"),poolSize , totalTickets);
        event.setVendor(vendor);
        event.setDescription(dataGenerator.generateRandomString("description"));
        event.setEventDateTime(DataGenerator.generateRandomDateTime());
        event.setPhoto(dataGenerator.generateImageByte());
        return eventService.addEvent(event);
    }

}
