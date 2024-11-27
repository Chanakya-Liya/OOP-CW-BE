package com.example.Api_Server.simulation;

import CLI.GenerateSimulation;
import CLI.Util;
import com.example.Api_Server.entity.Vendor;
import com.example.Api_Server.service.VendorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.logging.Logger;

@Component
public class VendorSimulation {
    @Autowired
    private GenerateSimulation generateForSimulation;
    private static final Logger logger = Logger.getLogger(VendorSimulation .class.getName());

    public void addEvent(Vendor vendor){
        while (true) {
            try {
                Thread.sleep(vendor.getEventCreationFrequency() * 1000L); // Correct usage assuming eventCreationFrequency is now an instance member of Vendor class.
                if (Util.getStartOption() == 1) {
                    generateForSimulation.generateForSimulation(false);
                } else {
                    generateForSimulation.generateForThreadTesting(false);
                }
                vendor.logInfo("New Event Created by Vendor: " + vendor.getId() ); //+ " event: " + lastEvent);
            } catch (InterruptedException e) {
                logger.warning("Vendor thread interrupted: " + e.getMessage());
                Thread.currentThread().interrupt();
                break; // Exit the loop to prevent resource leaks if interrupted

            } catch(IOException e){
                logger.warning("Error occurred while trying to create an event: " + e.getMessage());
            }
        }
    }
}
