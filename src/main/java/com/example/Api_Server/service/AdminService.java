package com.example.Api_Server.service;

import com.example.Api_Server.entity.Admin;
import com.example.Api_Server.entity.Customer;
import com.example.Api_Server.entity.Vendor;
import com.example.Api_Server.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;

    public void addAdmin(Admin admin){
        adminRepository.save(admin);
    }

    // Check if the email and password match an admin in the database
    public boolean checkAdmin(String email, String password) {
        List<Admin> admins = adminRepository.findAll();
        for (Admin admin : admins) {
            if (admin.getEmail().equals(email) && admin.getPassword().equals(admin.hashPassword(password))) {
                return true;
            }
        }
        return false;
    }

    // Get the admin object from the email
    public Admin getAdminFromEmail(String email) {
        List<Admin> admins = adminRepository.findAll();
        for (Admin admin : admins) {
            if (admin.getEmail().equals(email)) {
                return admin;
            }
        }
        return null;
    }
}
