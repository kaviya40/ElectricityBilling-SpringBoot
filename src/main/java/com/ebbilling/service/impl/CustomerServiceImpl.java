package com.ebbilling.service.impl;

import com.ebbilling.entity.Customer;
import com.ebbilling.entity.User;
import com.ebbilling.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerServiceImpl {

    private final CustomerRepository customerRepository;
    private final UserServiceImpl userService;

    public Customer add(String name, String meter, String address,
                        String phone, String email, Customer.ConnectionType type, String createdByUsername) {
        if (customerRepository.existsByMeterNumber(meter))
            throw new RuntimeException("Meter number already exists: " + meter);
        User user = userService.findByUsername(createdByUsername);
        return customerRepository.save(Customer.builder()
                .name(name).meterNumber(meter.toUpperCase()).address(address)
                .phone(phone).email(email).connectionType(type).user(user).build());
    }

    public Customer findById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
    }

    public List<Customer> findAll() { return customerRepository.findAll(); }

    public List<Customer> findByUser(String username) {
        return customerRepository.findByUser(userService.findByUsername(username));
    }

    public Customer update(Long id, String name, String address, String phone,
                           String email, Customer.ConnectionType type) {
        Customer c = findById(id);
        c.setName(name); c.setAddress(address); c.setPhone(phone);
        c.setEmail(email); c.setConnectionType(type);
        return customerRepository.save(c);
    }

    public void delete(Long id) { customerRepository.deleteById(id); }

    public boolean existsByMeter(String m) { return customerRepository.existsByMeterNumber(m); }

    public long count() { return customerRepository.count(); }
}
