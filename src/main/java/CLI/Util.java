package CLI;
import com.example.Api_Server.entity.Customer;
import com.example.Api_Server.entity.Event;
import com.example.Api_Server.entity.Vendor;
import com.example.Api_Server.repository.VendorRepository;
import com.example.Api_Server.service.CustomerService;
import com.example.Api_Server.service.VendorService;
import com.example.Api_Server.service.EventService;
import com.example.Api_Server.service.VendorEventAssociationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


@Component
public class Util {
    private static int startOption;

    @Autowired
    private final DataGenerator dataGenerator;
    @Autowired
    private final ConfigManager configManager;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private VendorRepository vendorRepository;
    @Autowired
    private EventService eventService;
    @Autowired
    private VendorEventAssociationService vendorEventAssociationService;


    @Autowired
    public Util(DataGenerator dataGenerator, ConfigManager configManager) {
        this.dataGenerator = dataGenerator;
        this.configManager = configManager;
    }

    @Autowired
    private LoggingConfig loggingConfig;
    private static final Logger logger;
    static {
        logger = Logger.getLogger(Util.class.getName());

        try {
            FileHandler fileHandler = new FileHandler(new LoggingConfig().getSimulationLog(), true); // "true" to append to the file
            fileHandler.setFormatter(new SimpleFormatter());  // Sets a simple text format for logs
            logger.addHandler(fileHandler);
            logger.setUseParentHandlers(false); // Disables logging to console
        } catch (IOException e) {
            logger.warning("Failed to set up file handler for logger: " + e.getMessage());
        }catch(InvalidPathException e) {
            logger.warning("Failed to set up file handler for logger: " + e.getMessage());
        }
    }


    public static int getStartOption() {
        return startOption;
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
                generateForSimulation();
            }else if(startOption == 2){
                generateForThreadTesting();

            }else if(startOption == 3){
                System.out.println("Exiting Program");
                System.exit(1);
            }

        } catch (Exception e) {
            logger.severe("Error generating simulated users: " + e.getMessage());  // Use severe for critical errors
            System.err.println("An error occurred. Please check the logs for details.");  // User-friendly message
        }
    }

    public void generateForSimulation() throws IOException {
        int EventCountMin = configManager.getIntValue("Simulation", "event", "EventCountMin");
        int EventCountMax = configManager.getIntValue("Simulation", "event", "EventCountMax");
        int PoolSizeMin = configManager.getIntValue("Simulation", "event", "PoolSizeMin");
        int PoolSizeMax = configManager.getIntValue("Simulation", "event", "PoolSizeMax");
        int TotalEventTicketsMin = configManager.getIntValue("Simulation", "event", "TotalEventTicketsMin");
        int TotalEventTicketsMax = configManager.getIntValue("Simulation", "event", "TotalEventTicketsMax");
        int EventCreationFrequencyMin = configManager.getIntValue("Simulation", "event", "EventCreationFrequencyMin");
        int EventCreationFrequencyMax = configManager.getIntValue("Simulation", "event", "EventCreationFrequencyMax");

        int CustomerCountMin = configManager.getIntValue("Simulation", "customer", "CustomerCountMin");
        int CustomerCountMax = configManager.getIntValue("Simulation", "customer", "CustomerCountMax");
        int RetrievalRateMin = configManager.getIntValue("Simulation", "customer", "RetrievalRateMin");
        int RetrievalRateMax = configManager.getIntValue("Simulation", "customer", "RetrievalRateMax");
        int CustomerFrequencyMin = configManager.getIntValue("Simulation", "customer", "FrequencyMin");
        int CustomerFrequencyMax = configManager.getIntValue("Simulation", "customer", "FrequencyMax");

        int VendorCountMin = configManager.getIntValue("Simulation", "vendor", "VendorCountMin");
        int VendorCountMax = configManager.getIntValue("Simulation", "vendor", "VendorCountMax");
        int ReleaseRateMin = configManager.getIntValue("Simulation", "vendor", "ReleaseRateMin");
        int ReleaseRateMax = configManager.getIntValue("Simulation", "vendor", "ReleaseRateMax");
        int VendorFrequencyMin = configManager.getIntValue("Simulation", "vendor", "FrequencyMin");
        int VendorFrequencyMax = configManager.getIntValue("Simulation", "vendor", "FrequencyMax");

        int simulatedVendors = dataGenerator.generateRandomInt(VendorCountMin, VendorCountMax);
        int simulatedCustomers = dataGenerator.generateRandomInt(CustomerCountMin, CustomerCountMax);
        int simulatedEvents = dataGenerator.generateRandomInt(EventCountMin, EventCountMax);

        try{
            List<Customer> customers = dataGenerator.simulateCustomers(simulatedCustomers, RetrievalRateMin, RetrievalRateMax, CustomerFrequencyMin, CustomerFrequencyMax);
            for(Customer customer : customers){
                customerService.addCustomer(customer);
            }
            List<Vendor> vendors = dataGenerator.simulateVendors(simulatedVendors, EventCreationFrequencyMin, EventCreationFrequencyMax);
            vendorRepository.saveAll(vendors);
            dataGenerator.simulateEventsForSimulationTesting(simulatedEvents, PoolSizeMin, PoolSizeMax,TotalEventTicketsMin, TotalEventTicketsMax, ReleaseRateMin, ReleaseRateMax, VendorFrequencyMin, VendorFrequencyMax, vendors);
        }catch (IOException e){
            throw new IOException();
        }
    }

    public void generateForThreadTesting() throws IOException {
        int simulatedEvents = configManager.getIntValue("ThreadTesting", "event", "EventCount");
        int PoolSize = configManager.getIntValue("ThreadTesting", "event", "PoolSize");
        int TotalEventTickets = configManager.getIntValue("ThreadTesting", "event", "TotalTicketCount");
        int EventCreationFrequency = configManager.getIntValue("ThreadTesting", "event", "EventCreationFrequency");

        int simulatedCustomers = configManager.getIntValue("ThreadTesting", "customer", "CustomerCount");
        int RetrievalRate = configManager.getIntValue("ThreadTesting", "customer", "RetrievalRate");
        int CustomerFrequency = configManager.getIntValue("ThreadTesting", "customer", "Frequency");

        int simulatedVendors = configManager.getIntValue("ThreadTesting", "vendor", "VendorCount");
        int ReleaseRate = configManager.getIntValue("ThreadTesting", "vendor", "ReleaseRate");
        int VendorFrequency = configManager.getIntValue("ThreadTesting", "vendor", "Frequency");
        try{
            List<Customer> customers = dataGenerator.simulateCustomers(simulatedCustomers, CustomerFrequency, RetrievalRate);
            for(Customer customer: customers){
                customerService.addCustomer(customer);
            }
            List<Vendor> vendors =  dataGenerator.simulateVendors(simulatedVendors, EventCreationFrequency);
            vendorRepository.saveAll(vendors);
            dataGenerator.simulateEventsForThreadTesting(simulatedEvents, PoolSize, TotalEventTickets, ReleaseRate, VendorFrequency, vendors);
        }catch (IOException e){
            throw new IOException();
        }

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

    public void endProgram(){
        while(true){
            System.out.println("1. Exit Program");
            System.out.println("2. To View All Events");
            System.out.println("3. To View All Vendors");
            System.out.println("4. To View All Customers");
            int option = validateUserInput("option", 1, 4);

            if(option == 1){
                for(Customer customer : customerService.getAll()){
                    logger.info(customer.toString());
                }
                logger.info("==================================================");
                for(Vendor vendor : vendorRepository.findAll()){
                    logger.info(vendor.toString());
                }
                logger.info("==================================================");
                for(Event event : eventService.getAll()) {
                    logger.info(event.toString());
                }
                System.exit(1);
            } else if (option == 2) {
                for(Event event : eventService.getAll()){
                    System.out.println(event.toString());
                }
            }else if(option == 3){
                for(Vendor vendor: vendorRepository.findAll()){
                    System.out.println(vendor.toString());
                }
            } else if (option == 4) {
                for(Customer customer : customerService.getAll()){
                    System.out.println(customer.toString());
                }
            }
        }
    }
}
