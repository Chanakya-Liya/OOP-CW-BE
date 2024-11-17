package com.example.Api_Server;
import entity.Customer;
import entity.Util;
import entity.Vendor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import service.CustomerService;
import service.VendorService;

@SpringBootApplication
@ComponentScan(basePackages = {"repository", "service", "com.example.Api_Server"})
@EntityScan(basePackages = "entity")
@EnableJpaRepositories(basePackages = {"repository"})
public class ApiServerApplication {
	private static CustomerService customerService;
	private static VendorService vendorService;
	public ApiServerApplication(CustomerService customerService, VendorService vendorService){
		this.customerService = customerService;
		this.vendorService = vendorService;
	}

    public static void main(String[] args) {
		Util util = new Util();
		SpringApplication.run(ApiServerApplication.class, args);
		util.generateSimulatedUsers();
		for(Customer customer : util.getCustomers()){
			customerService.addCustomer(customer);
		}
		for(Vendor vendor : util.getVendors()){
			vendorService.addVendor(vendor);
		}
	}
}
