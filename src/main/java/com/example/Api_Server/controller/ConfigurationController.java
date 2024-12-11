package com.example.Api_Server.controller;

import CLI.Util;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/config")
public class ConfigurationController {

    @Autowired
    private Util util;

    //loads the simulation data from the config.json file to the frontend
    @GetMapping("/simulation-data")
    public ResponseEntity<?> getSimulationData(@RequestParam(defaultValue = "Simulation") String mode) {
        // Load the JSON file from resources
        String filePath = "src/main/resources/config/config.json";
        try (FileReader reader = new FileReader(filePath)) {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> jsonData = objectMapper.readValue(reader, Map.class);
            if (jsonData.containsKey(mode)) {
                return ResponseEntity.ok(jsonData.get(mode));
            } else {
                return ResponseEntity.badRequest().body("Section not found: " + mode);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading simulation.json", e);
        }
    }

    //updates the simulation data in the config.json file
    @PutMapping("/update/{section}")
    public ResponseEntity<?> updateSimulationData(@PathVariable String section, @RequestBody Map<String, Map<String, Object>> data) {
        try {
            // Load the JSON file
            Path path = Paths.get("src/main/resources/config/config.json");
            String jsonContent = Files.readString(path);

            // Parse the JSON file
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonContent);

            // Find the section to update
            JsonNode sectionNode = rootNode.path(section);
            if (sectionNode.isMissingNode() || !sectionNode.isObject()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Section not found or invalid.");
            }

            // Update nested structures within the section
            ObjectNode sectionObjectNode = (ObjectNode) sectionNode;
            for (Map.Entry<String, Map<String, Object>> entry : data.entrySet()) {
                String subsection = entry.getKey();
                Map<String, Object> fieldsToUpdate = entry.getValue();

                // Get the subsection (e.g., "event", "customer", "vendor")
                JsonNode subsectionNode = sectionObjectNode.path(subsection);
                if (subsectionNode.isMissingNode() || !subsectionNode.isObject()) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Subsection not found: " + subsection);
                }

                // Update the fields in the subsection
                ObjectNode subsectionObjectNode = (ObjectNode) subsectionNode;
                fieldsToUpdate.forEach((key, value) -> {
                    subsectionObjectNode.set(key, objectMapper.valueToTree(value));
                });
            }

            // Write the updated JSON back to the file
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), rootNode);

            return ResponseEntity.ok("Simulation data updated successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update simulation data.");
        }
    }

    //starts the simulation based on the section
    @PostMapping("/start/{section}")
    public ResponseEntity<?> startSimulation(@PathVariable String section) {
        try {
            if (!section.equalsIgnoreCase("simulation") && !section.equalsIgnoreCase("threadtesting")) {
                return ResponseEntity.badRequest().body("Invalid section. Valid options are 'simulation' or 'threadtesting'.");
            }

            // Start simulation based on the section
            if (section.equalsIgnoreCase("simulation")) {
                util.generateSimulatedUsers(1);
            } else {
                util.generateSimulatedUsers(2);
            }

            String message = "Simulation for section '" + section + "' started successfully.";
            System.out.println(message);
            return ResponseEntity.ok(message);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to start simulation due to: " + e.getMessage());
        }
    }
}
