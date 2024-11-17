package com.example.Api_Server.service;

import com.example.Api_Server.entity.VendorEventAssociation;
import com.example.Api_Server.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public VendorEventAssociation addVendorEventAssociation(VendorEventAssociation vendorEventAssociation){
        return vendorEventAssociationRepository.save(vendorEventAssociation);
    }

    public void addVendorEventAssociationList(List<VendorEventAssociation> vendorEventAssociations){
        vendorEventAssociationRepository.saveAll(vendorEventAssociations);
    }
}
