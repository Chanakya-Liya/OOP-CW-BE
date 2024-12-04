package com.example.Api_Server.service;

import CLI.ConfigManager;
import CLI.DataGenerator;
import CLI.LoggingConfig;
import CLI.Util;
import com.example.Api_Server.entity.Event;
import com.example.Api_Server.entity.Vendor;
import com.example.Api_Server.entity.VendorEventAssociation;
import com.example.Api_Server.repository.*;
import jakarta.persistence.OptimisticLockException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;


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
    public void createSimulationEvent(Vendor vendor) throws IOException {
        int poolSizeMin = configManager.getIntValue("Simulation", "event", "PoolSizeMin");
        int poolSizeMax = configManager.getIntValue("Simulation", "event", "PoolSizeMax");
        int totalTicketsMin = configManager.getIntValue("Simulation", "event", "TotalEventTicketsMin");
        int totalTicketsMax = configManager.getIntValue("Simulation", "event", "TotalEventTicketsMax");
        int releaseRateMin = configManager.getIntValue("Simulation", "vendor", "ReleaseRateMin");
        int releaseRateMax = configManager.getIntValue("Simulation", "vendor", "ReleaseRateMax");
        int frequencyMin = configManager.getIntValue("Simulation", "vendor", "FrequencyMin");
        int frequencyMax = configManager.getIntValue("Simulation", "vendor", "FrequencyMax");
        try{
            Event event = new Event(dataGenerator.generateRandomString("event"),dataGenerator.generateRandomInt(poolSizeMin, poolSizeMax), dataGenerator.generateRandomInt(totalTicketsMin, totalTicketsMax));
            event.setVendor(vendor);
            event.setDescription(dataGenerator.generateRandomString("description"));
            event.setEventDateTime(dataGenerator.generateRandomDateTime());
            event.setPhoto(dataGenerator.generateImageByte());
            Event savedEvent = eventService.addEvent(event);
            VendorEventAssociation vendorEventAssociation = new VendorEventAssociation(vendor, savedEvent, dataGenerator.generateRandomInt(releaseRateMin, releaseRateMax), dataGenerator.generateRandomInt(frequencyMin, frequencyMax));
            vendorEventAssociationService.addVendorEventAssociation(vendorEventAssociation);
            vendor.addEvent(event);
            addVendor(vendor);
            generateVendorEventAssociations(vendor, savedEvent, releaseRateMin, releaseRateMax, frequencyMin, frequencyMax);
        } catch (Exception e) {
            vendor.logWarning("Error occurred while trying to create an event one: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public void createThreadTestingEvent(Vendor vendor){
        int PoolSize = configManager.getIntValue("ThreadTesting", "event", "PoolSize");
        int TotalEventTickets = configManager.getIntValue("ThreadTesting", "event", "TotalTicketCount");
        int ReleaseRate = configManager.getIntValue("ThreadTesting", "vendor", "ReleaseRate");
        int VendorFrequency = configManager.getIntValue("ThreadTesting", "vendor", "Frequency");
        try{
            Event event = new Event(dataGenerator.generateRandomString("event"), PoolSize, TotalEventTickets);
            event.setVendor(vendor);
            event.setEventDateTime(dataGenerator.generateRandomDateTime());
            event.setPhoto(dataGenerator.generateImageByte());
            event.setDescription(dataGenerator.generateRandomString("description"));
            Event savedEvent = eventService.addEvent(event); // Save the event first
            VendorEventAssociation vendorEventAssociation = new VendorEventAssociation(vendor, savedEvent, ReleaseRate, VendorFrequency);
            vendorEventAssociationService.addVendorEventAssociation(vendorEventAssociation);
            vendor.addEvent(event);
            addVendor(vendor);
            generateVendorEventAssociations(vendor, savedEvent, ReleaseRate, VendorFrequency);
        } catch (Exception e) {
            vendor.logWarning("Error occurred while trying to create an event one: " + e.getMessage());
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
            vendor.logWarning("Optimistic lock exception occurred: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            vendor.logWarning("Error occurred while trying to create an event : " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public void generateVendorEventAssociations(Vendor vendor, Event event, int releaseRateMin, int releaseRateMax, int frequencyMin, int frequencyMax) {
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
                VendorEventAssociation vendorEventAssociation = new VendorEventAssociation(vendorAssociation, event, dataGenerator.generateRandomInt(releaseRateMin, releaseRateMax), dataGenerator.generateRandomInt(frequencyMin, frequencyMax));
                vendorEventAssociationService.addVendorEventAssociation(vendorEventAssociation);
            }
        }catch (OptimisticLockException e) {
            vendor.logWarning("Optimistic lock exception occurred: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            vendor.logWarning("Error occurred while trying to create an event : " + e.getMessage());
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
                    createSimulationEvent(vendor);
                } else {
                    createThreadTestingEvent(vendor);
                }
                vendor.logInfo("New Event Created by Vendor: " + vendor.getId());
            } catch (Exception e) {
                vendor.logWarning("Error occurred while trying to create an event: " + e.getMessage());
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

    public boolean checkVendor(String email, String password) {
        List<Vendor> vendors = vendorRepository.findAll();
        for (Vendor vendor : vendors) {
            if (vendor.getEmail().equals(email) && vendor.getPassword().equals(vendor.hashPassword(password))) {
                return true;
            }
        }
        return false;
    }

    public Vendor getVendorFromEmail(String email) {
        List<Vendor> vendors = vendorRepository.findAll();
        for (Vendor vendor : vendors) {
            if (vendor.getEmail().equals(email)) {
                return vendor;
            }
        }
        return null;
    }
}
