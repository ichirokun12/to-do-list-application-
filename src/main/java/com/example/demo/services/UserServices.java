package com.example.demo.services;

import com.example.demo.entity.User;
import com.example.demo.repository.TaskRepo;
import com.example.demo.repository.UserRepository;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserServices {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TaskRepo taskRepo;

    public User createUser(
            @Size(max = 50, message = "First name cannot exceed 50 characters") String firstName,
            @Size(max = 50, message = "Last name cannot exceed 50 characters") String lastName,
            @NotBlank(message = "Username is required") String username,
            @NotBlank(message = "Password is required") String password,
            @NotBlank(message = "Email is required") @Email(message = "Email should be valid") String email) {

        // Check if user already exists
        if (existsByUsername(username)) {
            throw new RuntimeException("Username already exists: " + username);
        }

        if (existsByEmail(email)) {
            throw new RuntimeException("Email already exists: " + email);
        }

        User user = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .username(username)
                .password(passwordEncoder.encode(password))
                .email(email)
                .enabled(true)
                .accountNonLocked(true)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .roles("ROLE_USER")
                .build();

        return userRepository.save(user);
    }

    public List<User> getUser(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        List<User> users = new ArrayList<>();
        userOptional.ifPresent(users::add);
        return users;
    }

    public Optional<User> findByUserId(Long userId) {
        return userRepository.findById(userId);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUser(User user) {
        if (!userRepository.existsById(user.getUserId())) {
            throw new RuntimeException("User not found with ID: " + user.getUserId());
        }
        return userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with ID: " + userId);
        }
        userRepository.deleteById(userId);
    }

    public List<User> getUsersByRole(String role) {
        if (!role.startsWith("ROLE_")) {
            role = "ROLE_" + role;
        }
        return userRepository.findByRolesContaining(role);
    }

    public User enableUser(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setEnabled(true);
            return userRepository.save(user);
        }
        throw new RuntimeException("User not found with ID: " + userId);
    }

    public User disableUser(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setEnabled(false);
            return userRepository.save(user);
        }
        throw new RuntimeException("User not found with ID: " + userId);
    }

    public User changePassword(Long userId, String newPassword) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setPassword(passwordEncoder.encode(newPassword));
            return userRepository.save(user);
        }
        throw new RuntimeException("User not found with ID: " + userId);
    }

    public Long getUserCount() {
        return userRepository.count();
    }

    public List<User> searchUsersByUsername(String username) {
        return userRepository.findAll().stream()
                .filter(user -> user.getUsername().toLowerCase().contains(username.toLowerCase()))
                .toList();
    }

    // Remove the empty six-parameter method to avoid confusion
    // public void createUser(...) { } // Remove this
}