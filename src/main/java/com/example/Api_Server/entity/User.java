package com.example.Api_Server.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Getter
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "user_type", discriminatorType = DiscriminatorType.STRING)
abstract class User {
    private static int nextId = 1;
    @Getter
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int id;
    private String fName;
    private  String lName;
    private String username;
    private String password;
    @Setter
    private String email;
    private boolean simulated;
    private static final Logger logger = Logger.getLogger(User.class.getName());

    public User(String fName, String lName, String username, String password, String email, boolean simulated) {
        id = nextId++;
        this.fName = fName;
        this.lName = lName;
        this.username = username;
        this.email = email;
        this.simulated = simulated;
        if(isValidPassword(password) || simulated){
            this.password = hashPassword(password);
        }else{
            logger.warning("Make sure your password is at least 10 characters long and contain letters and numbers");
            throw new IllegalArgumentException();
        }
    }

    public User(){}

    public String hashPassword(String password){
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }catch(NoSuchAlgorithmException e){
            logger.warning("Unable to save password");
            return null;
        }
    }

    public static boolean isValidPassword(String password) {
        boolean hasLetter = false;
        boolean hasNumber = false;
        boolean passLength = password.length() >= 6;
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasNumber = true;
            }
            if (hasLetter && hasNumber && passLength) {
                return
                        true; // Password meets the criteria
            }
        }
        return false; // Password doesn't meet the criteria
    }
    public void setPassword(String password) {
        this.password = hashPassword(password);
    }


    @Override
    public String toString() {
        return "User{" +
                "fName='" + fName + '\'' +
                ", lName='" + lName + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", simulated=" + simulated +
                '}';
    }
}

