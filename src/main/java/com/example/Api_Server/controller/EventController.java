package com.example.Api_Server.controller;

import com.example.Api_Server.DTO.EventDTO;
import com.example.Api_Server.DTO.UserIdDTO;
import com.example.Api_Server.entity.Event;
import com.example.Api_Server.service.EventService;
import jakarta.websocket.server.PathParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/events")
public class EventController {

    @Autowired
    private EventService eventService;

    @PostMapping("/create")
    public ResponseEntity<String> createEvent(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("photo") MultipartFile photo) {
        try {
            // Save the event and photo
            eventService.createEvent(name, description, photo);
            return ResponseEntity.ok("Event created successfully");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error saving event: " + e.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<EventDTO>> getAllEvents() {
        List<EventDTO> events = eventService.getAllEvents();
        if (events.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDTO> getEvent(@PathVariable int id) {
        Optional<Event> event = eventService.getEventById(id);
        if(event.isPresent()){
            EventDTO eventDTO = new EventDTO(event.get());
            return ResponseEntity.ok(eventDTO);
        }else{
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/buy/{id}")
    public ResponseEntity<String> buyTicket(@PathVariable int id, @RequestBody UserIdDTO userId) {
        try {
            eventService.buyTicket(id, userId.getUserId());
            return ResponseEntity.ok("Ticket bought successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error buying ticket: " + e.getMessage());
        }
    }
}
