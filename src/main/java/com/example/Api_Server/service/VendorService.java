package com.example.Api_Server.service;

import CLI.ConfigManager;
import CLI.DataGenerator;
import CLI.LoggingConfig;
import CLI.Util;
import com.example.Api_Server.entity.Vendor;
import com.example.Api_Server.repository.*;
import com.example.Api_Server.simulation.VendorSimulation;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Autowired
    private VendorSimulation vendorSimulation;

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
    public Vendor addVendor(Vendor vendor){
        return vendorRepository.save(vendor);
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

    public void addEvents(Vendor vendor) {
        vendorSimulation.addEvent(vendor);
    }

    @Transactional
    public void saveAllVendors(List<Vendor> vendors){
        vendorRepository.saveAll(vendors);
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
