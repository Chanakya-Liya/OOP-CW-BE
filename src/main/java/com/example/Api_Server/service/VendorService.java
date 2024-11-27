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
import org.springframework.beans.factory.annotation.Autowired;
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

    public Vendor addVendor(Vendor vendor){
        return vendorRepository.save(vendor);
    }

    public int vendorCount(){
        return (int) vendorRepository.count();
    }

    public Vendor getVendor(int id){
        return vendorRepository.getById((long) id);
    }

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

    public void createThreadTestingEvent(Vendor vendor){
        int PoolSize = configManager.getIntValue("ThreadTesting", "event", "PoolSize");
        int TotalEventTickets = configManager.getIntValue("ThreadTesting", "event", "TotalTicketCount");
        int ReleaseRate = configManager.getIntValue("ThreadTesting", "vendor", "ReleaseRate");
        int VendorFrequency = configManager.getIntValue("ThreadTesting", "vendor", "Frequency");

        Event event = new Event(PoolSize, TotalEventTickets);
        event.setVendor(vendor);
        event = eventService.addEvent(event); // Save the event first
        VendorEventAssociation vendorEventAssociation = new VendorEventAssociation(vendor, event, ReleaseRate, VendorFrequency);
        vendorEventAssociationService.addVendorEventAssociation(vendorEventAssociation);
        generateVendorEventAssociations(vendor, event, ReleaseRate, VendorFrequency);
    }

    public void generateVendorEventAssociations(Vendor vendor, Event event, int releaseRate, int frequency) {
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
            VendorEventAssociation vendorEventAssociation = new VendorEventAssociation(vendorAssociation, event, releaseRate, frequency);
            vendorEventAssociationService.addVendorEventAssociation(vendorEventAssociation);
        }
        vendor.addEvent(event);
        vendorRepository.save(vendor);
    }


    public void addEvents(Vendor vendor) {
        while (true) {
            try {
                if (Util.getStartOption() == 1) {
                    createSimulationEvent();
                } else {
                    createThreadTestingEvent(vendor);
                }
                vendor.logInfo("New Event Created by Vendor: " + vendor.getId() ); //+ " event: " + lastEvent);

            } catch (Exception e) { // Catch broader Exception
                logger.warning("Error occurred while trying to create an event: " + e.getMessage());
            }
            try {
                Thread.sleep(vendor.getEventCreationFrequency() * 1000L); // Correct usage assuming eventCreationFrequency is now an instance member of Vendor class.
            } catch (InterruptedException e) {
                logger.warning("Vendor thread interrupted: " + e.getMessage());
                Thread.currentThread().interrupt();
                break; // Exit the loop to prevent resource leaks if interrupted

            }
        }
    }


    public void init() {
        List<Vendor> vendors = vendorRepository.findAll();
        for (Vendor vendor : vendors) {
            if (vendor.isSimulated()) {
                vendor.setVendorService(this);
                Thread vendorThread = new Thread(vendor); //Start vendor threads using lambda
                vendorThread.start();
            }
        }
    }
}
