package com.bankingsystem.service;

import com.bankingsystem.dto.request.AdminRegistrationRequest;
import com.bankingsystem.dto.request.LoginRequest;
import com.bankingsystem.dto.request.UserRegistrationRequest;
import com.bankingsystem.dto.response.AuthenticationResponse;
import com.bankingsystem.dto.response.UserResponse;
import com.bankingsystem.entity.LoginAttempt;
import com.bankingsystem.entity.User;
import com.bankingsystem.repository.LoginAttemptRepository;
import com.bankingsystem.repository.UserRepository;
import com.bankingsystem.security.CustomUserDetailsService;
import com.bankingsystem.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service class for authentication operations.
 */
@Service
@Transactional
public class AuthenticationService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoginAttemptRepository loginAttemptRepository;

    @Autowired
    private AuditService auditService;

    public AuthenticationResponse register(UserRegistrationRequest request) {
        // Create the user
        UserResponse userResponse = userService.createUser(request);
        
        // Generate tokens
        UserDetails userDetails = userDetailsService.loadUserByUsername(userResponse.getUsername());
        String accessToken = jwtUtil.generateToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);
        
        // Log the registration
        auditService.logUserAction(userResponse.getId(), "USER_REGISTRATION", 
                "User registered successfully", "AUTHENTICATION");
        
        return new AuthenticationResponse(accessToken, refreshToken, 
                jwtUtil.getExpirationTime(), userResponse);
    }

    public AuthenticationResponse registerAdmin(AdminRegistrationRequest request) {
        // Create the admin/staff user
        UserResponse userResponse = userService.createAdminUser(request);
        
        // Generate tokens
        UserDetails userDetails = userDetailsService.loadUserByUsername(userResponse.getUsername());
        String accessToken = jwtUtil.generateToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);
        
        // Log the registration
        auditService.logUserAction(userResponse.getId(), "ADMIN_REGISTRATION", 
                "Admin/Staff user registered successfully: " + request.getRole().getDisplayName(), "AUTHENTICATION");
        
        return new AuthenticationResponse(accessToken, refreshToken, 
                jwtUtil.getExpirationTime(), userResponse);
    }

    public AuthenticationResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String ipAddress = getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        
        try {
            // Authenticate the user
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            // Load user details
            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
            CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                    (CustomUserDetailsService.CustomUserPrincipal) userDetails;

            // Generate tokens
            String accessToken = jwtUtil.generateToken(userDetails);
            String refreshToken = jwtUtil.generateRefreshToken(userDetails);

            // Update last login
            userService.updateLastLogin(request.getUsername());

            // Log successful login attempt
            logLoginAttempt(request.getUsername(), ipAddress, userAgent, true, null, userPrincipal.getUser());

            // Convert user to response
            UserResponse userResponse = userService.getUserById(userPrincipal.getUserId());

            // Log the login
            auditService.logUserAction(userPrincipal.getUserId(), "USER_LOGIN", 
                    "User logged in successfully", "AUTHENTICATION");

            return new AuthenticationResponse(accessToken, refreshToken, 
                    jwtUtil.getExpirationTime(), userResponse);

        } catch (BadCredentialsException e) {
            // Log failed login attempt
            logLoginAttempt(request.getUsername(), ipAddress, userAgent, false, "Invalid credentials", null);
            
            // Increment failed login attempts
            userService.incrementFailedLoginAttempts(request.getUsername());
            
            throw new BadCredentialsException("Invalid username or password");
        }
    }

    public AuthenticationResponse refreshToken(String refreshToken) {
        try {
            if (!jwtUtil.validateToken(refreshToken)) {
                throw new BadCredentialsException("Invalid refresh token");
            }

            String tokenType = jwtUtil.getTokenType(refreshToken);
            if (!"refresh".equals(tokenType)) {
                throw new BadCredentialsException("Invalid token type");
            }

            String username = jwtUtil.extractUsername(refreshToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            String newAccessToken = jwtUtil.generateToken(userDetails);
            String newRefreshToken = jwtUtil.generateRefreshToken(userDetails);

            CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                    (CustomUserDetailsService.CustomUserPrincipal) userDetails;
            UserResponse userResponse = userService.getUserById(userPrincipal.getUserId());

            return new AuthenticationResponse(newAccessToken, newRefreshToken, 
                    jwtUtil.getExpirationTime(), userResponse);

        } catch (Exception e) {
            throw new BadCredentialsException("Invalid refresh token");
        }
    }

    public void logout(String username) {
        // Log the logout action
        UserResponse user = userService.getUserByUsername(username);
        auditService.logUserAction(user.getId(), "USER_LOGOUT", 
                "User logged out successfully", "AUTHENTICATION");
        
        // In a real implementation, you might want to blacklist the token
        // For now, we just log the action
    }

    private void logLoginAttempt(String username, String ipAddress, String userAgent, 
                                 boolean successful, String failureReason, User user) {
        LoginAttempt loginAttempt = new LoginAttempt();
        loginAttempt.setUsername(username);
        loginAttempt.setIpAddress(ipAddress);
        loginAttempt.setUserAgent(userAgent);
        loginAttempt.setAttemptTime(LocalDateTime.now());
        loginAttempt.setIsSuccessful(successful);
        loginAttempt.setFailureReason(failureReason);
        loginAttempt.setUser(user);
        
        loginAttemptRepository.save(loginAttempt);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
