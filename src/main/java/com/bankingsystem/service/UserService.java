package com.bankingsystem.service;

import com.bankingsystem.dto.request.AdminRegistrationRequest;
import com.bankingsystem.dto.request.UserRegistrationRequest;
import com.bankingsystem.dto.response.UserResponse;
import com.bankingsystem.entity.User;
import com.bankingsystem.enums.UserRole;
import com.bankingsystem.exception.ResourceNotFoundException;
import com.bankingsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for user-related operations.
 */
@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuditService auditService;

    public UserResponse createUser(UserRegistrationRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setAddress(request.getAddress());
        user.setCity(request.getCity());
        user.setState(request.getState());
        user.setPostalCode(request.getPostalCode());
        user.setCountry(request.getCountry());
        user.setRole(UserRole.CUSTOMER);
        user.setPasswordChangedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        return convertToUserResponse(savedUser);
    }

    public UserResponse createAdminUser(AdminRegistrationRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Validate role - only allow admin/staff roles
        if (request.getRole() == UserRole.CUSTOMER) {
            throw new IllegalArgumentException("Cannot create customer user through admin registration");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setAddress(request.getAddress());
        user.setCity(request.getCity());
        user.setState(request.getState());
        user.setPostalCode(request.getPostalCode());
        user.setCountry(request.getCountry());
        user.setRole(request.getRole());
        user.setPasswordChangedAt(LocalDateTime.now());
        
        // Admin users are automatically verified
        user.setKycVerified(true);
        user.setIsEnabled(true);

        User savedUser = userRepository.save(user);
        return convertToUserResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id.toString()));
        return convertToUserResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));
        return convertToUserResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", email));
        return convertToUserResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllCustomers() {
        return userRepository.findAllCustomers().stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }

    public UserResponse updateUser(Long id, UserRegistrationRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id.toString()));

        // Check if email is being changed and already exists
        if (!user.getEmail().equals(request.getEmail()) && 
            userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setAddress(request.getAddress());
        user.setCity(request.getCity());
        user.setState(request.getState());
        user.setPostalCode(request.getPostalCode());
        user.setCountry(request.getCountry());

        User updatedUser = userRepository.save(user);
        return convertToUserResponse(updatedUser);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id.toString()));
        
        // Soft delete by disabling the user
        user.setIsEnabled(false);
        userRepository.save(user);
    }

    public UserResponse enableUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id.toString()));
        
        user.setIsEnabled(true);
        user.setIsAccountNonLocked(true);
        user.setFailedLoginAttempts(0);
        
        User updatedUser = userRepository.save(user);
        return convertToUserResponse(updatedUser);
    }

    public UserResponse disableUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id.toString()));
        
        user.setIsEnabled(false);
        
        User updatedUser = userRepository.save(user);
        return convertToUserResponse(updatedUser);
    }

    public void updateLastLogin(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));
        
        user.setLastLogin(LocalDateTime.now());
        user.setFailedLoginAttempts(0);
        userRepository.save(user);
    }

    public void incrementFailedLoginAttempts(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user != null) {
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
            
            // Lock account after 5 failed attempts
            if (user.getFailedLoginAttempts() >= 5) {
                user.setIsAccountNonLocked(false);
            }
            
            userRepository.save(user);
        }
    }

    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    private UserResponse convertToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setFullName(user.getFullName());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setDateOfBirth(user.getDateOfBirth());
        response.setAddress(user.getAddress());
        response.setCity(user.getCity());
        response.setState(user.getState());
        response.setPostalCode(user.getPostalCode());
        response.setCountry(user.getCountry());
        response.setIsEnabled(user.getIsEnabled());
        response.setKycVerified(user.getKycVerified());
        response.setTwoFactorEnabled(user.getTwoFactorEnabled());
        response.setRole(user.getRole());
        response.setLastLogin(user.getLastLogin());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }

    @Transactional
    public UserResponse resetUserPassword(Long id, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id.toString()));
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setFailedLoginAttempts(0);
        user.setIsAccountNonLocked(true);
        
        User savedUser = userRepository.save(user);
        
        // Log password reset
        auditService.logUserAction(id, "PASSWORD_RESET", 
                "Password reset by admin/teller", "USER");
        
        return convertToUserResponse(savedUser);
    }

    @Transactional
    public UserResponse unlockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id.toString()));
        
        user.setIsAccountNonLocked(true);
        user.setFailedLoginAttempts(0);
        
        User savedUser = userRepository.save(user);
        
        // Log account unlock
        auditService.logUserAction(id, "ACCOUNT_UNLOCKED", 
                "Account unlocked by admin/teller", "USER");
        
        return convertToUserResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> searchUsers(String query) {
        List<User> users = userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(query);
        return users.stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }
}
