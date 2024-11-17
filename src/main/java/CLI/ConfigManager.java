package CLI;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

@Component
public class ConfigManager {
    private final String configFilePath = "src/main/java/CLI/Config/config.json";

    public int getIntValue(String category, String section, String key) {
        try (FileReader reader = new FileReader(configFilePath)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            return root.getAsJsonObject(category).getAsJsonObject(section).get(key).getAsInt();
        } catch (IOException | NullPointerException e) {  // Handle potential NullPointerException
            // Handle error appropriately – log, throw exception, or return a default value
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
        int min = getIntValue(category, section, minKey);
        int max = getIntValue(category, section, maxKey);

        if (min > max) {
            errors.add(String.format("Error in %s -> %s: %s (%d) is greater than %s (%d)",
                    category, section, minKey, min, maxKey, max));
        }
    }

    // Helper method to validate specific values
    private void validateSpecificValue(String category, String section, String key, ArrayList<String> errors) {
        int value = getIntValue(category, section, key);

        // Add specific checks here if needed (e.g., ensuring values are positive)
        if (value < 0) {
            errors.add(String.format("Error in %s -> %s: %s (%d) must be positive",
                    category, section, key, value));
        }
    }

    private void validateSpecificConditions(String category, String sectionOne, String lowerKey,  String sectionTw0, String higherKey, ArrayList<String> errors) {
        int min = getIntValue(category, sectionOne, lowerKey);
        int max = getIntValue(category, sectionTw0, higherKey);

        if (min > max) {
            errors.add(String.format("Error in %s -> %s and %s: %s (%d) is greater than %s (%d)",
                    category, sectionOne, sectionTw0, lowerKey, min, higherKey, max));
        }
    }
}
