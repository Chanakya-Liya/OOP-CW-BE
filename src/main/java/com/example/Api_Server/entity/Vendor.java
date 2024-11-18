package com.example.Api_Server.entity;
import CLI.Util;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

@Setter
@Getter
@Entity
@Table(name = "vendors")
@PrimaryKeyJoinColumn(name = "user_id")
public class Vendor extends User implements Runnable{
    private int eventCreationFrequency;
    @OneToMany(mappedBy = "vendor", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<Event> events = new HashSet<>();
    private static Logger logger = Logger.getLogger(Vendor.class.getName());


    public Vendor(String fName, String lName, String username, String password, String email, boolean simulated, int eventCreationFrequency, String vendorLogPath) {
        super(fName, lName, username, password, email, simulated);
        this.eventCreationFrequency = eventCreationFrequency;
        try {
            File logFile = new File(vendorLogPath);
            if(!logFile.exists()) {
                FileHandler fileHandler = new FileHandler(vendorLogPath, true);
                fileHandler.setFormatter(new SimpleFormatter());
                logger.addHandler(fileHandler);
                logger.setUseParentHandlers(false);
            }

        } catch (IOException | InvalidPathException e) {
            System.err.println("Failed to set up file handler for Customer logger: " + e.getMessage());
            // Handle error appropriately.  Perhaps provide a default logger so the application can continue.
        }
    }

    public Vendor(){}

    public void addEvent(Event event) {
        this.events.add(event);
        event.setVendor(this);
    }

    public void setEvents(Event event) {
        events.add(event);
    }

    @Override
    public void run(){
        System.out.println("Vendor Running: " + getId());
    }

    @Override
    public String toString() {
        StringBuilder eventIdBuilder = new StringBuilder("[");
        for (Event event : events) {
            eventIdBuilder.append(event.getId()).append(", ");
        }

        if (!events.isEmpty()) {
            eventIdBuilder.setLength(eventIdBuilder.length() - 2);
        }
        eventIdBuilder.append("]");
        return "Vendor{" +
                "eventIds=" + eventIdBuilder +
                ", id=" + super.getId() +
                ", Event Creation Frequency=" + eventCreationFrequency +
                "} " + super.toString();
    }
}

