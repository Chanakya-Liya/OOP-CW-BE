package CLI;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

@Configuration
public class LoggingConfig {
    private final String DirectoryPath = "src/main/resources/Logs/"+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd _ HH-mm-ss"));
    private final String CustomerLog = DirectoryPath + File.separator +"/customer.log";
    private final String EventLog = DirectoryPath + File.separator +"/event.log";
    private final String VendorLog =DirectoryPath + File.separator +"/vendor.log";
    private final String simulationLog = DirectoryPath + File.separator +"/simulation.log";

    @PostConstruct
    public void setupLoggers() {
        try {
            createDirectory(DirectoryPath);
            setupLogger(Util.class, simulationLog);

        } catch (IOException e) {
            // ... handle exceptions
        }
    }

    public void createDirectory(String directoryPath){
        File directory = new File(directoryPath);
        if (!directory.exists()) directory.mkdirs(); // Create all directories as needed
    }

    private void setupLogger(Class<?> clazz, String logFile) throws IOException {
        Logger logger = Logger.getLogger(clazz.getName());
        FileHandler fileHandler = new FileHandler(logFile, true);
        fileHandler.setFormatter(new SimpleFormatter());
        logger.addHandler(fileHandler);
        logger.setUseParentHandlers(false);
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

    public String getSimulationLog() {
        return simulationLog;
    }
}
