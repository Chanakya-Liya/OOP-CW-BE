package com.example.Api_Server.entity;
import CLI.Util;
import jakarta.persistence.*;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

@Entity
@Table(name = "vendor_event_associations")
public class VendorEventAssociation implements Runnable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long associationId;
    @ManyToOne
    @JoinColumn(name = "vendor_id")
    private Vendor vendor;
    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;
    private int releaseRate;
    private int frequency;
    private static Logger logger = Logger.getLogger(VendorEventAssociation.class.getName());

    public VendorEventAssociation(Vendor vendor, Event event, int releaseRate, int frequency, String eventLogPath) {
        this.vendor = vendor;
        this.event = event;
        this.releaseRate = releaseRate;
        this.frequency = frequency;
        try {
            FileHandler fileHandler = new FileHandler(eventLogPath, true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger = Logger.getLogger(Customer.class.getName() + "-" + event.getId()); // Unique logger name for each customer
            logger.addHandler(fileHandler);
            logger.setUseParentHandlers(false);
        } catch (IOException | InvalidPathException e) {
            System.err.println("Failed to set up file handler for Customer logger: " + e.getMessage());
            // Handle error appropriately.  Perhaps provide a default logger so the application can continue.
        }
    }

    public VendorEventAssociation() {}

    public Vendor getVendor() {
        return vendor;
    }

    public Event getEvent() {
        return event;
    }

    public int getReleaseRate() {
        return releaseRate;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setReleaseRate(int releaseRate) {
        this.releaseRate = releaseRate;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    @Override
    public void run(){
        while (!event.allTicketsSold()) {  // Continue until all tickets are sold
            try {
                synchronized (event) {  // Lock the event while modifying ticket lists
                    if (event.getPoolTickets().size() + releaseRate <= event.getPoolSize() && !event.getAvailableTickets().isEmpty()) {
                        int before = event.getPoolTickets().size();
                        int added = Math.min(releaseRate, event.getAvailableTickets().size());
                        for (int i = 0; i < releaseRate; i++) {
                            if (event.getAvailableTickets().isEmpty()) break; // Stop if no tickets are available
                            event.addTicketToPool();
                        }
                        int after = event.getPoolTickets().size();
                        logger.info("Vendor " + vendor.getId() +
                                " added " + added + " tickets to Event " + event.getId() +
                                ". Pool size: " + event.getPoolSize() + " before: " + before + " after: " + after + " available tickets: " + event.getAvailableTickets().size());
                    }
                }
                // Sleep for the specified frequency before the next addition cycle
                Thread.sleep(frequency * 1000L);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warning("VendorEventAssociation for Vendor " + vendor.getId() +
                        " interrupted: " + e.getMessage());
                return;
            }
        }
    }

    @Override
    public String toString() {
        return "VendorEventAssociation{" +
                "vendor=" + vendor.getId() +
                ", event=" + event.getId() +
                ", releaseRate=" + releaseRate +
                ", frequency=" + frequency +
                '}';
    }
}

