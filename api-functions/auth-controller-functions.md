# AuthController Functions

## Overview
The AuthController handles all authentication-related operations including user registration, login, logout, and token management.

**Base Path**: `/api/v1/auth`

---

## 🔐 Functions List

### 1. **User Registration**
- **Endpoint**: `POST /auth/register`
- **Purpose**: Register a new customer account
- **Access Level**: Public
- **Required Role**: None

#### Parameters
```json
{
  "username": "string (required, 3-50 chars)",
  "password": "string (required, 8+ chars)",
  "email": "string (required, valid email)",
  "firstName": "string (required)",
  "lastName": "string (required)",
  "phoneNumber": "string (required)",
  "dateOfBirth": "date (required)",
  "address": "string (required)",
  "city": "string (required)",
  "state": "string (required)",
  "postalCode": "string (required)",
  "country": "string (required)"
}
```

#### Response
```json
{
  "accessToken": "JWT_TOKEN",
  "refreshToken": "REFRESH_TOKEN",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "user": {
    "id": 1,
    "username": "johndoe",
    "email": "john@example.com",
    "role": "CUSTOMER"
  }
}
```

#### Business Logic
• Validates user input data
• Checks username and email uniqueness
• Encrypts password using BCrypt
• Creates user with CUSTOMER role by default
• Generates JWT access and refresh tokens
• Logs registration activity in audit system

---

### 2. **Admin/Staff Registration**
- **Endpoint**: `POST /auth/register-admin`
- **Purpose**: Register admin, manager, teller, or officer accounts
- **Access Level**: Protected
- **Required Role**: ADMIN or MANAGER

#### Parameters
```json
{
  "username": "string (required)",
  "password": "string (required)",
  "email": "string (required)",
  "firstName": "string (required)",
  "lastName": "string (required)",
  "phoneNumber": "string (required)",
  "dateOfBirth": "date (required)",
  "address": "string (required)",
  "city": "string (required)",
  "state": "string (required)",
  "postalCode": "string (required)",
  "country": "string (required)",
  "role": "ADMIN|MANAGER|TELLER|LOAN_OFFICER|SECURITY_OFFICER"
}
```

#### Business Logic
• Validates admin privileges of requesting user
• Creates user with specified administrative role
• Sends welcome email to new staff member
• Logs admin creation activity

---

### 3. **User Login**
- **Endpoint**: `POST /auth/login`
- **Purpose**: Authenticate user and provide access tokens
- **Access Level**: Public
- **Required Role**: None

#### Parameters
```json
{
  "username": "string (required)",
  "password": "string (required)"
}
```

#### Response
```json
{
  "accessToken": "JWT_TOKEN",
  "refreshToken": "REFRESH_TOKEN",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "user": {
    "id": 1,
    "username": "johndoe",
    "email": "john@example.com",
    "role": "CUSTOMER",
    "lastLogin": "2025-08-12T10:30:00"
  }
}
```

#### Business Logic
• Validates username and password
• Checks account status (enabled, non-locked)
• Updates last login timestamp
• Generates new JWT tokens
• Records login attempt (success/failure)
• Implements rate limiting for failed attempts
• Locks account after multiple failed attempts

---

### 4. **Token Refresh**
- **Endpoint**: `POST /auth/refresh`
- **Purpose**: Generate new access token using refresh token
- **Access Level**: Protected
- **Required Role**: Any authenticated user

#### Parameters
```json
{
  "refreshToken": "string (required)"
}
```

#### Response
```json
{
  "accessToken": "NEW_JWT_TOKEN",
  "refreshToken": "NEW_REFRESH_TOKEN",
  "tokenType": "Bearer",
  "expiresIn": 86400
}
```

#### Business Logic
• Validates refresh token signature and expiration
• Checks if refresh token is not blacklisted
• Generates new access and refresh tokens
• Invalidates old refresh token
• Updates token usage tracking

---

### 5. **User Logout**
- **Endpoint**: `POST /auth/logout`
- **Purpose**: Invalidate user session and tokens
- **Access Level**: Protected
- **Required Role**: Any authenticated user

#### Parameters
- **Header**: `Authorization: Bearer {JWT_TOKEN}`

#### Response
```json
{
  "message": "Logged out successfully"
}
```

#### Business Logic
• Validates current JWT token
• Adds token to blacklist
• Invalidates all refresh tokens for user
• Records logout activity
• Clears any server-side session data

---

### 6. **Change Password**
- **Endpoint**: `POST /auth/change-password`
- **Purpose**: Allow users to change their password
- **Access Level**: Protected
- **Required Role**: Any authenticated user

#### Parameters
```json
{
  "currentPassword": "string (required)",
  "newPassword": "string (required, 8+ chars)",
  "confirmPassword": "string (required, must match newPassword)"
}
```

#### Response
```json
{
  "message": "Password changed successfully"
}
```

#### Business Logic
• Validates current password
• Checks new password strength requirements
• Encrypts new password with BCrypt
• Updates password and timestamp
• Invalidates all existing sessions
• Sends password change notification email
• Logs password change activity

---

### 7. **Forgot Password**
- **Endpoint**: `POST /auth/forgot-password`
- **Purpose**: Initiate password reset process
- **Access Level**: Public
- **Required Role**: None

#### Parameters
```json
{
  "email": "string (required, valid email)"
}
```

#### Response
```json
{
  "message": "Password reset email sent"
}
```

#### Business Logic
• Validates email exists in system
• Generates secure password reset token
• Sets token expiration (usually 1 hour)
• Sends password reset email
• Logs password reset request
• Rate limits reset requests per email

---

### 8. **Reset Password**
- **Endpoint**: `POST /auth/reset-password`
- **Purpose**: Complete password reset with token
- **Access Level**: Public
- **Required Role**: None

#### Parameters
```json
{
  "token": "string (required, reset token)",
  "newPassword": "string (required, 8+ chars)",
  "confirmPassword": "string (required, must match)"
}
```

#### Response
```json
{
  "message": "Password reset successfully"
}
```

#### Business Logic
• Validates reset token and expiration
• Checks password strength requirements
• Updates user password
• Invalidates reset token
• Invalidates all existing sessions
• Sends password reset confirmation email
• Logs password reset completion

---

## 🔒 Security Features

### Password Security
• **Encryption**: BCrypt with salt rounds
• **Strength Requirements**: Minimum 8 characters, mixed case, numbers
• **History**: Prevents reuse of last 5 passwords
• **Expiration**: Optional password expiration policy

### Token Security
• **JWT Algorithm**: HS512 with secret key
• **Access Token**: Short-lived (1 hour default)
• **Refresh Token**: Longer-lived (7 days default)
• **Blacklisting**: Invalidated tokens tracked
• **Rotation**: Refresh tokens rotated on use

### Account Protection
• **Rate Limiting**: Login attempt throttling
• **Account Lockout**: Temporary lock after failed attempts
• **IP Tracking**: Monitor login locations
• **Device Tracking**: Track login devices
• **Audit Logging**: Complete activity tracking

---

## 📊 Error Responses

### Validation Errors (400)
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "details": [
    {
      "field": "password",
      "message": "Password must be at least 8 characters"
    }
  ]
}
```

### Authentication Errors (401)
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid credentials"
}
```

### Account Locked (423)
```json
{
  "status": 423,
  "error": "Locked",
  "message": "Account locked due to multiple failed login attempts"
}
```

---

## 🧪 Usage Examples

### Register New Customer
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "password": "SecurePass123!",
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+1234567890",
    "dateOfBirth": "1990-01-01",
    "address": "123 Main St",
    "city": "New York",
    "state": "NY",
    "postalCode": "10001",
    "country": "USA"
  }'
```

### Login User
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "password": "SecurePass123!"
  }'
```

### Refresh Token
```bash
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "your_refresh_token_here"
  }'
```

---

## 📈 Performance Considerations

• **Token Caching**: Cache valid tokens to reduce database lookups
• **Rate Limiting**: Implement request throttling to prevent abuse
• **Database Indexing**: Ensure username/email columns are indexed
• **Password Hashing**: Balance security vs. performance in BCrypt rounds
• **Session Management**: Use stateless JWT tokens for scalability
