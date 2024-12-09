package com.example.Api_Server.entity;

import jakarta.persistence.Entity;

@Entity
public class Admin extends  User{

    public Admin(String fName, String lName, String username, String password, String email, boolean simulated) {
        super(fName, lName, username, password, email, simulated);
    }

    public Admin(){}
}
