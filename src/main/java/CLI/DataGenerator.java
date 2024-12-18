// DataGenerator.java
package CLI;

import com.example.Api_Server.entity.Customer;
import com.example.Api_Server.entity.Event;
import com.example.Api_Server.entity.Vendor;
import com.example.Api_Server.entity.VendorEventAssociation;
import com.example.Api_Server.repository.CustomerRepository;
import com.example.Api_Server.repository.TicketRepository;
import com.example.Api_Server.repository.VendorRepository;
import com.example.Api_Server.service.CustomerService;
import com.example.Api_Server.service.EventService;
import com.example.Api_Server.service.VendorEventAssociationService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class DataGenerator {

    private final Random random = new Random();

    private static final String fNameFile = "src/main/java/CLI/Static/fName.txt";
    private static final String lNameFIle = "src/main/java/CLI/Static/lName.txt";
    private static final String passwordFile = "src/main/java/CLI/Static/passwords.txt";
    private static final String email = "src/main/java/CLI/Static/email.txt";
    private static final String username = "src/main/java/CLI/Static/username.txt";
    private static final String eventFile = "src/main/java/CLI/Static/eventName.txt";
    private static final String description = "src/main/java/CLI/Static/description.txt";
    private ArrayList<String> fNames = new ArrayList<>();
    private ArrayList<String> lNames = new ArrayList<>();
    private ArrayList<String> passwords = new ArrayList<>();
    private ArrayList<String> emails = new ArrayList<>();
    private ArrayList<String> usernames = new ArrayList<>();
    private ArrayList<String> eventNames = new ArrayList<>();
    private ArrayList<String> descriptions = new ArrayList<>();

    @Autowired
    private VendorRepository vendorRepository;
    @Autowired
    private VendorEventAssociationService vendorEventAssociationService;
    @Autowired
    private EventService eventService;
    @Autowired
    private ConfigManager configManager;
    @Autowired
    private LoggingConfig loggingConfig;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private TicketRepository ticketRepository;

    // Load the files into the arraylists
    @PostConstruct
    private void loadNames() throws IOException {
        loadFile(fNameFile, fNames);
        loadFile(lNameFIle, lNames);
        loadFile(passwordFile, passwords);
        loadFile(email, emails);
        loadFile(username, usernames);
        loadFile(eventFile, eventNames);
        loadFile(description, descriptions);
    }

    // Generate a random date time
    public static LocalDateTime generateRandomDateTime() {
        long startEpochSecond = LocalDateTime.now().toEpochSecond(java.time.ZoneOffset.UTC);
        long endEpochSecond = LocalDateTime.now().plusMonths(10).toEpochSecond(java.time.ZoneOffset.UTC);
        long randomEpochSecond = ThreadLocalRandom.current().nextLong(startEpochSecond, endEpochSecond);
        LocalDateTime randomDateTime =  LocalDateTime.ofEpochSecond(randomEpochSecond, 0, java.time.ZoneOffset.UTC);
        return randomDateTime.truncatedTo(ChronoUnit.HOURS);
    }

    // Load the files into the arraylists
    private void loadFile(String filePath, ArrayList<String> arrayList) throws IOException {
        Scanner fileScanner = new Scanner(new File(filePath));
        while(fileScanner.hasNextLine()){
            arrayList.add(fileScanner.nextLine());
        }
    }

    // Generate a random image from a list
    public String generateImageByte() throws IOException {
        return "src/main/resources/Static/images/" + generateRandomInt(1, 13) + ".jpg";
    }

    // Generate a random image from a list
    public String generateRandomString(String stringRequired) throws IOException {
        if(stringRequired.equalsIgnoreCase("fname")){
            return fNames.get(random.nextInt(fNames.size()));
        } else if (stringRequired.equalsIgnoreCase("lname")) {
            return lNames.get(random.nextInt(lNames.size()));
        } else if(stringRequired.equalsIgnoreCase("password")){
            return passwords.get(random.nextInt(passwords.size()));
        }else if(stringRequired.equalsIgnoreCase("email")){
            return emails.get(random.nextInt(emails.size()));
        } else if (stringRequired.equalsIgnoreCase("event")){
            return eventNames.get(random.nextInt(eventNames.size()));
        }else if(stringRequired.equalsIgnoreCase("description")){
            return descriptions.get(random.nextInt(descriptions.size()));
        }else{
            return usernames.get(random.nextInt(usernames.size()));
        }
    }

    public int generateRandomInt(int start, int end){
        if(start == end || start > end){
            return end;
        }
        return random.nextInt(start, end);
    }

    // Generate customers for the simulation
    public List<Customer> simulateCustomers(int simulateCustomers, int customerFrequency, int customerRetrieve, CustomerService customerService) throws IOException {
        List<Customer> customers = new ArrayList<>();
        for(int i = 0; i < simulateCustomers; i++){
            Customer customer = new Customer(generateRandomString("fname"), generateRandomString("lname"), generateRandomString("username"), generateRandomString("password"), generateRandomString("email"), true, customerRetrieve, customerFrequency);
            customer.setCustomerService(customerService);
            customers.add(customer);
        }
        return customers;
    }

    // Generate customers for the simulation
    public List<Customer> simulateCustomers(int simulateCustomers, int customerRetrieveMin, int customerRetrieveMax, int customerFrequencyMin, int customerFrequencyMax, CustomerService customerService) throws IOException {
        List<Customer> customers = new ArrayList<>();
        for(int i = 0; i < simulateCustomers; i++){
            Customer customer = new Customer(generateRandomString("fname"), generateRandomString("lname"), generateRandomString("username"), generateRandomString("password"), generateRandomString("email"), true, generateRandomInt(customerRetrieveMin, customerRetrieveMax), generateRandomInt(customerFrequencyMin, customerFrequencyMax));
            customer.setCustomerService(customerService);
            customers.add(customer);
        }
        Customer customer = new Customer("admin", "admin", "admin", "chana123", "a@a");
        customer.setCustomerService(customerService);
        return customers;
    }

    // Generate vendors for the simulation
    public List<Vendor> simulateVendors(int simulateVendors, int eventCreationFrequencyMin, int eventCreationFrequencyMax) throws IOException {
        List<Vendor> vendors = new ArrayList<>();
        for(int i = 0; i < simulateVendors; i++){
            Vendor vendor = new Vendor(generateRandomString("fname"), generateRandomString("lname"), generateRandomString("username"), generateRandomString("password"), generateRandomString("email"), true, generateRandomInt(eventCreationFrequencyMin, eventCreationFrequencyMax), loggingConfig.getVendorLog());
            vendors.add(vendor);
        }
        return vendors;
    }

    // Generate vendors for the simulation
    public List<Vendor> simulateVendors(int simulateVendors, int eventCreationFrequency) throws IOException {
        List<Vendor> vendors = new ArrayList<>();
        for(int i = 0; i < simulateVendors; i++){
            Vendor vendor = new Vendor(generateRandomString("fname"), generateRandomString("lname"), generateRandomString("username"), generateRandomString("password"), generateRandomString("email"), true, eventCreationFrequency,loggingConfig.getVendorLog());
            vendors.add(vendor);
        }
        return vendors;
    }

    // Generate events for the simulation
    public void simulateEventsForThreadTesting(int simulatedEvent, int poolSize, int totalEventTickets, int releaseRate, int frequency, List<Vendor> vendors) throws IOException {
        for (int i = 0; i < simulatedEvent; i++) {
            Event event = new Event(generateRandomString("event"), poolSize, totalEventTickets);
            event.setDescription(generateRandomString("description"));
            event.setEventDateTime(generateRandomDateTime());
            event.setPhoto(generateImageByte());
            event = eventService.addEvent(event); // Save the event first
            int vendorCount = generateRandomInt(-5, (int) vendorRepository.count());
            if (vendorCount <= 0) {
                vendorCount = 1;
            }
            ArrayList<Integer> addedVendors = new ArrayList<>(); // Track added vendors
            for (int j = 0; j < vendorCount; j++) {
                int vendorPosition;
                do {
                    vendorPosition = generateRandomInt(0, vendors.size());
                } while (addedVendors.contains(vendorPosition));
                addedVendors.add(vendorPosition); // Add vendor position *after* checking


                Vendor vendor = vendors.get(vendorPosition); // Get vendor by ID from service
                vendor.addEvent(event);  // Manage the bidirectional relationship
                vendorRepository.save(vendor);  // Persist the vendor change immediately

                VendorEventAssociation vendorEventAssociation = new VendorEventAssociation(vendor, event, releaseRate, frequency);
                vendorEventAssociationService.addVendorEventAssociation(vendorEventAssociation); // Save the association immediately
            }
        }
    }

    // Generate events for the simulation
    public void simulateEventsForSimulationTesting(int simulatedEvent, int poolSizeMin, int poolSizeMax, int totalEventTicketsMin, int totalEventTicketsMax, int releaseRateMin, int releaseRateMax, int frequencyMin, int frequencyMax, List<Vendor> vendors) throws IOException {
        for (int i = 0; i < simulatedEvent; i++) {
            Event event = new Event(generateRandomString("event"),generateRandomInt(poolSizeMin, poolSizeMax), generateRandomInt(totalEventTicketsMin, totalEventTicketsMax));
            event.setDescription(generateRandomString("description"));
            event.setEventDateTime(generateRandomDateTime());
            event.setPhoto(generateImageByte());
            event = eventService.addEvent(event); // Save event first

            int vendorCount = generateRandomInt(-5, (int) vendorRepository.count());
            if (vendorCount <= 0) {
                vendorCount = 1;
            }
            ArrayList<Integer> addedVendors = new ArrayList<>();

            for (int j = 0; j < vendorCount; j++) {
                int vendorPosition;
                do {
                    vendorPosition = generateRandomInt(0, vendors.size());
                } while (addedVendors.contains(vendorPosition));
                addedVendors.add(vendorPosition); // Add vendor position *after* checking


                Vendor vendor = vendors.get(vendorPosition); // Retrieve vendor by ID
                vendor.addEvent(event); // Use addEvent to manage bidirectional relationship
                vendorRepository.save(vendor); // Save the vendor


                VendorEventAssociation vendorEventAssociation = new VendorEventAssociation(vendor, event, generateRandomInt(releaseRateMin, releaseRateMax), generateRandomInt(frequencyMin, frequencyMax));
                vendorEventAssociationService.addVendorEventAssociation(vendorEventAssociation);
            }
        }
    }
}