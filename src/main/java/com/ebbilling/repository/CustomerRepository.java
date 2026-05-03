package com.ebbilling.repository;

import com.ebbilling.entity.Customer;
import com.ebbilling.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByMeterNumber(String meterNumber);
    boolean existsByMeterNumber(String meterNumber);
    List<Customer> findByUser(User user);
}
