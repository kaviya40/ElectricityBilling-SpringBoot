package com.ebbilling.service.impl;

import com.ebbilling.entity.User;
import com.ebbilling.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    public User register(String username, String fullName, String email,
                         String password, String phone, User.Role role) {
        if (userRepository.existsByUsername(username))
            throw new RuntimeException("Username already taken");
        if (userRepository.existsByEmail(email))
            throw new RuntimeException("Email already registered");

        return userRepository.save(User.builder()
                .username(username).fullName(fullName).email(email)
                .password(passwordEncoder.encode(password))
                .phone(phone).role(role).enabled(true).build());
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<User> findAll() { return userRepository.findAll(); }

    public void delete(Long id) { userRepository.deleteById(id); }

    public boolean existsByUsername(String u) { return userRepository.existsByUsername(u); }
    public boolean existsByEmail(String e)    { return userRepository.existsByEmail(e); }

    public long countByRole(User.Role role)   { return userRepository.countByRole(role); }
}
