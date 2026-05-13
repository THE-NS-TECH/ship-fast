package com.shipfast.repository;

import com.shipfast.domain.DeliveryTrip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeliveryTripRepository extends JpaRepository<DeliveryTrip, Long> {
}
