package com.example.Api_Server.service;

import com.example.Api_Server.entity.*;
import com.example.Api_Server.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class VendorEventAssociationService {
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
    private final Object lock = new Object();

    @Transactional
    public void addVendorEventAssociation(VendorEventAssociation vendorEventAssociation){
        vendorEventAssociationRepository.save(vendorEventAssociation);
    }

    @Transactional
    public void addVendorEventAssociationList(List<VendorEventAssociation> vendorEventAssociations){
        vendorEventAssociationRepository.saveAll(vendorEventAssociations);
    }

    @Transactional
    public void performTicketRelease(VendorEventAssociation vendorEventAssociation) {
        while (vendorEventAssociation.getEvent().getSoldTickets().size() < vendorEventAssociation.getEvent().getTotalTickets()) {  // Continue as long as there are unsold tickets in the database.
            try {
                synchronized (lock) { // Synchronize on lock object
                    Optional<Event> optionalEvent = eventRepository.findById((long)vendorEventAssociation.getEvent().getId()); //Retrieve Event from database
                    if(optionalEvent.isEmpty()){
                        break;
                    }
                    Event event = optionalEvent.get();
                    int added = 0;
                    if (event.getPoolTickets().size() + vendorEventAssociation.getReleaseRate() <= event.getPoolSize()) {
                        for (int i = 0; i < vendorEventAssociation.getReleaseRate(); i++) {

                            int availableTickets = event.getTotalTickets() - (int) (event.getSoldTickets().size() + event.getPoolTickets().size()); //Check the number of available tickets using repository
                            if (availableTickets <= 0) break;

                            Ticket ticket = new Ticket(event);
                            ticket.setStatus(TicketStatus.POOL);//Update status from available to pool

                            ticketRepository.save(ticket); //Persist the changes
                            added++;
                        }

                        String message = String.format("Vendor %d added %d tickets to Event %d. Pool size: %d available tickets: %d", vendorEventAssociation.getVendor().getId(), added, event.getId(), event.getPoolSize(), event.getTotalTickets() - (event.getPoolTickets().size() + event.getSoldTickets().size()));
                        vendorEventAssociation.logInfo(message);// Log message
                    }
                }

                Thread.sleep(vendorEventAssociation.getFrequency() * 1000L);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Proper interruption handling
                vendorEventAssociation.logWarning("VendorEventAssociation thread interrupted: " + e.getMessage());
                break; // Exit the loop when the thread is interrupted
            }
        }
    }

    @Transactional
    public void init() {
        List<VendorEventAssociation> vendorEventAssociations = vendorEventAssociationRepository.findAll();
        for (VendorEventAssociation vendorEventAssociation : vendorEventAssociations) {
            vendorEventAssociation.setVendorEventAssociationService(this);
            Thread vendorAssociationThread = new Thread(vendorEventAssociation);
            vendorAssociationThread.start();
        }
    }
}
