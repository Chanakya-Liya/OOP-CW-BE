package com.example.Api_Server.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Entity
@Table(name = "tickets")
public class Ticket {
    @Getter
    private static int nextId = 1;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Setter
    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;

    @Setter
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Setter
    @Enumerated(EnumType.STRING)
    private TicketStatus status;

    public Ticket() {

    }

    public Ticket(Event event) {
        this.event = event;
        this.status = TicketStatus.AVAILABLE;
    }

    public void sellTicket(Customer customer) {
        this.customer = customer;
        this.status = TicketStatus.SOLD;
    }

    @Override
    public String toString() {
        return "Ticket{" +
                "id=" + id +
                ", event=" + event +
                ", customer=" + (customer != null ? customer : "Not Sold") +
                ", status=" + status +
                '}';
    }
}
