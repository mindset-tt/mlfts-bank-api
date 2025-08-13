# TransactionController Functions

## Overview
The TransactionController handles all transaction operations including deposits, withdrawals, transfers, and transaction history management.

**Base Path**: `/api/v1/transactions`

---

## 💰 Functions List

### 1. **Deposit Money**
- **Endpoint**: `POST /transactions/deposit`
- **Purpose**: Deposit money into a specified account
- **Access Level**: Protected
- **Required Role**: CUSTOMER, ADMIN, TELLER

#### Parameters
```json
{
  "accountId": "number (required)",
  "amount": "number (required, > 0)",
  "description": "string (optional)",
  "depositMethod": "CASH|CHECK|TRANSFER|ATM"
}
```

#### Response
```json
{
  "id": 1,
  "transactionId": "TXN1234567890",
  "transactionType": "DEPOSIT",
  "amount": 500.00,
  "description": "Cash deposit",
  "status": "COMPLETED",
  "accountId": 1,
  "accountNumber": "ACC1234567890",
  "balanceAfter": 1500.00,
  "transactionDate": "2025-08-12T10:30:00",
  "processedBy": "TELLER_001"
}
```

#### Business Logic
• Validates account ownership and status
• Checks deposit limits and compliance rules
• Updates account balance immediately
• Generates unique transaction ID
• Records transaction in audit trail
• Sends deposit confirmation notification
• Updates available balance calculation

---

### 2. **Withdraw Money**
- **Endpoint**: `POST /transactions/withdraw`
- **Purpose**: Withdraw money from a specified account
- **Access Level**: Protected
- **Required Role**: CUSTOMER, ADMIN, TELLER

#### Parameters
```json
{
  "accountId": "number (required)",
  "amount": "number (required, > 0)",
  "description": "string (optional)",
  "withdrawalMethod": "ATM|BRANCH|CHECK|ONLINE"
}
```

#### Response
```json
{
  "id": 2,
  "transactionId": "TXN1234567891",
  "transactionType": "WITHDRAWAL",
  "amount": 200.00,
  "description": "ATM withdrawal",
  "status": "COMPLETED",
  "accountId": 1,
  "accountNumber": "ACC1234567890",
  "balanceAfter": 1300.00,
  "transactionDate": "2025-08-12T11:15:00",
  "fee": 2.50
}
```

#### Business Logic
• Validates sufficient account balance
• Checks daily/monthly withdrawal limits
• Applies ATM or transaction fees if applicable
• Updates account balance
• Checks for overdraft usage
• Records transaction details
• Triggers low balance alerts if needed

---

### 3. **Transfer Money**
- **Endpoint**: `POST /transactions/transfer`
- **Purpose**: Transfer money between accounts
- **Access Level**: Protected
- **Required Role**: CUSTOMER, ADMIN, TELLER

#### Parameters
```json
{
  "fromAccountId": "number (required)",
  "toAccountId": "number (required)",
  "amount": "number (required, > 0)",
  "description": "string (optional)",
  "transferType": "INTERNAL|EXTERNAL",
  "recipientName": "string (required for external)"
}
```

#### Response
```json
{
  "id": 3,
  "transactionId": "TXN1234567892",
  "transactionType": "TRANSFER",
  "amount": 300.00,
  "description": "Monthly rent payment",
  "status": "COMPLETED",
  "fromAccount": {
    "accountId": 1,
    "accountNumber": "ACC1234567890",
    "balanceAfter": 1000.00
  },
  "toAccount": {
    "accountId": 2,
    "accountNumber": "ACC1234567891",
    "balanceAfter": 5300.00
  },
  "transactionDate": "2025-08-12T12:00:00",
  "fee": 0.00
}
```

#### Business Logic
• Validates both source and destination accounts
• Checks account ownership permissions
• Verifies sufficient balance in source account
• Applies transfer limits and restrictions
• Performs atomic debit/credit operations
• Handles external transfer processing
• Records dual transaction entries
• Sends transfer notifications to both parties

---

### 4. **Get User Transactions**
- **Endpoint**: `GET /transactions/user`
- **Purpose**: Retrieve transaction history for authenticated user
- **Access Level**: Protected
- **Required Role**: CUSTOMER, ADMIN, TELLER

#### Parameters
- **Query Parameters**:
  - `page`: number (optional, default: 0)
  - `size`: number (optional, default: 20)
  - `accountId`: number (optional, filter by account)
  - `transactionType`: string (optional, filter by type)
  - `startDate`: date (optional, filter by date range)
  - `endDate`: date (optional, filter by date range)

#### Response
```json
{
  "content": [
    {
      "id": 3,
      "transactionId": "TXN1234567892",
      "transactionType": "TRANSFER",
      "amount": 300.00,
      "description": "Monthly rent payment",
      "status": "COMPLETED",
      "accountNumber": "ACC1234567890",
      "transactionDate": "2025-08-12T12:00:00"
    }
  ],
  "totalElements": 25,
  "totalPages": 3,
  "size": 20,
  "number": 0,
  "first": true,
  "last": false
}
```

#### Business Logic
• Returns paginated transaction history
• Filters transactions by user ownership
• Applies date range and type filters
• Orders by transaction date (newest first)
• Includes transaction status and details
• TELLER can view customer transactions for support

---

### 5. **Get Transaction Details**
- **Endpoint**: `GET /transactions/{transactionId}`
- **Purpose**: Retrieve detailed information for specific transaction
- **Access Level**: Protected
- **Required Role**: CUSTOMER (own transactions), ADMIN, TELLER

#### Parameters
- **Path Variable**: `transactionId` (Long, required)

#### Response
```json
{
  "id": 3,
  "transactionId": "TXN1234567892",
  "transactionType": "TRANSFER",
  "amount": 300.00,
  "fee": 0.00,
  "description": "Monthly rent payment",
  "status": "COMPLETED",
  "referenceNumber": "REF789456123",
  "fromAccount": {
    "accountId": 1,
    "accountNumber": "ACC1234567890",
    "balanceBefore": 1300.00,
    "balanceAfter": 1000.00
  },
  "toAccount": {
    "accountId": 2,
    "accountNumber": "ACC1234567891",
    "balanceBefore": 5000.00,
    "balanceAfter": 5300.00
  },
  "transactionDate": "2025-08-12T12:00:00",
  "processedBy": "SYSTEM",
  "ipAddress": "192.168.1.100",
  "userAgent": "Mozilla/5.0...",
  "createdAt": "2025-08-12T12:00:00",
  "updatedAt": "2025-08-12T12:00:00"
}
```

#### Business Logic
• Validates transaction access permissions
• Returns complete transaction audit trail
• Includes balance changes and fees
• Shows processing details and metadata
• Provides fraud detection information

---

### 6. **Reverse Transaction**
- **Endpoint**: `POST /transactions/{transactionId}/reverse`
- **Purpose**: Reverse a completed transaction (admin/manager only)
- **Access Level**: Protected
- **Required Role**: ADMIN, MANAGER

#### Parameters
- **Path Variable**: `transactionId` (Long, required)
- **Request Body**:
```json
{
  "reason": "string (required, reversal reason)",
  "authorizedBy": "string (required, authorizing person)",
  "notifyCustomer": "boolean (optional, default: true)"
}
```

#### Response
```json
{
  "originalTransaction": {
    "id": 3,
    "transactionId": "TXN1234567892",
    "status": "REVERSED"
  },
  "reversalTransaction": {
    "id": 4,
    "transactionId": "TXN1234567893",
    "transactionType": "REVERSAL",
    "amount": 300.00,
    "status": "COMPLETED",
    "reversalReason": "Duplicate payment error",
    "authorizedBy": "MANAGER_001"
  },
  "message": "Transaction reversed successfully"
}
```

#### Business Logic
• Validates administrative authorization
• Checks if transaction is reversible
• Creates offsetting reversal transaction
• Restores account balances
• Records reversal reason and authorization
• Notifies affected customers
• Updates audit trail

---

### 7. **Schedule Recurring Transaction**
- **Endpoint**: `POST /transactions/schedule`
- **Purpose**: Set up recurring transactions (transfers, payments)
- **Access Level**: Protected
- **Required Role**: CUSTOMER, ADMIN, TELLER

#### Parameters
```json
{
  "fromAccountId": "number (required)",
  "toAccountId": "number (required)",
  "amount": "number (required, > 0)",
  "description": "string (required)",
  "frequency": "DAILY|WEEKLY|MONTHLY|QUARTERLY|YEARLY",
  "startDate": "date (required)",
  "endDate": "date (optional)",
  "maxOccurrences": "number (optional)"
}
```

#### Response
```json
{
  "scheduleId": 1,
  "fromAccountId": 1,
  "toAccountId": 2,
  "amount": 300.00,
  "description": "Monthly rent payment",
  "frequency": "MONTHLY",
  "startDate": "2025-09-01",
  "endDate": "2026-08-31",
  "nextExecutionDate": "2025-09-01",
  "maxOccurrences": 12,
  "isActive": true,
  "createdAt": "2025-08-12T13:00:00"
}
```

#### Business Logic
• Validates recurring transaction setup
• Calculates next execution dates
• Stores schedule configuration
• Performs initial validation checks
• Sets up automated processing
• Allows schedule modification/cancellation

---

### 8. **Get Transaction Statement**
- **Endpoint**: `GET /transactions/statement`
- **Purpose**: Generate account statement for specified period
- **Access Level**: Protected
- **Required Role**: CUSTOMER, ADMIN, TELLER

#### Parameters
- **Query Parameters**:
  - `accountId`: number (required)
  - `startDate`: date (required)
  - `endDate`: date (required)
  - `format`: string (optional, "PDF|CSV|JSON", default: "JSON")

#### Response
```json
{
  "accountNumber": "ACC1234567890",
  "accountType": "CHECKING",
  "statementPeriod": {
    "startDate": "2025-07-01",
    "endDate": "2025-07-31"
  },
  "openingBalance": 1200.00,
  "closingBalance": 1500.00,
  "totalDebits": 800.00,
  "totalCredits": 1100.00,
  "transactionCount": 15,
  "transactions": [
    {
      "date": "2025-07-31",
      "description": "Salary deposit",
      "debit": 0.00,
      "credit": 2500.00,
      "balance": 1500.00
    }
  ],
  "fees": 5.00,
  "interest": 1.25
}
```

#### Business Logic
• Validates account access permissions
• Calculates opening and closing balances
• Summarizes transaction activity
• Includes fees and interest calculations
• Generates downloadable statements
• Records statement generation activity

---

## 🔒 Security Features

### Transaction Limits
• **Daily Limits**: Configurable per account type
• **Monthly Limits**: Aggregate transaction limits
• **Single Transaction**: Maximum per transaction
• **Velocity Checks**: Rapid transaction detection

### Fraud Prevention
• **IP Monitoring**: Track transaction locations
• **Device Fingerprinting**: Detect unusual devices
• **Pattern Analysis**: Identify suspicious patterns
• **Real-time Alerts**: Immediate fraud notifications

### Audit Trail
• **Complete Logging**: Every transaction recorded
• **Immutable Records**: Transaction history protected
• **Access Tracking**: Who accessed what when
• **Compliance Reporting**: Regulatory requirement support

---

## 📊 Error Responses

### Insufficient Funds (400)
```json
{
  "status": 400,
  "error": "Insufficient Funds",
  "message": "Account balance insufficient for withdrawal",
  "details": {
    "availableBalance": 100.00,
    "requestedAmount": 500.00,
    "overdraftAvailable": 200.00
  }
}
```

### Transaction Limit Exceeded (400)
```json
{
  "status": 400,
  "error": "Limit Exceeded",
  "message": "Daily transaction limit exceeded",
  "details": {
    "dailyLimit": 5000.00,
    "usedToday": 4800.00,
    "requestedAmount": 500.00
  }
}
```

### Account Frozen (423)
```json
{
  "status": 423,
  "error": "Account Locked",
  "message": "Account is frozen - transactions not allowed"
}
```

---

## 🧪 Usage Examples

### Deposit Money
```bash
curl -X POST http://localhost:8080/api/v1/transactions/deposit \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${JWT_TOKEN}" \
  -d '{
    "accountId": 1,
    "amount": 500.00,
    "description": "Cash deposit",
    "depositMethod": "CASH"
  }'
```

### Transfer Money
```bash
curl -X POST http://localhost:8080/api/v1/transactions/transfer \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${JWT_TOKEN}" \
  -d '{
    "fromAccountId": 1,
    "toAccountId": 2,
    "amount": 300.00,
    "description": "Monthly savings transfer",
    "transferType": "INTERNAL"
  }'
```

### Get Transaction History
```bash
curl -X GET "http://localhost:8080/api/v1/transactions/user?page=0&size=10&accountId=1" \
  -H "Authorization: Bearer ${JWT_TOKEN}"
```

---

## 📈 Performance Considerations

• **Database Indexing**: Index on user_id, account_id, transaction_date
• **Pagination**: Limit large transaction history queries
• **Caching**: Cache frequent balance calculations
• **Batch Processing**: Process multiple transactions efficiently
• **Connection Pooling**: Optimize database connections for high volume
