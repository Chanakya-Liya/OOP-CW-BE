package com.example.Api_Server.entity;
import CLI.LoggingConfig;
import CLI.Util;
import jakarta.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.util.Objects;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

@Entity
@Table(name="customers")
@PrimaryKeyJoinColumn(name="user_id")
public class Customer extends User implements Runnable {
    private int retrievalRate;
    private int frequency;
    private static Logger logger = Logger.getLogger(Customer.class.getName());


    public Customer(String fName, String lName, String username, String password, String email, boolean simulated, int retrievalRate, int frequency, String customerLogPath) {
        super(fName, lName, username, password, email, simulated);
        this.retrievalRate = retrievalRate;
        this.frequency = frequency;

        try {
            FileHandler fileHandler = new FileHandler(customerLogPath, true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger = Logger.getLogger(Customer.class.getName() + "-" + getId()); // Unique logger name for each customer
            logger.addHandler(fileHandler);
            logger.setUseParentHandlers(false);
        } catch (IOException | InvalidPathException e) {
            System.err.println("Failed to set up file handler for Customer logger: " + e.getMessage());
            // Handle error appropriately.  Perhaps provide a default logger so the application can continue.
        }
    }

    public Customer() {
        super();
    }

    public int getRetrievalRate() {
        return retrievalRate;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public void setRetrievalRate(int retrievalRate) {
        this.retrievalRate = retrievalRate;
    }

    @Override
    public void run(){
        System.out.println("Customer running: " + getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return retrievalRate == customer.retrievalRate && frequency == customer.frequency;
    }

    @Override
    public int hashCode() {
        return Objects.hash(retrievalRate, frequency);
    }

    @Override
    public String toString() {
        return "Customer{" +
                "CustomerId=" + super.getId() +
                ", retrievalRate=" + retrievalRate +
                ", frequency=" + frequency +
                "} " + super.toString();
    }
}

