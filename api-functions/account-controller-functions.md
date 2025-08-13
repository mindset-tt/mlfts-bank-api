# AccountController Functions

## Overview
The AccountController manages all bank account operations including account creation, balance inquiries, account status management, and account closure.

**Base Path**: `/api/v1/accounts`

---

## 🏦 Functions List

### 1. **Create Account**
- **Endpoint**: `POST /accounts`
- **Purpose**: Create a new bank account for authenticated user
- **Access Level**: Protected
- **Required Role**: CUSTOMER, ADMIN, MANAGER, TELLER

#### Parameters
```json
{
  "accountType": "CHECKING|SAVINGS|BUSINESS",
  "initialBalance": "number (required, >= minimum balance)",
  "overdraftLimit": "number (optional, default: 0)",
  "purpose": "string (optional, account purpose description)"
}
```

#### Response
```json
{
  "id": 1,
  "accountNumber": "ACC1234567890",
  "accountType": "CHECKING",
  "balance": 1000.00,
  "availableBalance": 1000.00,
  "overdraftLimit": 500.00,
  "minimumBalance": 100.00,
  "interestRate": 0.01,
  "monthlyMaintenanceFee": 5.00,
  "isActive": true,
  "isFrozen": false,
  "openedDate": "2025-08-12",
  "closedDate": null,
  "userId": 1,
  "createdAt": "2025-08-12T10:30:00",
  "updatedAt": "2025-08-12T10:30:00"
}
```

#### Business Logic
• Validates user authentication and authorization
• Checks minimum balance requirements based on account type
• Generates unique account number (10-digit format)
• Sets account-specific parameters (interest rate, fees)
• Creates initial deposit transaction if balance > 0
• Records account creation in audit logs
• Sends account creation notification to user

#### Minimum Balance Requirements
- **CHECKING**: $100.00
- **SAVINGS**: $500.00  
- **BUSINESS**: $1,000.00

---

### 2. **Get User Accounts**
- **Endpoint**: `GET /accounts`
- **Purpose**: Retrieve all accounts owned by authenticated user
- **Access Level**: Protected
- **Required Role**: CUSTOMER, ADMIN, MANAGER, TELLER

#### Parameters
- None (uses authenticated user context)

#### Response
```json
[
  {
    "id": 1,
    "accountNumber": "ACC1234567890",
    "accountType": "CHECKING",
    "balance": 1500.00,
    "availableBalance": 1500.00,
    "overdraftLimit": 500.00,
    "isActive": true,
    "isFrozen": false,
    "openedDate": "2025-08-12"
  },
  {
    "id": 2,
    "accountNumber": "ACC1234567891",
    "accountType": "SAVINGS",
    "balance": 5000.00,
    "availableBalance": 5000.00,
    "overdraftLimit": 0.00,
    "isActive": true,
    "isFrozen": false,
    "openedDate": "2025-08-12"
  }
]
```

#### Business Logic
• Retrieves accounts owned by authenticated user
• Filters out closed/inactive accounts unless specifically requested
• Returns accounts ordered by creation date (newest first)
• Includes current balance and available balance calculations
• TELLER can view accounts for customer service purposes

---

### 3. **Get Account by ID**
- **Endpoint**: `GET /accounts/{accountId}`
- **Purpose**: Retrieve detailed information for specific account
- **Access Level**: Protected
- **Required Role**: CUSTOMER (own accounts), ADMIN, MANAGER, TELLER

#### Parameters
- **Path Variable**: `accountId` (Long, required)

#### Response
```json
{
  "id": 1,
  "accountNumber": "ACC1234567890",
  "accountType": "CHECKING",
  "balance": 1500.00,
  "availableBalance": 1200.00,
  "overdraftLimit": 500.00,
  "minimumBalance": 100.00,
  "interestRate": 0.01,
  "monthlyMaintenanceFee": 5.00,
  "isActive": true,
  "isFrozen": false,
  "openedDate": "2025-08-12",
  "closedDate": null,
  "lastTransactionDate": "2025-08-12T14:30:00",
  "transactionCount": 5,
  "userId": 1,
  "userName": "John Doe",
  "createdAt": "2025-08-12T10:30:00",
  "updatedAt": "2025-08-12T14:30:00"
}
```

#### Business Logic
• Validates account ownership or appropriate role access
• Calculates available balance (balance - pending transactions)
• Includes transaction summary information
• Shows account status and restrictions
• Provides complete account history overview

---

### 4. **Freeze Account**
- **Endpoint**: `PUT /accounts/{accountId}/freeze`
- **Purpose**: Temporarily freeze account to prevent transactions
- **Access Level**: Protected
- **Required Role**: ADMIN, MANAGER, TELLER

#### Parameters
- **Path Variable**: `accountId` (Long, required)
- **Request Body**:
```json
{
  "reason": "string (required, freeze reason)",
  "notifyCustomer": "boolean (optional, default: true)"
}
```

#### Response
```json
{
  "id": 1,
  "accountNumber": "ACC1234567890",
  "isActive": true,
  "isFrozen": true,
  "frozenDate": "2025-08-12T15:00:00",
  "frozenReason": "Suspicious activity detected",
  "message": "Account frozen successfully"
}
```

#### Business Logic
• Validates administrative privileges
• Immediately blocks all transactions on account
• Records freeze reason and timestamp
• Notifies account holder via email/SMS
• Creates audit trail entry
• Allows emergency unfreezing by senior staff

---

### 5. **Unfreeze Account**
- **Endpoint**: `PUT /accounts/{accountId}/unfreeze`
- **Purpose**: Remove freeze restriction from account
- **Access Level**: Protected
- **Required Role**: ADMIN, MANAGER, TELLER

#### Parameters
- **Path Variable**: `accountId` (Long, required)
- **Request Body**:
```json
{
  "reason": "string (required, unfreeze reason)",
  "authorizedBy": "string (optional, authorizing staff member)"
}
```

#### Response
```json
{
  "id": 1,
  "accountNumber": "ACC1234567890",
  "isActive": true,
  "isFrozen": false,
  "unfrozenDate": "2025-08-12T16:00:00",
  "unfrozenReason": "Investigation completed - no fraud detected",
  "message": "Account unfrozen successfully"
}
```

#### Business Logic
• Validates authorization to unfreeze account
• Restores normal transaction capabilities
• Records unfreeze reason and authorizing staff
• Notifies account holder of account restoration
• Creates audit trail entry

---

### 6. **Close Account**
- **Endpoint**: `DELETE /accounts/{accountId}`
- **Purpose**: Permanently close a bank account
- **Access Level**: Protected  
- **Required Role**: CUSTOMER (own accounts), ADMIN, MANAGER

#### Parameters
- **Path Variable**: `accountId` (Long, required)
- **Request Body**:
```json
{
  "reason": "string (required, closure reason)",
  "transferRemainingBalance": "boolean (optional, default: false)",
  "transferToAccountId": "number (required if transferRemainingBalance=true)"
}
```

#### Response
```json
{
  "id": 1,
  "accountNumber": "ACC1234567890",
  "isActive": false,
  "closedDate": "2025-08-12T17:00:00",
  "closureReason": "Customer request - moving to another bank",
  "finalBalance": 0.00,
  "message": "Account closed successfully"
}
```

#### Business Logic
• Validates account can be closed (no outstanding loans/obligations)
• Checks for remaining balance and pending transactions
• Transfers remaining balance if requested
• Deactivates all associated cards
• Cancels scheduled transactions
• Creates final account statement
• Records closure in audit logs
• Sends account closure confirmation

---

### 7. **Get Account Balance**
- **Endpoint**: `GET /accounts/{accountId}/balance`
- **Purpose**: Get current account balance and available funds
- **Access Level**: Protected
- **Required Role**: CUSTOMER (own accounts), ADMIN, MANAGER, TELLER

#### Parameters
- **Path Variable**: `accountId` (Long, required)

#### Response
```json
{
  "accountId": 1,
  "accountNumber": "ACC1234567890",
  "currentBalance": 1500.00,
  "availableBalance": 1200.00,
  "pendingDebits": 300.00,
  "pendingCredits": 0.00,
  "overdraftLimit": 500.00,
  "overdraftUsed": 0.00,
  "minimumBalance": 100.00,
  "lastUpdated": "2025-08-12T17:30:00"
}
```

#### Business Logic
• Calculates real-time available balance
• Includes pending transaction impacts
• Shows overdraft utilization
• Provides balance history trend
• Updates last access timestamp

---

### 8. **Update Account Settings**
- **Endpoint**: `PUT /accounts/{accountId}/settings`
- **Purpose**: Update account preferences and settings
- **Access Level**: Protected
- **Required Role**: CUSTOMER (own accounts), ADMIN, MANAGER, TELLER

#### Parameters
- **Path Variable**: `accountId` (Long, required)
- **Request Body**:
```json
{
  "overdraftLimit": "number (optional)",
  "monthlyMaintenanceFee": "number (optional, admin only)",
  "interestRate": "number (optional, admin only)",
  "notificationPreferences": {
    "lowBalanceAlert": "boolean",
    "transactionAlerts": "boolean",
    "monthlyStatements": "boolean"
  }
}
```

#### Response
```json
{
  "id": 1,
  "accountNumber": "ACC1234567890",
  "overdraftLimit": 750.00,
  "monthlyMaintenanceFee": 5.00,
  "interestRate": 0.015,
  "notificationPreferences": {
    "lowBalanceAlert": true,
    "transactionAlerts": true,
    "monthlyStatements": true
  },
  "message": "Account settings updated successfully"
}
```

#### Business Logic
• Validates user permissions for each setting
• Applies business rules for limit changes
• Requires approval for significant increases
• Updates notification preferences
• Records setting changes in audit logs

---

## 🔒 Security Features

### Access Control
• **Account Ownership**: Users can only access their own accounts
• **Role-based Access**: TELLER can assist customers, ADMIN has full access
• **Transaction Limits**: Daily/monthly limits enforced
• **Fraud Detection**: Suspicious activity monitoring

### Validation Rules
• **Minimum Balances**: Enforced per account type
• **Overdraft Limits**: Maximum limits based on credit assessment
• **Account Numbers**: Unique, auto-generated identifiers
• **Status Checks**: Prevents operations on frozen/closed accounts

---

## 📊 Error Responses

### Insufficient Balance (400)
```json
{
  "status": 400,
  "error": "Insufficient Balance",
  "message": "Account balance insufficient for this operation",
  "details": {
    "currentBalance": 100.00,
    "requiredAmount": 500.00,
    "minimumBalance": 100.00
  }
}
```

### Account Frozen (423)
```json
{
  "status": 423,
  "error": "Account Locked",
  "message": "Account is frozen and cannot process transactions",
  "details": {
    "frozenDate": "2025-08-12T15:00:00",
    "reason": "Suspicious activity detected"
  }
}
```

### Account Not Found (404)
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Account not found or access denied"
}
```

---

## 🧪 Usage Examples

### Create Checking Account
```bash
curl -X POST http://localhost:8080/api/v1/accounts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${JWT_TOKEN}" \
  -d '{
    "accountType": "CHECKING",
    "initialBalance": 1000.00,
    "overdraftLimit": 500.00,
    "purpose": "Primary checking account"
  }'
```

### Get User Accounts
```bash
curl -X GET http://localhost:8080/api/v1/accounts \
  -H "Authorization: Bearer ${JWT_TOKEN}"
```

### Freeze Account (TELLER/ADMIN)
```bash
curl -X PUT http://localhost:8080/api/v1/accounts/1/freeze \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${TELLER_TOKEN}" \
  -d '{
    "reason": "Customer reported card theft",
    "notifyCustomer": true
  }'
```

---

## 📈 Performance Considerations

• **Balance Caching**: Cache frequently accessed balances
• **Index Optimization**: Ensure account_number and user_id are indexed
• **Pagination**: Implement pagination for large account lists
• **Connection Pooling**: Optimize database connections
• **Audit Efficiency**: Batch audit log writes for better performance
