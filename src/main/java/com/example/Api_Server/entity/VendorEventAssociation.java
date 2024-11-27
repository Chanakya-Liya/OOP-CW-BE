package com.example.Api_Server.entity;
import CLI.Util;
import com.example.Api_Server.service.VendorEventAssociationService;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
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
    @Getter
    @ManyToOne
    @JoinColumn(name = "vendor_id")
    private Vendor vendor;
    @Getter
    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;
    @Setter
    @Getter
    private int releaseRate;
    @Getter
    @Setter
    private int frequency;

    private static final Logger logger = Logger.getLogger(VendorEventAssociation.class.getName());

    @Setter
    @Transient
    private VendorEventAssociationService vendorEventAssociationService;

    public VendorEventAssociation(Vendor vendor, Event event, int releaseRate, int frequency) {
        this.vendor = vendor;
        this.event = event;
        this.releaseRate = releaseRate;
        this.frequency = frequency;
    }

    public VendorEventAssociation() {}

    public void logInfo(String msg){
        logger.info(msg);
    }

    public void logWarning(String msg){
        logger.warning(msg);
    }

    @Override
    public void run(){
        if (vendorEventAssociationService != null) {
            vendorEventAssociationService.performTicketRelease(this);
        } else {
            System.err.println("VendorEventAssociationService is not set for vendor event association: " + associationId);
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

