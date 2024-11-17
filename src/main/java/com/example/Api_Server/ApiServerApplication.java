package com.example.Api_Server;


import entity.Customer;
import entity.Util;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import repository.CustomerRepository;

@SpringBootApplication
@ComponentScan(basePackages = {"repository", "service", "com.example.Api_Server"})
@EntityScan(basePackages = "entity")
@EnableJpaRepositories(basePackages = {"repository"})
public class ApiServerApplication {
	private static CustomerRepository customerRepository;

    public ApiServerApplication(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public static void main(String[] args) {

		SpringApplication.run(ApiServerApplication.class, args);
		Util.generateSimulatedUsers();
        for(Customer customer : Util.getCustomers()){
            customerRepository.save(customer);
        }
	}

}
