package CLI;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

@Component
public class ConfigManager {
    private final String configFilePath = "src/main/resources/config/config.json";

    public int getIntValue(String category, String section, String key) {
        try (FileReader reader = new FileReader(configFilePath)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            return root.getAsJsonObject(category).getAsJsonObject(section).get(key).getAsInt();
        } catch (IOException | NullPointerException e) {  // Handle potential NullPointerException
            System.err.println("Error reading config value: " + e.getMessage());
            return 0; // Or throw an exception
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
            validateMinValue("Simulation", "event", "EventCountMin", errors);
            validateMinValue("Simulation", "event", "PoolSizeMin", errors);
            validateMinValue("Simulation", "event", "TotalEventTicketsMin", errors);
            validateMinValue("Simulation", "event", "EventCreationFrequencyMin", errors);
            validateMaxValue("event", "EventCountMax", 30, errors);
            validateMaxValue("event", "PoolSizeMax", 700, errors);
            validateMaxValue("event", "TotalEventTicketsMax", 5000, errors);


            // Validate Simulation customer configuration
            validateMinMax("Simulation", "customer", "CustomerCountMin", "CustomerCountMax", errors);
            validateMinMax("Simulation", "customer", "RetrievalRateMin", "RetrievalRateMax", errors);
            validateMinMax("Simulation", "customer", "FrequencyMin", "FrequencyMax", errors);
            validateMinValue("Simulation", "customer", "CustomerCountMin", errors);
            validateMinValue("Simulation", "customer", "RetrievalRateMin", errors);
            validateMinValue("Simulation", "customer", "FrequencyMin", errors);
            validateMaxValue("customer", "CustomerCountMax", 500, errors);
            validateMaxValue("customer", "RetrievalRateMax", 50, errors);


            // Validate Simulation vendor configuration
            validateMinMax("Simulation", "vendor", "VendorCountMin", "VendorCountMax", errors);
            validateMinMax("Simulation", "vendor", "ReleaseRateMin", "ReleaseRateMax", errors);
            validateMinMax("Simulation", "vendor", "FrequencyMin", "FrequencyMax", errors);
            validateMinValue("Simulation", "vendor", "VendorCountMin", errors);
            validateMinValue("Simulation", "vendor", "ReleaseRateMin", errors);
            validateMinValue("Simulation", "vendor", "FrequencyMin", errors);
            validateSpecificConditions("Simulation", "ReleaseRateMin", "PoolSizeMax", errors);
            validateMaxValue("vendor", "VendorCountMax", 60, errors);
            validateMaxValue("vendor", "ReleaseRateMax", 250, errors);

            // Validate ThreadTesting event configuration
            validateMinValue("ThreadTesting", "event", "EventCount", errors);
            validateMinValue("ThreadTesting", "event", "PoolSize", errors);
            validateMinValue("ThreadTesting", "event", "TotalTicketCount", errors);
            validateMinValue("ThreadTesting", "event", "EventCreationFrequency", errors);
            validateMinMax("ThreadTesting", "event", "PoolSize", "TotalTicketCount", errors);

            // Validate ThreadTesting customer configuration
            validateMinValue("ThreadTesting", "customer", "CustomerCount", errors);
            validateMinValue("ThreadTesting", "customer", "RetrievalRate", errors);
            validateMinValue("ThreadTesting", "customer", "Frequency", errors);

            // Validate ThreadTesting vendor configuration
            validateMinValue("ThreadTesting", "vendor", "VendorCount", errors);
            validateMinValue("ThreadTesting", "vendor", "ReleaseRate", errors);
            validateMinValue("ThreadTesting", "vendor", "Frequency", errors);
            validateSpecificConditions("ThreadTesting", "ReleaseRate", "PoolSize", errors);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Invalid config file");
            System.exit(0);
        }
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
        int min = getIntValue(category, section, minKey);
        int max = getIntValue(category, section, maxKey);

        if (min > max) {
            errors.add(String.format("Error in %s -> %s: %s (%d) is greater than %s (%d)",
                    category, section, minKey, min, maxKey, max));
        }
    }

    // Helper method to validate specific values
    private void validateMinValue(String category, String section, String key, ArrayList<String> errors) {
        int value = getIntValue(category, section, key);

        //ensuring values are positive
        if (value < 0) {
            errors.add(String.format("Error in %s -> %s: %s (%d) must be positive",
                    category, section, key, value));
        }
    }

    // Helper method to validate max values
    private void validateMaxValue(String section, String key, int maxValue, ArrayList<String> errors) {
        int value = getIntValue("Simulation", section, key);
        if (value > maxValue) {
            errors.add(String.format("Error in %s -> %s: %s (%d) must be less than %d",
                    "Simulation", section, key, value, maxValue));
        }
    }

    // Helper method to validate specific conditions
    private void validateSpecificConditions(String category, String lowerKey, String higherKey, ArrayList<String> errors) {
        int min = getIntValue(category, "vendor", lowerKey);
        int max = getIntValue(category, "event", higherKey);

        if (min > max) {
            errors.add(String.format("Error in %s -> %s and %s: %s (%d) is greater than %s (%d)",
                    category, "vendor", "event", lowerKey, min, higherKey, max));
        }
    }
}
