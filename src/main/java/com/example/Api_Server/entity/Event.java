package com.example.Api_Server.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "events")
@ToString(exclude = {"vendor", "tickets"})
public class Event{
    private static int nextId = 1;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Setter
    @Getter
    private String name;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vendor_id")
    private Vendor vendor;
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Getter
    private List<Ticket> tickets = new ArrayList<>();
    private int poolSize;
    private int totalTickets;
    @Setter
    @Getter
    private String photo;
    @Getter
    @Setter
    private String Description;
    @Getter
    @Setter
    private LocalDateTime eventDateTime;


    public Event(String name, int poolSize, int totalTickets) {
        this.id = nextId++;
        this.name = name;
        this.poolSize = poolSize;
        this.totalTickets = totalTickets;
        addPoolTickets(poolSize);
    }

    public Event(){}

    public void addPoolTickets(int poolSize) {
        synchronized (this){
            for (int i = 0; i < poolSize; i++) {
                addTicket(TicketStatus.POOL);
            }
        }
    }

    public void addTicket(TicketStatus status) {
        Ticket ticket = new Ticket();
        ticket.setEvent(this);
        ticket.setStatus(status);
        tickets.add(ticket);
    }

    public List<Ticket> getPoolTickets() {
        return tickets.stream().filter(t -> t.getStatus() == TicketStatus.POOL).collect(Collectors.toList());
    }

    public int getRemainingTicketCount() {
        return (totalTickets - (int) tickets.stream().filter(t -> t.getStatus() == TicketStatus.SOLD).count());
    }

    public List<Ticket> getAvailableTickets() {
        return tickets.stream().filter(t -> t.getStatus() == TicketStatus.AVAILABLE).collect(Collectors.toList());
    }

    public List<Ticket> getSoldTickets() {
        return tickets.stream().filter(t -> t.getStatus() == TicketStatus.SOLD).collect(Collectors.toList());
    }

    public int getId() {
        return id;
    }

    public Vendor getVendor() {
        return vendor;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public int getTotalTickets() {
        return totalTickets;
    }

    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
    }

    public void removeTicketFromPool() {
        tickets.stream().filter(t -> t.getStatus() == TicketStatus.POOL).findFirst()
                .ifPresent(t -> t.setStatus(TicketStatus.SOLD));
    }

    public void addTicketToPool() {
        tickets.stream().filter(t -> t.getStatus() == TicketStatus.AVAILABLE).findFirst()
                .ifPresent(t -> t.setStatus(TicketStatus.POOL));
    }

    public boolean allTicketsSold() {
        return getPoolTickets().isEmpty() && getAvailableTickets().isEmpty();
    }
}

