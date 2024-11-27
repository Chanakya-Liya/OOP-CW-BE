package CLI;

import com.example.Api_Server.entity.Customer;
import com.example.Api_Server.entity.Vendor;
import com.example.Api_Server.repository.VendorRepository;
import com.example.Api_Server.service.CustomerService;
import com.example.Api_Server.service.VendorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class GenerateSimulation {

    @Autowired
    private final DataGenerator dataGenerator;
    @Autowired
    private final ConfigManager configManager;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    public GenerateSimulation(DataGenerator dataGenerator, ConfigManager configManager) {
        this.dataGenerator = dataGenerator;
        this.configManager = configManager;
    }


    public void generateForSimulation(boolean startUp) throws IOException {
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
            if(!startUp){
                simulatedEvents = 1;
            }else{
                List<Customer> customers = dataGenerator.simulateCustomers(simulatedCustomers, RetrievalRateMin, RetrievalRateMax, CustomerFrequencyMin, CustomerFrequencyMax, customerService);
                customerService.saveAllCustomers(customers);
                List<Vendor> vendors = dataGenerator.simulateVendors(simulatedVendors, EventCreationFrequencyMin, EventCreationFrequencyMax);
                vendorRepository.saveAll(vendors);
            }
            dataGenerator.simulateEventsForSimulationTesting(simulatedEvents, PoolSizeMin, PoolSizeMax,TotalEventTicketsMin, TotalEventTicketsMax, ReleaseRateMin, ReleaseRateMax, VendorFrequencyMin, VendorFrequencyMax, vendorRepository.findAll());
        }catch (IOException e){
            throw new IOException();
        }
    }

    public void generateForThreadTesting(boolean startUp) throws IOException {
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
            if(!startUp){
                simulatedEvents = 1;
            }else{
                List<Customer> customers = dataGenerator.simulateCustomers(simulatedCustomers, CustomerFrequency, RetrievalRate, customerService);
                customerService.saveAllCustomers(customers);
                List<Vendor> vendors =  dataGenerator.simulateVendors(simulatedVendors, EventCreationFrequency);
                vendorRepository.saveAll(vendors);
            }
            dataGenerator.simulateEventsForThreadTesting(simulatedEvents, PoolSize, TotalEventTickets, ReleaseRate, VendorFrequency, vendorRepository.findAll());
        }catch (IOException e){
            throw new IOException();
        }

    }
}
