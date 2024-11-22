package com.example.Api_Server.entity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) throws IOException {
        String logFilePath = "src/main/java/com/example/Api_Server/entity/log_file.txt"; // Replace with your log file path
        List<String> logLines = Files.readAllLines(Paths.get(logFilePath));

        Pattern patternCustomer = Pattern.compile("ticket (\\d+) from event (\\d+)"); // Updated regex
        Map<Long, Integer> ticketCounts = new HashMap<>();
        Map<Long, Long> ticketToEvent = new HashMap<>();

        for (String line : logLines) {
            Matcher matcher = patternCustomer.matcher(line);
            if (matcher.find()) {
                Long ticketId = Long.parseLong(matcher.group(1));
                Long eventId = Long.parseLong(matcher.group(2));

                ticketCounts.put(ticketId, ticketCounts.getOrDefault(ticketId, 0) + 1); // Increment count
                ticketToEvent.put(ticketId, eventId); // Map ticket to event
            }
        }

        System.out.println("Oversold Tickets:");
        int i = 1;
        for (Map.Entry<Long, Integer> entry : ticketCounts.entrySet()) {
            if (entry.getValue() > 1) { // If ticket sold more than once
                System.out.println(i + ". Ticket ID: " + entry.getKey() + ", Event ID: " + ticketToEvent.get(entry.getKey()) + ", Sold Count: " + entry.getValue());
                i = i + entry.getValue();
            }
        }


    }
}