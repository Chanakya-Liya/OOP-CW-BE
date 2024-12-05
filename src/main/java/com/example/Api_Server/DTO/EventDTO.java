package com.example.Api_Server.DTO;

import com.example.Api_Server.entity.Event;
import com.example.Api_Server.entity.Ticket;
import com.example.Api_Server.entity.TicketStatus;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Getter
public class EventDTO {
    private String name;
    private String description;
    private int availableSeats;
    private String photo;
    private String eventDate;

    public EventDTO(Event event) {
        int poolSeats = (int) event.getTickets().stream()
                .filter(ticket -> ticket.getStatus() == TicketStatus.POOL)
                .count();
        this.name = event.getName();
        this.description = event.getDescription();
        this.availableSeats = poolSeats;
        this.eventDate = event.getEventDateTime().toString();
        this.photo = event.getPhoto();

    }
}
