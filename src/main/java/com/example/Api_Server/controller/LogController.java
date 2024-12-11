package com.example.Api_Server.controller;

import CLI.LoggingConfig;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/logs")
public class LogController {

    @Autowired
    private LoggingConfig loggingConfig;

    // Stream logs from the specified log file to the frontend
    @GetMapping("/{type}")
    public void streamLogs(@PathVariable String type, HttpServletResponse response) {
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");

        String logFilePath = switch (type.toLowerCase()) {
            case "customer" -> loggingConfig.getDirectoryPath() + "/customer.log";
            case "event" -> loggingConfig.getDirectoryPath() + "/event.log";
            case "vendor" -> loggingConfig.getDirectoryPath() + "/vendor.log";
            default -> null;
        };

        if (logFilePath == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try (PrintWriter writer = response.getWriter()) {
                writer.write("Invalid log type selected.");
                writer.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(logFilePath));
             PrintWriter writer = response.getWriter()) {

            String line;
            while ((line = reader.readLine()) != null) {
                writer.write("data: " + line + "\n\n");
                writer.flush();
                Thread.sleep(500); // Simulate streaming delay
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
