package com.example.Api_Server;

import CLI.ConfigManager;
import CLI.LoggingConfig;
import CLI.Util;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EntityScan(basePackages = "com.example.Api_Server.entity")
@ComponentScan(basePackages = {"com.example.Api_Server", "CLI"})
public class ApiServerApplication {

    public static void main(String[] args) {
		ConfigManager configManager = new ConfigManager();
		configManager.validateConfig();
		ConfigurableApplicationContext context = SpringApplication.run(ApiServerApplication.class, args);
		Util util = context.getBean(Util.class);
		util.generateSimulatedUsers();
		util.endProgram();
	}
}
