package com.example.Api_Server.entity;
import CLI.LoggingConfig;
import CLI.Util;
import com.example.Api_Server.service.CustomerService;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.util.Objects;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

@Setter
@Entity
@Table(name="customers")
@PrimaryKeyJoinColumn(name="user_id")
public class Customer extends User implements Runnable {
    @Getter
    private int retrievalRate;
    @Getter
    private int frequency;
    private static Logger logger = Logger.getLogger(Customer.class.getName());

    @Transient
    private CustomerService customerService;


    public Customer(String fName, String lName, String username, String password, String email, boolean simulated, int retrievalRate, int frequency) {
        super(fName, lName, username, password, email, simulated);
        this.retrievalRate = retrievalRate;
        this.frequency = frequency;
    }

    public Customer(String fName, String lName, String username, String password, String email) {
        super(fName, lName, username, password, email, false);
    }

    public Customer() {
        super();
    }

    public void logInfo(String msg){
        logger.info(msg);
    }

    @Override
    public void run(){
        if (customerService != null) {
            customerService.performTicketRetrieval(this);
        } else {
            System.err.println("CustomerService is not set for customer: " + getId());
        }
    }

    public void logWarning(String warning){
        logger.warning(warning);
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

