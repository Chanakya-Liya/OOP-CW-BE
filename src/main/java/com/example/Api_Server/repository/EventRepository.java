package com.example.Api_Server.repository;

import com.example.Api_Server.entity.Event;
import com.example.Api_Server.entity.Ticket;
import com.example.Api_Server.entity.TicketStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.tickets")
    List<Event> findAllWithTickets();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Transactional
    @Query("SELECT t FROM Ticket t WHERE t.status = 'POOL' AND t.event = :event ORDER BY t.id ASC LIMIT 1")
    Optional<Ticket> findPoolTicketByEvent(@Param("event") Event event);
}
