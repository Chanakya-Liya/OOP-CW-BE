package entity;
import jakarta.persistence.*;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

@Entity
@Table(name = "vendors")
@PrimaryKeyJoinColumn(name = "user_id")
public class Vendor extends User implements Runnable{
    private int eventCreationFrequency;
    @OneToMany(mappedBy = "vendor", cascade = CascadeType.ALL)
    private ArrayList<Event> events = new ArrayList<>();
    private static final Logger logger = Logger.getLogger(Vendor.class.getName());

    static {
        try {
            String filePath = Util.getVendorLog();
            FileHandler fileHandler = new FileHandler(filePath, true); // "true" to append to the file
            fileHandler.setFormatter(new SimpleFormatter());  // Sets a simple text format for logs
            logger.addHandler(fileHandler);
            logger.setUseParentHandlers(false); // Disables logging to console
        } catch (IOException e) {
            logger.warning("Failed to set up file handler for logger: " + e.getMessage());
        }catch(InvalidPathException e){
            logger.warning("Failed to set up file handler for logger hehe: " + e.getMessage());
        }
    }

    public Vendor(String fName, String lName, String username, String password, String email, boolean simulated) {
        super(fName, lName, username, password, email, simulated);
        if(Util.getStartOption() == 1){
            eventCreationFrequency = Util.generateRandomInt(Util.readJsonFile("Simulation", "event", "EventCreationFrequencyMin"), Util.readJsonFile("Simulation", "event", "EventCreationFrequencyMax"));
        }else{
            eventCreationFrequency = Util.readJsonFile("ThreadTesting", "event", "EventCreationFrequency");
        }
    }

    public Vendor(){}

    public ArrayList<Event> getEvents() {
        return events;
    }

    public void setEvents(ArrayList<Event> events) {
        this.events = events;
    }

    public void setEvents(Event event) {
        events.add(event);
    }

    @Override
    public void run(){
        while(true){
            try{
                if(Util.getStartOption() == 1){
                    Util.generateForSimulation(false);
                }else{
                    Util.generateForThreadTesting(false);
                }
                Util.getEvents().getLast().setVendor(this);
                logger.info("New Event Created by Vendor:" + super.getId() + " event: " + Util.getEvents().getLast());
            }catch (IOException e){
                logger.warning("Error occurred while trying to create an event: " + e);
            }
            try {
                Thread.sleep(eventCreationFrequency * 1000L);
            } catch (InterruptedException e) {
                logger.warning("Error occurred while trying to create an event: " + e);
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder eventIdBuilder = new StringBuilder("[");
        for (Event event : events) {
            eventIdBuilder.append(event.getId()).append(", ");
        }

        if (!events.isEmpty()) {
            eventIdBuilder.setLength(eventIdBuilder.length() - 2);
        }
        eventIdBuilder.append("]");
        return "Vendor{" +
                "eventIds=" + eventIdBuilder +
                ", id=" + super.getId() +
                ", Event Creation Frequency=" + eventCreationFrequency +
                "} " + super.toString();
    }
}

