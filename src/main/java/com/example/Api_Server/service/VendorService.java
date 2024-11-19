package com.example.Api_Server.service;

import CLI.ConfigManager;
import CLI.DataGenerator;
import CLI.LoggingConfig;
import CLI.Util;
import com.example.Api_Server.entity.Vendor;
import com.example.Api_Server.repository.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

@Service
public class VendorService {
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
    @Autowired
    private LoggingConfig loggingConfig;
    @Autowired
    private ConfigManager configManager;
    @Autowired
    private DataGenerator dataGenerator;

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

    public void addEvents(Vendor vendor) {
        while (true) {
            try {
                if (Util.getStartOption() == 1) {
                    // Get config values using configManager
                    int poolSizeMin = configManager.getIntValue("Simulation", "event", "PoolSizeMin");
                    int poolSizeMax = configManager.getIntValue("Simulation", "event", "PoolSizeMax");
                    int totalTicketsMin = configManager.getIntValue("Simulation", "event", "TotalEventTicketsMin");
                    int totalTicketsMax = configManager.getIntValue("Simulation", "event", "TotalEventTicketsMax");
                    int releaseRateMin = configManager.getIntValue("Simulation", "vendor", "ReleaseRateMin");
                    int releaseRateMax = configManager.getIntValue("Simulation", "vendor", "ReleaseRateMax");
                    int frequencyMin = configManager.getIntValue("Simulation", "vendor", "FrequencyMin");
                    int frequencyMax = configManager.getIntValue("Simulation", "vendor", "FrequencyMax");

                    dataGenerator.simulateEventsForSimulationTesting(1, poolSizeMin, poolSizeMax, totalTicketsMin, totalTicketsMax, releaseRateMin, releaseRateMax, frequencyMin, frequencyMax, getAll());
                } else {

                    dataGenerator.simulateEventsForThreadTesting(1, configManager.getIntValue("ThreadTesting", "event", "PoolSize"), configManager.getIntValue("ThreadTesting", "event", "TotalTicketCount"), configManager.getIntValue("ThreadTesting", "vendor", "ReleaseRate"), configManager.getIntValue("ThreadTesting", "vendor", "Frequency"), getAll());
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

    @PostConstruct
    public void init() {
        List<Vendor> vendors = vendorRepository.findAll();
        for (Vendor vendor : vendors) {
            if (vendor.isSimulated()) {
                Thread vendorThread = new Thread(() -> addEvents(vendor)); //Start vendor threads using lambda
                vendorThread.start();
            }
        }
    }
}
