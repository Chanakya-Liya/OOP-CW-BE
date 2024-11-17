package com.example.Api_Server.repository;

import com.example.Api_Server.entity.VendorEventAssociation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VendorEventAssociationRepository extends JpaRepository<VendorEventAssociation, Long> {
}
