# OOP-CW-BE (Ticketing System Backend)

## Project Overview
- **Project Name:** Api-Server
- **Description:** Backend for the OOP CW - Ticketing System.
- **Technologies Used:** Java, Spring Boot, PostgreSQL, Gson, Hibernate Validator, Lombok.

## Key Features
- **Customer Management:** Manage customer data and interactions.
- **Vendor Management:** Manage vendors and their events.
- **Event Management:** Create, update, and manage events and tickets.
- **Simulation:** Generate simulated users, vendors, and events for testing purposes.

## Technology Stack
- **Java Version:** 23
- **Spring Boot Version:** 3.3.5
- **Database:** PostgreSQL

## Dependencies
- `org.postgresql:postgresql`
- `org.springframework.boot:spring-boot-starter-data-jpa`
- `com.google.code.gson:gson`
- `jakarta.validation:jakarta.validation-api`
- `org.hibernate.validator:hibernate-validator`
- `org.springframework.boot:spring-boot-starter-web`
- `org.springframework.boot:spring-boot-starter-test`
- `org.projectlombok:lombok`

## How to Run the Application
1. **Clone the repository:**
   ```sh
   git clone https://github.com/Chanakya-Liya/OOP-CW-BE.git
   cd OOP-CW-BE
   ```

2. **Run Docker Compose to set up the database:**
   ```sh
   docker-compose up -d
   ```

3. **Create the `ticketingSystem` database:**
   ```sh
   docker exec -it ticketingSystem psql -U Chanakya -c "CREATE DATABASE ticketingSystem;"
   ```

4. **Build the project using Maven:**
   ```sh
   ./mvnw clean install
   ```

5. **Run the application:**
   ```sh
   ./mvnw spring-boot:run
   ```

## Configuration
- **Configuration File:** Ensure you have altered the `application.yml` configuration file to your liking before starting the simulation.
- **Database Setup:** Update the database URL, username, and password in the `application.yml` file.

## Use Cases
- **Simulated Users:**
  ```java
  Util.generateSimulatedUsers();
  ```
- **Customer Service Initialization:**
  ```java
  customerService.init();
  ```
- **Vendor Service Initialization:**
  ```java
  vendorService.init();
  ```
- **Event Service Initialization:**
  ```java
  eventService.init();
  ```

## Example Usage
- **Simulate Users:**
  ```java
  Util.generateSimulatedUsers();
  ```

## License
This project is licensed under the Apache License, Version 2.0. See [LICENSE](http://www.apache.org/licenses/LICENSE-2.0) for more details.
