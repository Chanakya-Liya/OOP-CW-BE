package com.example.Api_Server.entity;
import CLI.Util;
import com.example.Api_Server.service.VendorService;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
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

    @Transient
    private VendorService vendorService;


    public Vendor(String fName, String lName, String username, String password, String email, boolean simulated, int eventCreationFrequency, String vendorLogPath) {
        super(fName, lName, username, password, email, simulated);
        this.eventCreationFrequency = eventCreationFrequency;
    }

    public Vendor(){}

    public void addEvent(Event event) {
        this.events.add(event);
        event.setVendor(this);
    }

    public void logInfo(String msg){
        logger.info(msg);
    }

    public void setEvents(Event event) {
        events.add(event);
    }

    @Override
    public void run(){
        if (vendorService != null) {
            vendorService.addEvents(this);
        } else {
            System.err.println("VendorService is not set for vendor: " + getId());
        }
    }

    @Override
    public String toString() {

        return "Vendor{" +
                ", id=" + super.getId() +
                ", Event Creation Frequency=" + eventCreationFrequency +
                "} " + super.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vendor vendor = (Vendor) o;
        return Objects.equals(id, vendor.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

