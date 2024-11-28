package com.example.Api_Server.repository;

import com.example.Api_Server.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, Long> {

    @Transactional
    @Query("SELECT v FROM Vendor v WHERE v.id = :id")
    Optional<Vendor> findById(@Param("id") int id);
}
