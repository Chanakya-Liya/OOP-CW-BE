package entity;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import service.CustomerService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

@Component
public class Util {
    private final static ArrayList<Customer> customers = new ArrayList<>();
    private final static ArrayList<Vendor> vendors = new ArrayList<>();
    private final static ArrayList<Event> events = new ArrayList<>();
    private static final String DirectoryPath = "src/main/resources/Logs/"+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd _ HH-mm-ss"));
    private static final String CustomerLog = DirectoryPath + File.separator +"/customer.log";
    private static final String EventLog = DirectoryPath + File.separator +"/event.log";
    private static final String VendorLog =DirectoryPath + File.separator +"/vendor.log";
    private static final String simulationLog = DirectoryPath + File.separator +"/simulation.log";
    private static final Logger logger = Logger.getLogger(Util.class.getName());
    private static int startOption;

    static {
        try {
            File directory = new File(DirectoryPath);
            if (directory.mkdir()) {
                System.out.println("Directory created successfully.");
            } else {
                System.out.println("Failed to create directory.");

            }
            FileHandler fileHandler = new FileHandler(simulationLog, true); // "true" to append to the file
            fileHandler.setFormatter(new SimpleFormatter());  // Sets a simple text format for logs
            logger.addHandler(fileHandler);
            logger.setUseParentHandlers(false); // Disables logging to console
        } catch (IOException e) {
            logger.warning("Failed to set up file handler for logger: " + e.getMessage());
        }catch(InvalidPathException e){
            logger.warning("Failed to set up file handler for logger : " + e.getMessage());
        }
    }
    @Autowired
    private CustomerService customerService;

    public ArrayList<Customer> getCustomers() {
        return customers;
    }

    public ArrayList<Vendor> getVendors() {
        return vendors;
    }

    public  ArrayList<Event> getEvents() {
        return events;
    }

    public String getCustomerLog() {
        return CustomerLog;
    }

    public String getEventLog() {
        return EventLog;
    }

    public String getVendorLog() {
        return VendorLog;
    }

    public int getStartOption() {
        return startOption;
    }



    public String generateRandomString(String stringRequired) throws IOException {
        String fNameFile = "src/main/java/entity/Static/fName.txt";
        String lNameFIle = "src/main/java/entity/Static/lName.txt";
        String passwordFile = "src/main/java/entity/Static/passwords.txt";
        String email = "src/main/java/entity/Static/email.txt";
        String username = "src/main/java/entity/Static/username.txt";
        Random random = new Random();
        String filePath;
        int size;
        if(stringRequired.equalsIgnoreCase("fname")){
            filePath = fNameFile;
            size = 4945;
        } else if (stringRequired.equalsIgnoreCase("lname")) {
            filePath = lNameFIle;
            size = 21985;
        } else if(stringRequired.equalsIgnoreCase("password")){
            filePath = passwordFile;
            size = 10000;
        }else if(stringRequired.equalsIgnoreCase("email")){
            filePath = email;
            size = 8844;
        }else{
            filePath = username;
            size = 81475;
        }
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        int fNameValue = random.nextInt(1, size);
        int currentLine = 0;
        while (reader.readLine() != null) {
            currentLine++;
            if (currentLine == fNameValue) {
                return reader.readLine();
            }
        }
        return null;
    }

    public int generateRandomInt(int start, int end){
        if(start == end){
            return end;
        }
        Random random = new Random();
        return random.nextInt(start, end);
    }

    public void generateSimulatedUsers(){
        try {
            System.out.println("Welcome to the ticket vendor simulation CLI");
            System.out.println("Make sure you have altered the config file to your liking before starting");
            System.out.println("Please select one of the following");
            System.out.println("1. Simulation");
            System.out.println("2. Thread Testing");
            System.out.println("3. Exit");
            startOption = validateUserInput("Option", 1 , 3);

            if(startOption == 1){
                generateForSimulation(true);
            }else if(startOption == 2){
                generateForThreadTesting(true);

            }else if(startOption == 3){
                System.out.println("Exiting Program");
                System.exit(1);
            }

        } catch (Exception e) {
            System.out.println("fuck");
            System.out.println(e);
        }
    }

    public void generateForSimulation(boolean isStartUp) throws IOException {
        int EventCountMin = readJsonFile("Simulation", "event", "EventCountMin");
        int EventCountMax = readJsonFile("Simulation", "event", "EventCountMax");
        int PoolSizeMin = readJsonFile("Simulation", "event", "PoolSizeMin");
        int PoolSizeMax = readJsonFile("Simulation", "event", "PoolSizeMax");
        int TotalEventTicketsMin = readJsonFile("Simulation", "event", "TotalEventTicketsMin");
        int TotalEventTicketsMax = readJsonFile("Simulation", "event", "TotalEventTicketsMax");

        int CustomerCountMin = readJsonFile("Simulation", "customer", "CustomerCountMin");
        int CustomerCountMax = readJsonFile("Simulation", "customer", "CustomerCountMax");
        int RetrievalRateMin = readJsonFile("Simulation", "customer", "RetrievalRateMin");
        int RetrievalRateMax = readJsonFile("Simulation", "customer", "RetrievalRateMax");
        int CustomerFrequencyMin = readJsonFile("Simulation", "customer", "FrequencyMin");
        int CustomerFrequencyMax = readJsonFile("Simulation", "customer", "FrequencyMax");

        int VendorCountMin = readJsonFile("Simulation", "vendor", "VendorCountMin");
        int VendorCountMax = readJsonFile("Simulation", "vendor", "VendorCountMax");
        int ReleaseRateMin = readJsonFile("Simulation", "vendor", "ReleaseRateMin");
        int ReleaseRateMax = readJsonFile("Simulation", "vendor", "ReleaseRateMax");
        int VendorFrequencyMin = readJsonFile("Simulation", "vendor", "FrequencyMin");
        int VendorFrequencyMax = readJsonFile("Simulation", "vendor", "FrequencyMax");

        int simulatedVendors = generateRandomInt(VendorCountMin, VendorCountMax);
        int simulatedCustomers = generateRandomInt(CustomerCountMin, CustomerCountMax);
        int simulatedEvents = generateRandomInt(EventCountMin, EventCountMax);

        if(!isStartUp){
            simulatedEvents = 1;
        }else{
            System.out.println(simulatedCustomers);
            simulateCustomersForSimulationTesting(simulatedCustomers, RetrievalRateMin, RetrievalRateMax, CustomerFrequencyMin, CustomerFrequencyMax);
            simulateVendorsForThreadTesting(simulatedVendors);
        }
        simulateEventsForSimulationTesting(simulatedEvents, PoolSizeMin, PoolSizeMax,TotalEventTicketsMin, TotalEventTicketsMax, ReleaseRateMin, ReleaseRateMax, VendorFrequencyMin, VendorFrequencyMax);
    }

    public void generateForThreadTesting(boolean isStartUp) throws IOException {
        int simulatedEvents = readJsonFile("ThreadTesting", "event", "EventCount");
        int PoolSize = readJsonFile("ThreadTesting", "event", "PoolSize");
        int TotalEventTickets = readJsonFile("ThreadTesting", "event", "TotalTicketCount");

        int simulatedCustomers = readJsonFile("ThreadTesting", "customer", "CustomerCount");
        int RetrievalRate = readJsonFile("ThreadTesting", "customer", "RetrievalRate");
        int CustomerFrequency = readJsonFile("ThreadTesting", "customer", "Frequency");

        int simulatedVendors = readJsonFile("ThreadTesting", "vendor", "VendorCount");
        int ReleaseRate = readJsonFile("ThreadTesting", "vendor", "ReleaseRate");
        int VendorFrequency = readJsonFile("ThreadTesting", "vendor", "Frequency");

        if(!isStartUp){
            simulatedEvents = 1;
        }else{
            simulateCustomersForThreadTesting(simulatedCustomers, CustomerFrequency, RetrievalRate);
            simulateVendorsForThreadTesting(simulatedVendors);
        }
        simulateEventsForThreadTesting(simulatedEvents, PoolSize, TotalEventTickets, ReleaseRate, VendorFrequency);
    }

    public int validateUserInput(String prompt, int min, int max){
        Scanner scanner = new Scanner(System.in);
        int option;

        System.out.printf("%s: ", prompt);

        while (true) {
            try {
                option = scanner.nextInt();

                if (option >= min && option <= max) {
                    break;
                } else {
                    System.out.printf("Invalid input. Please enter a number between %d and %d: ", min, max);
                }
            } catch (InputMismatchException e) {
                System.out.print("Invalid input. Please enter a valid integer: ");
                scanner.nextLine();
            }
        }
        return option;
    }

    public void simulateCustomersForThreadTesting(int simulateCustomers, int customerFrequency, int customerRetrieve) throws IOException {
        for(int i = 0; i < simulateCustomers; i++){
            Customer customer = new Customer(generateRandomString("fname"), generateRandomString("lname"), generateRandomString("username"), generateRandomString("password"), generateRandomString("email"), true, customerRetrieve, customerFrequency);
            customers.add(customer);
        }
    }

    public void simulateCustomersForSimulationTesting(int simulateCustomers, int customerRetrieveMin, int customerRetrieveMax, int customerFrequencyMin, int customerFrequencyMax) throws IOException {
        for(int i = 0; i < simulateCustomers; i++){
            Customer customer = new Customer(generateRandomString("fname"), generateRandomString("lname"), generateRandomString("username"), generateRandomString("password"), generateRandomString("email"), true, generateRandomInt(customerRetrieveMin, customerRetrieveMax), generateRandomInt(customerFrequencyMin, customerFrequencyMax));
            customers.add(customer);
        }
    }

    public void simulateVendorsForThreadTesting(int simulateVendors) throws IOException {
        for(int i = 0; i < simulateVendors; i++){
            Vendor vendor = new Vendor(generateRandomString("fname"), generateRandomString("lname"), generateRandomString("username"), generateRandomString("password"), generateRandomString("email"), true);
            vendors.add(vendor);
        }
    }

    public void simulateEventsForThreadTesting(int simulatedEvent, int poolSize, int totalEventTickets, int releaseRate, int frequency){
        List<Event> eventsToSave = new ArrayList<>();
        List<VendorEventAssociation> associationsToSave = new ArrayList<>();
        for(int i = 0; i < simulatedEvent; i++){
            boolean flag = true;
            Event event = new Event(poolSize, totalEventTickets);
            eventsToSave.add(event);
            int vendorCount = generateRandomInt(-5,vendors.size());
            ArrayList<Integer> addedVendors = new ArrayList<>();
            if(vendorCount <= 0){
                vendorCount = 1;
            }
            for(int j = 0; j < vendorCount; j++){
                int vendorPosition;
                do{
                    vendorPosition = generateRandomInt(0,vendors.size());
                }while(addedVendors.contains(vendorPosition));

                if(flag){
                    event.setVendor(vendors.get(vendorPosition));
                }
                VendorEventAssociation vendorEventAssociation = new VendorEventAssociation(vendors.get(vendorPosition), event, releaseRate, frequency);
                associationsToSave.add(vendorEventAssociation);
                Thread eventThread = new Thread(vendorEventAssociation);
                eventThread.start();
                vendors.get(vendorPosition).setEvents(event);
                addedVendors.add(vendorPosition);
                flag = false;
            }
            events.add(event);
        }
    }

    public void simulateEventsForSimulationTesting(int simulatedEvent, int poolSizeMin, int poolSizeMax, int totalEventTicketsMin, int totalEventTicketsMax, int releaseRateMin, int releaseRateMax, int frequencyMin, int frequencyMax){
        for(int i = 0; i < simulatedEvent; i++){
            boolean flag = true;
            Event event = new Event(generateRandomInt(poolSizeMin, poolSizeMax), generateRandomInt(totalEventTicketsMin, totalEventTicketsMax));
            int vendorCount = generateRandomInt(-5,vendors.size());
            ArrayList<Integer> addedVendors = new ArrayList<>();
            if(vendorCount <= 0){
                vendorCount = 1;
            }
            for(int j = 0; j < vendorCount; j++){
                int vendorPosition;
                do{
                    vendorPosition = generateRandomInt(0,vendors.size());
                }while(addedVendors.contains(vendorPosition));

                if(flag){
                    event.setVendor(vendors.get(vendorPosition));
                }
                VendorEventAssociation vendorEventAssociation = new VendorEventAssociation(vendors.get(vendorPosition), event, generateRandomInt(releaseRateMin, releaseRateMax), generateRandomInt(frequencyMin, frequencyMax));
                vendors.get(vendorPosition).setEvents(event);
                addedVendors.add(vendorPosition);
                Thread eventThread = new Thread(vendorEventAssociation);
                eventThread.start();
                flag = false;
            }
            events.add(event);
        }
    }

    public int readJsonFile(String type, String option ,String config) {
        try (FileReader reader = new FileReader("src/main/java/entity/Config/config.json")) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            return root.getAsJsonObject(type).getAsJsonObject(option).get(config).getAsInt();
        } catch (IOException e) {
            return 0;
        }
    }

    public void validateConfig() {
        ArrayList<String> errors = new ArrayList<>();
        try{
            // Validate Simulation event configuration
            validateMinMax("Simulation", "event", "EventCountMin", "EventCountMax", errors);
            validateMinMax("Simulation", "event", "PoolSizeMin", "PoolSizeMax", errors);
            validateMinMax("Simulation", "event", "TotalEventTicketsMin", "TotalEventTicketsMax", errors);
            validateMinMax("Simulation", "event", "EventCreationFrequencyMin", "EventCreationFrequencyMax", errors);
            validateMinMax("Simulation", "event", "PoolSizeMax", "TotalEventTicketsMin", errors);
            validateSpecificValue("Simulation", "event", "EventCountMin", errors);
            validateSpecificValue("Simulation", "event", "PoolSizeMin", errors);
            validateSpecificValue("Simulation", "event", "TotalEventTicketsMin", errors);
            validateSpecificValue("Simulation", "event", "EventCreationFrequencyMin", errors);

            // Validate Simulation customer configuration
            validateMinMax("Simulation", "customer", "CustomerCountMin", "CustomerCountMax", errors);
            validateMinMax("Simulation", "customer", "RetrievalRateMin", "RetrievalRateMax", errors);
            validateMinMax("Simulation", "customer", "FrequencyMin", "FrequencyMax", errors);
            validateSpecificValue("Simulation", "customer", "CustomerCountMin", errors);
            validateSpecificValue("Simulation", "customer", "RetrievalRateMin", errors);
            validateSpecificValue("Simulation", "customer", "FrequencyMin", errors);

            // Validate Simulation vendor configuration
            validateMinMax("Simulation", "vendor", "VendorCountMin", "VendorCountMax", errors);
            validateMinMax("Simulation", "vendor", "ReleaseRateMin", "ReleaseRateMax", errors);
            validateMinMax("Simulation", "vendor", "FrequencyMin", "FrequencyMax", errors);
            validateSpecificValue("Simulation", "vendor", "VendorCountMin", errors);
            validateSpecificValue("Simulation", "vendor", "ReleaseRateMin", errors);
            validateSpecificValue("Simulation", "vendor", "FrequencyMin", errors);
            validateSpecificConditions("Simulation", "vendor", "ReleaseRateMin", "event", "PoolSizeMax", errors);

            // Validate ThreadTesting event configuration
            validateSpecificValue("ThreadTesting", "event", "EventCount", errors);
            validateSpecificValue("ThreadTesting", "event", "PoolSize", errors);
            validateSpecificValue("ThreadTesting", "event", "TotalTicketCount", errors);
            validateSpecificValue("ThreadTesting", "event", "EventCreationFrequency", errors);
            validateMinMax("ThreadTesting", "event", "PoolSize", "TotalTicketCount", errors);

            // Validate ThreadTesting customer configuration
            validateSpecificValue("ThreadTesting", "customer", "CustomerCount", errors);
            validateSpecificValue("ThreadTesting", "customer", "RetrievalRate", errors);
            validateSpecificValue("ThreadTesting", "customer", "Frequency", errors);

            // Validate ThreadTesting vendor configuration
            validateSpecificValue("ThreadTesting", "vendor", "VendorCount", errors);
            validateSpecificValue("ThreadTesting", "vendor", "ReleaseRate", errors);
            validateSpecificValue("ThreadTesting", "vendor", "Frequency", errors);
            validateSpecificConditions("ThreadTesting", "vendor", "ReleaseRate", "event", "PoolSize", errors);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Invalid config file");
            System.exit(0);
        }


        // Print errors
        if (errors.isEmpty()) {
            System.out.println("Configuration is valid.");
        } else {
            System.out.println("Configuration Errors:");
            errors.forEach(System.out::println);
            System.exit(1);
        }
    }

    // Helper method to validate min/max ranges
    private void validateMinMax(String category, String section, String minKey, String maxKey, ArrayList<String> errors) {
        int min = readJsonFile(category, section, minKey);
        int max = readJsonFile(category, section, maxKey);

        if (min > max) {
            errors.add(String.format("Error in %s -> %s: %s (%d) is greater than %s (%d)",
                    category, section, minKey, min, maxKey, max));
        }
    }

    // Helper method to validate specific values
    private void validateSpecificValue(String category, String section, String key, ArrayList<String> errors) {
        int value = readJsonFile(category, section, key);

        // Add specific checks here if needed (e.g., ensuring values are positive)
        if (value < 0) {
            errors.add(String.format("Error in %s -> %s: %s (%d) must be positive",
                    category, section, key, value));
        }
    }

    private void validateSpecificConditions(String category, String sectionOne, String lowerKey,  String sectionTw0, String higherKey, ArrayList<String> errors) {
        int min = readJsonFile(category, sectionOne, lowerKey);
        int max = readJsonFile(category, sectionTw0, higherKey);

        if (min > max) {
            errors.add(String.format("Error in %s -> %s and %s: %s (%d) is greater than %s (%d)",
                    category, sectionOne, sectionTw0, lowerKey, min, higherKey, max));
        }
    }

    public void endProgram(){
        while(true){
            System.out.println("1. Exit Program");
            System.out.println("2. To View All Events");
            System.out.println("3. To View All Vendors");
            System.out.println("4. To View All Customers");
            int option = validateUserInput("option", 1, 4);

            if(option == 1){
                for(Customer customer : customers){
                    logger.info(customer.toString());
                }
                logger.info("==================================================");
                for(Vendor vendor : vendors){
                    logger.info(vendor.toString());
                }
                logger.info("==================================================");
                for(Event event : events) {
                    logger.info(event.toString());
                }
                System.exit(1);
            } else if (option == 2) {
                for(Event event : events){
                    System.out.println(event.toString());
                }
            }else if(option == 3){
                for(Vendor vendor: vendors){
                    System.out.println(vendor.toString());
                }
            } else if (option == 4) {
                for(Customer customer : customers){
                    System.out.println(customer.toString());
                }
            }
        }
    }
}
