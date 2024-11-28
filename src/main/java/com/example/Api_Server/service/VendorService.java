package com.example.Api_Server.service;

import CLI.ConfigManager;
import CLI.DataGenerator;
import CLI.LoggingConfig;
import CLI.Util;
import com.example.Api_Server.entity.Event;
import com.example.Api_Server.entity.Vendor;
import com.example.Api_Server.entity.VendorEventAssociation;
import com.example.Api_Server.repository.*;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.OptimisticLockException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

@Service
public class VendorService {
    @Autowired
    private CustomerService customerService;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private VendorEventAssociationRepository vendorEventAssociationRepository;
    @Autowired
    private VendorEventAssociationService vendorEventAssociationService;
    @Autowired
    private VendorRepository vendorRepository;
    @Autowired
    private LoggingConfig loggingConfig;
    @Autowired
    private ConfigManager configManager;
    @Autowired
    private DataGenerator dataGenerator;
    @Autowired
    private EventService eventService;

    private volatile boolean running = true;

    private static final Logger logger;
    static {
        logger = Logger.getLogger(VendorService.class.getName());

        try {
            FileHandler fileHandler = new FileHandler(new LoggingConfig().getEventLog(), true); // "true" to append to the file
            fileHandler.setFormatter(new SimpleFormatter());  // Sets a simple text format for logs
            logger.addHandler(fileHandler);
            logger.setUseParentHandlers(false); // Disables logging to console
        } catch (IOException e) {
            logger.warning("Failed to set up file handler for logger: " + e.getMessage());
        }catch(InvalidPathException e){
            logger.warning("Failed to set up file handler for logger: " + e.getMessage());
        }
    }

    @Transactional
    public void addVendor(Vendor vendor){
        vendorRepository.save(vendor);
    }

    @Transactional
    public int vendorCount(){
        return (int) vendorRepository.count();
    }

    @Transactional
    public Vendor getVendor(int id){
        return vendorRepository.getById((long) id);
    }

    @Transactional
    public List<Vendor> getAll(){
        return vendorRepository.findAll();
    }


    @Transactional
    public void createSimulationEvent(){
        int poolSizeMin = configManager.getIntValue("Simulation", "event", "PoolSizeMin");
        int poolSizeMax = configManager.getIntValue("Simulation", "event", "PoolSizeMax");
        int totalTicketsMin = configManager.getIntValue("Simulation", "event", "TotalEventTicketsMin");
        int totalTicketsMax = configManager.getIntValue("Simulation", "event", "TotalEventTicketsMax");
        int releaseRateMin = configManager.getIntValue("Simulation", "vendor", "ReleaseRateMin");
        int releaseRateMax = configManager.getIntValue("Simulation", "vendor", "ReleaseRateMax");
        int frequencyMin = configManager.getIntValue("Simulation", "vendor", "FrequencyMin");
        int frequencyMax = configManager.getIntValue("Simulation", "vendor", "FrequencyMax");

        Event event = new Event(dataGenerator.generateRandomInt(poolSizeMin, poolSizeMax), dataGenerator.generateRandomInt(totalTicketsMin, totalTicketsMax));
        event = eventService.addEvent(event); // Save event first

    }

    @Transactional
    public void createThreadTestingEvent(Vendor vendor){
        int PoolSize = configManager.getIntValue("ThreadTesting", "event", "PoolSize");
        int TotalEventTickets = configManager.getIntValue("ThreadTesting", "event", "TotalTicketCount");
        int ReleaseRate = configManager.getIntValue("ThreadTesting", "vendor", "ReleaseRate");
        int VendorFrequency = configManager.getIntValue("ThreadTesting", "vendor", "Frequency");
        try{
            Event event = new Event(PoolSize, TotalEventTickets);
            event.setVendor(vendor);
            Event savedEvent = eventService.addEvent(event); // Save the event first
            VendorEventAssociation vendorEventAssociation = new VendorEventAssociation(vendor, savedEvent, ReleaseRate, VendorFrequency);
            vendorEventAssociationService.addVendorEventAssociation(vendorEventAssociation);
            vendor.addEvent(event);
            addVendor(vendor);
            generateVendorEventAssociations(vendor, savedEvent, ReleaseRate, VendorFrequency);
        } catch (Exception e) {
            logger.warning("Error occurred while trying to create an event one: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public void generateVendorEventAssociations(Vendor vendor, Event event, int releaseRate, int frequency) {
        try{
            int customerSize = customerService.getAll().size();
            int vendorCount = dataGenerator.generateRandomInt(-5, vendorCount() - 1);
            if (vendorCount <= 0) {
                vendorCount = 0;
            }
            Set<Vendor> addedVendors = new HashSet<>(); // Use Set to avoid duplicates automatically

            for (int j = 0; j < vendorCount; j++) {
                Optional<Vendor> vendorOptional;
                do {
                    vendorOptional = vendorRepository.findById(dataGenerator.generateRandomInt(1, vendorCount() - 5) + customerSize);
                } while (vendorOptional.isEmpty() || addedVendors.contains(vendorOptional.get()) || vendorOptional.get().getId() == vendor.getId());

                Vendor vendorAssociation = vendorOptional.get(); // Extract the vendor
                addedVendors.add(vendorAssociation); // Add it to the set
                vendorAssociation.addEvent(event); // Manage the bidirectional relationship
                addVendor(vendorAssociation);
                VendorEventAssociation vendorEventAssociation = new VendorEventAssociation(vendorAssociation, event, releaseRate, frequency);
                vendorEventAssociationService.addVendorEventAssociation(vendorEventAssociation);
            }
        }catch (OptimisticLockException e) {
            logger.warning("Optimistic lock exception occurred: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.warning("Error occurred while trying to create an event two: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        running = false;
    }

    @Transactional
    public void addEvents(Vendor vendor) {
        while (running) {
            try {
                Thread.sleep(vendor.getEventCreationFrequency() * 1000L);
                if (Util.getStartOption() == 1) {
                    createSimulationEvent();
                } else {
                    createThreadTestingEvent(vendor);
                }
                vendor.logInfo("New Event Created by Vendor: " + vendor.getId());
            } catch (Exception e) {
                logger.warning("Error occurred while trying to create an event: " + e.getMessage());
            }
        }
    }


    public void init() {
        List<Vendor> vendors = vendorRepository.findAll();
        for (Vendor vendor : vendors) {
            if (vendor.isSimulated()) {
                vendor.setVendorService(this);
                Thread vendorThread = new Thread(vendor);
                vendorThread.start();
            }
        }
    }
}
