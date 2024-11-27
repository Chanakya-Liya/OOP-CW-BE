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
    private GenerateSimulation generateForSimulation;
    @Autowired
    private VendorEventAssociationService vendorEventAssociationService;
    @Autowired
    private TicketService ticketService;
    @Autowired
    private VendorEventAssociationRepository vendorEventAssociationRepository;
    @Autowired
    private VendorService vendorService;


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


    public void generateSimulatedUsers(){
        try {
            System.out.println("Welcome to the ticket vendor simulation CLI");
            System.out.println("Make sure you have altered the config file to your liking before starting");
            System.out.println("Please select one of the following");
            System.out.println("1. Simulation");
            System.out.println("2. Thread Testing");
            System.out.println("3. Exit");
            startOption = validateUserInput("Option", 1 , 3);
            System.out.println("Loading Simulation");
            if(startOption == 1){
                generateForSimulation.generateForSimulation(true);
            }else if(startOption == 2){
                generateForSimulation.generateForThreadTesting(true);
            }else if(startOption == 3){
                System.out.println("Exiting Program");
                System.exit(1);
            }
            System.out.println("Simulation loaded successfully");
            customerService.init();
            vendorService.init();
            vendorEventAssociationService.init();
        } catch (Exception e) {
            logger.severe("Error generating simulated users: " + e.getMessage());  // Use severe for critical errors
            System.err.println("An error occurred. Please check the logs for details.");  // User-friendly message
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
