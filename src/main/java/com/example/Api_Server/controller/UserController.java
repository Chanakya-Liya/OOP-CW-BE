package com.example.Api_Server.controller;

import com.example.Api_Server.DTO.UserDTO;
import com.example.Api_Server.service.CustomerService;
import com.example.Api_Server.service.VendorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private CustomerService customerService;
    @Autowired
    private VendorService vendorService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody UserDTO userDTO) {
        try {
            String email = userDTO.getEmail();
            String password = userDTO.getPassword();

            Map<String, String> response = new HashMap<>();
            if (customerService.checkCustomer(email, password)) {
                response.put("role", "Customer");
                response.put("token", generateJwtToken(email, "Customer"));
                return ResponseEntity.ok(response);
            } else if (vendorService.checkVendor(email, password)) {
                response.put("role", "Vendor");
                response.put("token", generateJwtToken(email, "Vendor"));
                return ResponseEntity.ok(response);
            } else {
                response.put("error", "Invalid credentials");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        } catch (Exception e) {
            // Log the exception for debugging
            System.err.println("Error during login: " + e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An internal error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    private String generateJwtToken(String email, String role) {
        return Base64.getEncoder().encodeToString((role + ":" +email + ":" + System.currentTimeMillis()).getBytes());
    }
}
