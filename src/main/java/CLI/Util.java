package CLI;
import com.example.Api_Server.entity.*;
import com.example.Api_Server.repository.VendorEventAssociationRepository;
import com.example.Api_Server.repository.VendorRepository;
import com.example.Api_Server.service.*;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


@Component
public class Util {
    @Getter
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
    private VendorService vendorService;
    @Autowired
    private TicketService ticketService;
    @Autowired
    private VendorEventAssociationRepository vendorEventAssociationRepository;
    @Autowired
    private AdminService adminService;


    @Autowired
    public Util(DataGenerator dataGenerator, ConfigManager configManager) {
        this.dataGenerator = dataGenerator;
        this.configManager = configManager;
    }

    private static final Logger logger;
    static {
        logger = Logger.getLogger(Util.class.getName());

        try {
            File logFile = new File(new LoggingConfig().getSimulationLog());
            if(!logFile.exists()) {
                FileHandler fileHandler = new FileHandler(new LoggingConfig().getSimulationLog(), true); // "true" to append to the file
                fileHandler.setFormatter(new SimpleFormatter());
                logger.addHandler(fileHandler);
                logger.setUseParentHandlers(false);
            }
        } catch (IOException | InvalidPathException e) {
            logger.warning("Failed to set up file handler for logger: " + e.getMessage());
        }
    }

    //generates the admin account
    public void generateAdmin(){
        Admin admin = new Admin("Super", "Admin", "admin", "admin123", "admin@gmail.com", true);
        adminService.addAdmin(admin);
    }

    //generates the users according to the option selected
    public void generateSimulatedUsers(int option){
        try {
            if(option == 1){
                generateForSimulation();
            }else if(option == 2){
                generateForThreadTesting();
            }
            System.out.println("Simulation loaded successfully");
            customerService.init();
            vendorEventAssociationService.init();
            vendorService.init();
//            eventService.printEventPhoto();
        } catch (Exception e) {
            logger.severe("Error generating simulated users: " + e.getMessage());  // Use severe for critical errors
            System.err.println("An error occurred. Please check the logs for details.");  // User-friendly message
        }
    }

    //generates the simulation data
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

        System.out.println("Simulated Events: " + simulatedEvents);
        try{
            List<Customer> customers = dataGenerator.simulateCustomers(simulatedCustomers, RetrievalRateMin, RetrievalRateMax, CustomerFrequencyMin, CustomerFrequencyMax, customerService);
            for(Customer customer : customers){
                customerService.addCustomer(customer);
            }
            List<Vendor> vendors = dataGenerator.simulateVendors(simulatedVendors, EventCreationFrequencyMin, EventCreationFrequencyMax);
            vendorRepository.saveAll(vendors);
            System.out.println("Loading simulation...");
            dataGenerator.simulateEventsForSimulationTesting(simulatedEvents, PoolSizeMin, PoolSizeMax,TotalEventTicketsMin, TotalEventTicketsMax, ReleaseRateMin, ReleaseRateMax, VendorFrequencyMin, VendorFrequencyMax, vendors);
        }catch (IOException e){
            throw new IOException();
        }
    }

    //generates the thread testing data
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
            System.out.println("Loading simulation...");
            List<Customer> customers = dataGenerator.simulateCustomers(simulatedCustomers, CustomerFrequency, RetrievalRate, customerService);
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
}
