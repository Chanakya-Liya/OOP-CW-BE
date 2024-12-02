package com.example.Api_Server.controller;

import com.example.Api_Server.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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

}
