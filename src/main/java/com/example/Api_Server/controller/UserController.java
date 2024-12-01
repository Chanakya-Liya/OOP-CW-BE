package com.example.Api_Server.controller;

import com.example.Api_Server.DTO.UserDTO;
import com.example.Api_Server.entity.Vendor;
import com.example.Api_Server.service.CustomerService;
import com.example.Api_Server.service.VendorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private CustomerService customerService;
    @Autowired
    private VendorService vendorService;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UserDTO userDTO) {
        try{
            String email = userDTO.getEmail();
            String password = userDTO.getPassword();

            if(customerService.checkCustomer(email, password)) {
                return new ResponseEntity<>("Customer", org.springframework.http.HttpStatus.OK);
            } else if(vendorService.checkVendor(email, password)) {
                return new ResponseEntity<>("Vendor", org.springframework.http.HttpStatus.OK);
            }else{
                return new ResponseEntity<>("Invalid", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Invalid", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
}
