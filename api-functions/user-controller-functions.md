# UserController Functions

## Overview

The UserController manages user account operations including profile management, password reset, account settings, and customer service functions.

**Base Path**: `/api/v1/users`

---

## 👤 Functions List

### 1. **Get User Profile**

- **Endpoint**: `GET /users/profile`
- **Purpose**: Retrieve current user profile information
- **Access Level**: Protected
- **Required Role**: CUSTOMER, ADMIN, TELLER, LOAN_OFFICER

#### No Parameters Required

#### Profile Response

```json
{
  "id": 1,
  "username": "john.doe",
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "fullName": "John Doe",
  "phone": "+1234567890",
  "dateOfBirth": "1990-05-15",
  "address": {
    "street": "123 Main Street",
    "city": "New York",
    "state": "NY",
    "zipCode": "10001",
    "country": "USA"
  },
  "role": "CUSTOMER",
  "accountStatus": "ACTIVE",
  "emailVerified": true,
  "phoneVerified": true,
  "twoFactorEnabled": true,
  "lastLoginDate": "2025-08-12T09:00:00",
  "createdAt": "2025-01-15T10:30:00",
  "updatedAt": "2025-08-12T09:00:00",
  "preferences": {
    "language": "en",
    "timezone": "America/New_York",
    "currency": "USD",
    "notifications": {
      "email": true,
      "sms": true,
      "push": true
    }
  }
}
```

#### Business Process

• Retrieves authenticated user information
• Returns complete profile data
• Includes security settings
• Shows verification status
• Provides preference settings

---

### 2. **Update User Profile**

- **Endpoint**: `PUT /users/profile`
- **Purpose**: Update user profile information
- **Access Level**: Protected
- **Required Role**: CUSTOMER, ADMIN, TELLER

#### Update Request

```json
{
  "firstName": "string (optional)",
  "lastName": "string (optional)",
  "phone": "string (optional)",
  "address": {
    "street": "string (optional)",
    "city": "string (optional)",
    "state": "string (optional)",
    "zipCode": "string (optional)",
    "country": "string (optional)"
  },
  "preferences": {
    "language": "string (optional)",
    "timezone": "string (optional)",
    "currency": "string (optional)",
    "notifications": {
      "email": "boolean (optional)",
      "sms": "boolean (optional)",
      "push": "boolean (optional)"
    }
  }
}
```

#### Update Response

```json
{
  "id": 1,
  "username": "john.doe",
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+1234567891",
  "address": {
    "street": "456 Oak Avenue",
    "city": "New York",
    "state": "NY",
    "zipCode": "10002",
    "country": "USA"
  },
  "preferences": {
    "language": "en",
    "timezone": "America/New_York",
    "currency": "USD",
    "notifications": {
      "email": true,
      "sms": false,
      "push": true
    }
  },
  "updatedAt": "2025-08-12T15:30:00",
  "message": "Profile updated successfully"
}
```

#### Business Process

• Validates user input data
• Updates allowed profile fields
• Triggers verification for sensitive changes
• Records profile change audit
• Sends update confirmation

---

### 3. **Reset User Password (Admin/Teller)**

- **Endpoint**: `POST /users/{userId}/reset-password`
- **Purpose**: Reset password for customer (customer service function)
- **Access Level**: Protected
- **Required Role**: ADMIN, TELLER

#### Path Parameter

- `userId` (Long, required)

#### Reset Request

```json
{
  "newPassword": "string (required, min 8 chars)",
  "temporaryPassword": "boolean (default: true)",
  "sendNotification": "boolean (default: true)",
  "reason": "string (required)"
}
```

#### Reset Response

```json
{
  "userId": 2,
  "username": "customer.user",
  "passwordReset": true,
  "isTemporary": true,
  "resetDate": "2025-08-12T16:00:00",
  "resetBy": "TELLER_001",
  "reason": "Customer forgot password",
  "expiryDate": "2025-08-19T16:00:00",
  "mustChangeOnLogin": true,
  "message": "Password reset successfully",
  "notificationSent": true
}
```

#### Business Process

• Validates administrative authorization
• Generates secure temporary password
• Forces password change on next login
• Records password reset audit
• Sends customer notification
• Logs customer service activity

---

### 4. **Search Users (Admin/Teller)**

- **Endpoint**: `GET /users/search`
- **Purpose**: Search for users by various criteria
- **Access Level**: Protected
- **Required Role**: ADMIN, TELLER

#### Search Parameters

- `query`: string (optional, search term)
- `email`: string (optional, exact email)
- `phone`: string (optional, phone number)
- `accountNumber`: string (optional, account number)
- `firstName`: string (optional, first name)
- `lastName`: string (optional, last name)
- `role`: string (optional, user role)
- `status`: string (optional, account status)
- `page`: number (optional, default: 0)
- `size`: number (optional, default: 20)

#### Search Results

```json
{
  "content": [
    {
      "id": 2,
      "username": "jane.smith",
      "email": "jane.smith@example.com",
      "firstName": "Jane",
      "lastName": "Smith",
      "fullName": "Jane Smith",
      "phone": "+1234567892",
      "role": "CUSTOMER",
      "accountStatus": "ACTIVE",
      "lastLoginDate": "2025-08-11T14:20:00",
      "createdAt": "2025-02-01T09:15:00",
      "accountsCount": 2,
      "totalBalance": 5432.10
    }
  ],
  "totalElements": 15,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

#### Business Process

• Performs comprehensive user search
• Supports multiple search criteria
• Returns paginated results
• Includes account summary information
• Logs search activity for audit

---

### 5. **Lock/Unlock User Account**

- **Endpoint**: `POST /users/{userId}/lock`
- **Purpose**: Lock or unlock user account for security
- **Access Level**: Protected
- **Required Role**: ADMIN, TELLER

#### Path Parameter

- `userId` (Long, required)

#### Lock Request

```json
{
  "action": "LOCK|UNLOCK",
  "reason": "string (required)",
  "duration": "number (optional, hours for temporary lock)",
  "notifyUser": "boolean (default: true)"
}
```

#### Lock Response

```json
{
  "userId": 2,
  "username": "customer.user",
  "previousStatus": "ACTIVE",
  "currentStatus": "LOCKED",
  "action": "LOCK",
  "reason": "Suspicious activity detected",
  "lockedDate": "2025-08-12T17:00:00",
  "lockedBy": "TELLER_001",
  "unlockDate": "2025-08-13T17:00:00",
  "isTemporary": true,
  "message": "Account locked successfully",
  "notificationSent": true
}
```

#### Business Process

• Validates security authorization
• Immediately blocks user access
• Records security action reason
• Sets automatic unlock if temporary
• Notifies user of account status
• Creates security audit entry

---

### 6. **Get User Activity**

- **Endpoint**: `GET /users/{userId}/activity`
- **Purpose**: Retrieve user activity and login history
- **Access Level**: Protected
- **Required Role**: CUSTOMER (own activity), ADMIN, TELLER

#### Path Parameter

- `userId` (Long, required)

#### Activity Parameters

- `page`: number (optional, default: 0)
- `size`: number (optional, default: 50)
- `activityType`: string (optional, filter by type)
- `startDate`: date (optional, date range)
- `endDate`: date (optional, date range)

#### Activity Response

```json
{
  "userId": 1,
  "username": "john.doe",
  "content": [
    {
      "id": 1,
      "activityType": "LOGIN",
      "description": "User logged in successfully",
      "timestamp": "2025-08-12T09:00:00",
      "ipAddress": "192.168.1.100",
      "userAgent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)...",
      "location": "New York, NY",
      "deviceInfo": "Windows Desktop",
      "success": true
    },
    {
      "id": 2,
      "activityType": "PROFILE_UPDATE",
      "description": "Profile information updated",
      "timestamp": "2025-08-12T15:30:00",
      "changedFields": ["phone", "address"],
      "success": true
    }
  ],
  "totalElements": 45,
  "totalPages": 1,
  "size": 50,
  "number": 0,
  "summary": {
    "totalLogins": 25,
    "failedLogins": 2,
    "lastLogin": "2025-08-12T09:00:00",
    "uniqueDevices": 3,
    "uniqueLocations": 2
  }
}
```

#### Business Process

• Returns comprehensive activity log
• Includes login and security events
• Shows device and location information
• Provides activity analytics
• Supports fraud investigation

---

### 7. **Update User Settings**

- **Endpoint**: `PUT /users/settings`
- **Purpose**: Update user security and notification settings
- **Access Level**: Protected
- **Required Role**: CUSTOMER, ADMIN, TELLER

#### Settings Request

```json
{
  "securitySettings": {
    "twoFactorEnabled": "boolean (optional)",
    "sessionTimeout": "number (optional, minutes)",
    "allowMultipleSessions": "boolean (optional)"
  },
  "notificationSettings": {
    "emailNotifications": "boolean (optional)",
    "smsNotifications": "boolean (optional)",
    "pushNotifications": "boolean (optional)",
    "transactionAlerts": "boolean (optional)",
    "loginAlerts": "boolean (optional)",
    "marketingEmails": "boolean (optional)"
  },
  "privacySettings": {
    "shareDataForImprovement": "boolean (optional)",
    "allowThirdPartyOffers": "boolean (optional)"
  }
}
```

#### Settings Response

```json
{
  "userId": 1,
  "securitySettings": {
    "twoFactorEnabled": true,
    "sessionTimeout": 30,
    "allowMultipleSessions": false,
    "lastPasswordChange": "2025-07-15T10:00:00"
  },
  "notificationSettings": {
    "emailNotifications": true,
    "smsNotifications": false,
    "pushNotifications": true,
    "transactionAlerts": true,
    "loginAlerts": true,
    "marketingEmails": false
  },
  "privacySettings": {
    "shareDataForImprovement": true,
    "allowThirdPartyOffers": false
  },
  "updatedAt": "2025-08-12T18:00:00",
  "message": "Settings updated successfully"
}
```

#### Business Process

• Updates security configurations
• Configures notification preferences
• Sets privacy options
• Validates security requirements
• Records settings changes

---

### 8. **Verify User Identity**

- **Endpoint**: `POST /users/{userId}/verify-identity`
- **Purpose**: Verify user identity for account services
- **Access Level**: Protected
- **Required Role**: ADMIN, TELLER

#### Path Parameter

- `userId` (Long, required)

#### Verification Request

```json
{
  "verificationType": "PHONE|EMAIL|DOCUMENT|IN_PERSON",
  "verificationData": {
    "documentType": "DRIVERS_LICENSE|PASSPORT|SSN",
    "documentNumber": "string (optional)",
    "verificationCode": "string (optional)"
  },
  "verifiedBy": "string (required)",
  "notes": "string (optional)"
}
```

#### Verification Response

```json
{
  "userId": 2,
  "username": "customer.user",
  "verificationType": "PHONE",
  "verificationStatus": "VERIFIED",
  "verificationDate": "2025-08-12T19:00:00",
  "verifiedBy": "TELLER_001",
  "expiryDate": "2026-08-12T19:00:00",
  "verificationLevel": "STANDARD",
  "notes": "Phone verification completed successfully",
  "message": "Identity verification successful"
}
```

#### Business Process

• Performs identity verification checks
• Records verification method and results
• Updates customer verification status
• Sets verification expiry dates
• Creates compliance audit trail

---

## 🔒 Security Features

### Profile Protection

• **Data Validation**: Input sanitization and validation
• **Access Control**: Role-based profile access
• **Change Auditing**: Complete profile change tracking
• **PII Protection**: Sensitive data encryption

### Password Security

• **Strong Password Policy**: Complexity requirements
• **Password History**: Prevent password reuse
• **Temporary Passwords**: Secure temporary access
• **Password Expiry**: Automatic password aging

### Activity Monitoring

• **Login Tracking**: Complete login audit trail
• **Device Fingerprinting**: Unusual device detection
• **Location Monitoring**: Geographic access tracking
• **Fraud Detection**: Suspicious activity identification

---

## 📊 Error Responses

### User Not Found (404)

```json
{
  "status": 404,
  "error": "User Not Found",
  "message": "User with ID 999 not found or access denied"
}
```

### Insufficient Permissions (403)

```json
{
  "status": 403,
  "error": "Access Denied",
  "message": "Insufficient permissions to access user data"
}
```

### Validation Error (400)

```json
{
  "status": 400,
  "error": "Validation Failed",
  "message": "Invalid input data",
  "details": {
    "phone": "Invalid phone number format",
    "email": "Email address already in use"
  }
}
```

### Account Locked (423)

```json
{
  "status": 423,
  "error": "Account Locked",
  "message": "User account is currently locked",
  "details": {
    "lockReason": "Security lock due to suspicious activity",
    "lockedDate": "2025-08-12T17:00:00",
    "unlockDate": "2025-08-13T17:00:00"
  }
}
```

---

## 🧪 Usage Examples

### Get User Profile

```bash
curl -X GET http://localhost:8080/api/v1/users/profile \
  -H "Authorization: Bearer ${JWT_TOKEN}"
```

### Update Profile Information

```bash
curl -X PUT http://localhost:8080/api/v1/users/profile \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${JWT_TOKEN}" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "phone": "+1234567891",
    "address": {
      "street": "456 Oak Avenue",
      "city": "New York",
      "state": "NY",
      "zipCode": "10002"
    }
  }'
```

### Reset Customer Password (Teller)

```bash
curl -X POST http://localhost:8080/api/v1/users/2/reset-password \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${TELLER_JWT_TOKEN}" \
  -d '{
    "newPassword": "NewSecure123!",
    "temporaryPassword": true,
    "reason": "Customer forgot password and requested reset"
  }'
```

### Search Users (Admin/Teller)

```bash
curl -X GET "http://localhost:8080/api/v1/users/search?query=john&page=0&size=10" \
  -H "Authorization: Bearer ${TELLER_JWT_TOKEN}"
```

### Lock User Account

```bash
curl -X POST http://localhost:8080/api/v1/users/2/lock \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${ADMIN_JWT_TOKEN}" \
  -d '{
    "action": "LOCK",
    "reason": "Suspicious activity detected",
    "duration": 24,
    "notifyUser": true
  }'
```

---

## 📈 Performance Considerations

• **Profile Caching**: Cache frequently accessed profiles
• **Search Optimization**: Indexed search fields for fast lookups
• **Activity Logging**: Asynchronous activity recording
• **Database Indexing**: Index on username, email, phone for searches
• **Session Management**: Efficient session storage and cleanup
