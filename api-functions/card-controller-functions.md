# CardController Functions

## Overview

The CardController manages credit/debit card operations including card issuance, activation, transactions, and account management.

**Base Path**: `/api/v1/cards`

---

## 💳 Functions List

### 1. **Request New Card**

- **Endpoint**: `POST /cards/request`
- **Purpose**: Request a new credit or debit card
- **Access Level**: Protected
- **Required Role**: CUSTOMER, ADMIN, TELLER

#### Request Parameters

```json
{
  "cardType": "DEBIT|CREDIT",
  "accountId": "number (required for debit cards)",
  "creditLimit": "number (optional, for credit cards)",
  "deliveryAddress": {
    "street": "string (required)",
    "city": "string (required)",
    "state": "string (required)",
    "zipCode": "string (required)",
    "country": "string (required)"
  },
  "expeditedDelivery": "boolean (optional, default: false)"
}
```

#### Response Data

```json
{
  "id": 1,
  "cardRequestId": "CARD2025001",
  "cardType": "DEBIT",
  "accountId": 1,
  "accountNumber": "ACC1234567890",
  "status": "PENDING_APPROVAL",
  "requestDate": "2025-08-12T10:00:00",
  "estimatedDelivery": "2025-08-20",
  "deliveryAddress": {
    "street": "123 Main Street",
    "city": "New York",
    "state": "NY",
    "zipCode": "10001",
    "country": "USA"
  },
  "trackingInfo": "Will be provided upon card production"
}
```

#### Implementation Logic

• Validates account ownership and eligibility
• Performs credit check for credit cards
• Verifies delivery address
• Generates card request ID
• Initiates card production process
• Calculates delivery timeline
• Sends confirmation notification

---

### 2. **Get User Cards**

- **Endpoint**: `GET /cards/user`
- **Purpose**: Retrieve all cards for authenticated user
- **Access Level**: Protected
- **Required Role**: CUSTOMER, ADMIN, TELLER

#### Query Parameters

- `page`: number (optional, default: 0)
- `size`: number (optional, default: 20)
- `cardType`: string (optional, filter by type)
- `status`: string (optional, filter by status)
- `includeInactive`: boolean (optional, default: false)

#### Response Data

```json
{
  "content": [
    {
      "id": 1,
      "cardNumber": "****-****-****-1234",
      "cardType": "DEBIT",
      "cardBrand": "VISA",
      "accountId": 1,
      "status": "ACTIVE",
      "expiryDate": "2027-08",
      "issueDate": "2025-08-12",
      "cardHolderName": "JOHN DOE",
      "creditLimit": null,
      "availableCredit": null,
      "lastUsed": "2025-08-10T14:30:00"
    }
  ],
  "totalElements": 2,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

#### Implementation Logic

• Returns paginated card list
• Masks sensitive card information
• Filters by user ownership or TELLER access
• Includes card status and usage information
• Shows available credit for credit cards

---

### 3. **Get Card Details**

- **Endpoint**: `GET /cards/{cardId}`
- **Purpose**: Retrieve detailed information for specific card
- **Access Level**: Protected
- **Required Role**: CUSTOMER (own cards), ADMIN, TELLER

#### Path Variable

- `cardId` (Long, required)

#### Response Data

```json
{
  "id": 1,
  "cardNumber": "****-****-****-1234",
  "fullCardNumber": "4532-1234-5678-1234",
  "cardType": "DEBIT",
  "cardBrand": "VISA",
  "accountId": 1,
  "accountNumber": "ACC1234567890",
  "cardHolderName": "JOHN DOE",
  "status": "ACTIVE",
  "issueDate": "2025-08-12",
  "expiryDate": "2027-08",
  "cvv": "123",
  "pin": "****",
  "creditLimit": null,
  "availableCredit": null,
  "monthlySpending": 1250.00,
  "lastTransaction": {
    "amount": 45.67,
    "merchant": "Coffee Shop",
    "date": "2025-08-10T14:30:00",
    "location": "New York, NY"
  },
  "securitySettings": {
    "contactlessEnabled": true,
    "internationalTransactions": false,
    "onlineTransactions": true,
    "atmWithdrawals": true,
    "dailySpendingLimit": 2000.00
  }
}
```

#### Implementation Logic

• Validates card access permissions
• Returns sensitive information securely
• Includes recent transaction summary
• Shows security and limit settings
• Provides card usage analytics

---

### 4. **Activate Card**

- **Endpoint**: `POST /cards/{cardId}/activate`
- **Purpose**: Activate a newly issued card
- **Access Level**: Protected
- **Required Role**: CUSTOMER, ADMIN, TELLER

#### Path Variable

- `cardId` (Long, required)

#### Request Parameters

```json
{
  "activationCode": "string (required)",
  "newPin": "string (required, 4 digits)",
  "confirmPin": "string (required, must match newPin)"
}
```

#### Response Data

```json
{
  "cardId": 1,
  "cardNumber": "****-****-****-1234",
  "status": "ACTIVE",
  "activationDate": "2025-08-12T15:00:00",
  "message": "Card activated successfully",
  "securityFeatures": {
    "chipEnabled": true,
    "contactlessEnabled": true,
    "pinSet": true
  },
  "usageInstructions": [
    "Use your PIN for ATM transactions",
    "Card can be used immediately for purchases",
    "Enable notifications in mobile app"
  ]
}
```

#### Implementation Logic

• Validates activation code and card status
• Encrypts and stores PIN securely
• Updates card status to active
• Enables security features
• Sends activation confirmation
• Triggers welcome messaging

---

### 5. **Block/Unblock Card**

- **Endpoint**: `POST /cards/{cardId}/block`
- **Purpose**: Block or unblock a card for security
- **Access Level**: Protected
- **Required Role**: CUSTOMER, ADMIN, TELLER

#### Path Variable

- `cardId` (Long, required)

#### Request Parameters

```json
{
  "action": "BLOCK|UNBLOCK",
  "reason": "LOST|STOLEN|SUSPICIOUS_ACTIVITY|TEMPORARY_BLOCK|CUSTOMER_REQUEST",
  "comment": "string (optional)"
}
```

#### Response Data

```json
{
  "cardId": 1,
  "cardNumber": "****-****-****-1234",
  "previousStatus": "ACTIVE",
  "currentStatus": "BLOCKED",
  "action": "BLOCK",
  "reason": "LOST",
  "actionDate": "2025-08-12T16:00:00",
  "actionBy": "CUSTOMER",
  "message": "Card has been blocked successfully",
  "replacementCard": {
    "requestId": "CARD2025002",
    "estimatedDelivery": "2025-08-18",
    "expeditedAvailable": true
  }
}
```

#### Implementation Logic

• Validates card ownership and current status
• Immediately blocks/unblocks card transactions
• Records security action in audit log
• Offers replacement card if blocked permanently
• Notifies fraud prevention system
• Sends status change confirmation

---

### 6. **Update Card Settings**

- **Endpoint**: `PUT /cards/{cardId}/settings`
- **Purpose**: Update card security and usage settings
- **Access Level**: Protected
- **Required Role**: CUSTOMER, ADMIN, TELLER

#### Path Variable

- `cardId` (Long, required)

#### Request Parameters

```json
{
  "contactlessEnabled": "boolean (optional)",
  "internationalTransactions": "boolean (optional)",
  "onlineTransactions": "boolean (optional)",
  "atmWithdrawals": "boolean (optional)",
  "dailySpendingLimit": "number (optional)",
  "monthlySpendingLimit": "number (optional)",
  "notifications": {
    "transactionAlerts": "boolean (optional)",
    "largeTransactionThreshold": "number (optional)",
    "internationalUsageAlerts": "boolean (optional)"
  }
}
```

#### Response Data

```json
{
  "cardId": 1,
  "cardNumber": "****-****-****-1234",
  "updatedSettings": {
    "contactlessEnabled": true,
    "internationalTransactions": false,
    "onlineTransactions": true,
    "atmWithdrawals": true,
    "dailySpendingLimit": 1500.00,
    "monthlySpendingLimit": 10000.00,
    "notifications": {
      "transactionAlerts": true,
      "largeTransactionThreshold": 100.00,
      "internationalUsageAlerts": true
    }
  },
  "updateDate": "2025-08-12T17:00:00",
  "message": "Card settings updated successfully"
}
```

#### Implementation Logic

• Validates card ownership and status
• Updates security and spending controls
• Applies new limits immediately
• Records settings changes in audit log
• Enables/disables transaction types
• Configures notification preferences

---

### 7. **Get Card Transactions**

- **Endpoint**: `GET /cards/{cardId}/transactions`
- **Purpose**: Retrieve transaction history for specific card
- **Access Level**: Protected
- **Required Role**: CUSTOMER (own cards), ADMIN, TELLER

#### Path Variable

- `cardId` (Long, required)

#### Query Parameters

- `page`: number (optional, default: 0)
- `size`: number (optional, default: 20)
- `startDate`: date (optional)
- `endDate`: date (optional)
- `transactionType`: string (optional)
- `minAmount`: number (optional)
- `maxAmount`: number (optional)

#### Response Data

```json
{
  "content": [
    {
      "id": 1,
      "transactionId": "TXN1234567890",
      "cardNumber": "****-****-****-1234",
      "amount": 45.67,
      "transactionType": "PURCHASE",
      "merchantName": "Starbucks Coffee",
      "merchantCategory": "Food & Dining",
      "location": "New York, NY",
      "transactionDate": "2025-08-10T14:30:00",
      "status": "COMPLETED",
      "authorizationCode": "123456",
      "isContactless": false,
      "isInternational": false
    }
  ],
  "totalElements": 45,
  "totalPages": 3,
  "size": 20,
  "number": 0,
  "summary": {
    "totalSpending": 2456.78,
    "averageTransaction": 54.59,
    "largestTransaction": 459.99,
    "transactionCount": 45
  }
}
```

#### Implementation Logic

• Returns paginated transaction history
• Filters by date range and amount
• Includes merchant and location information
• Shows transaction status and method
• Provides spending analytics summary

---

### 8. **Change Card PIN**

- **Endpoint**: `POST /cards/{cardId}/change-pin`
- **Purpose**: Change card PIN for ATM and chip transactions
- **Access Level**: Protected
- **Required Role**: CUSTOMER, ADMIN, TELLER

#### Path Variable

- `cardId` (Long, required)

#### Request Parameters

```json
{
  "currentPin": "string (required, 4 digits)",
  "newPin": "string (required, 4 digits)",
  "confirmPin": "string (required, must match newPin)"
}
```

#### Response Data

```json
{
  "cardId": 1,
  "cardNumber": "****-****-****-1234",
  "pinChangeDate": "2025-08-12T18:00:00",
  "status": "SUCCESS",
  "message": "PIN changed successfully",
  "securityNote": "Your new PIN is now active for all transactions"
}
```

#### Implementation Logic

• Validates current PIN securely
• Ensures new PIN meets security requirements
• Encrypts and stores new PIN
• Invalidates previous PIN immediately
• Records PIN change in security log
• Sends confirmation notification

---

## 🔒 Security Features

### Card Protection

• **Real-time Fraud Detection**: AI-powered transaction monitoring
• **Geo-location Tracking**: Unusual location alerts
• **Velocity Checks**: Multiple transaction detection
• **EMV Chip Technology**: Secure chip-based transactions

### Access Control

• **PIN Encryption**: Military-grade PIN storage
• **CVV Validation**: Dynamic CVV verification
• **Biometric Authentication**: Fingerprint/face recognition
• **Two-Factor Authentication**: SMS/app verification

### Compliance

• **PCI DSS Compliance**: Payment card industry standards
• **Data Encryption**: End-to-end encryption
• **Audit Trail**: Complete transaction logging
• **Regulatory Reporting**: Automated compliance reports

---

## 📊 Error Responses

### Card Not Found (404)

```json
{
  "status": 404,
  "error": "Card Not Found",
  "message": "Card with ID 999 not found or access denied"
}
```

### Invalid PIN (400)

```json
{
  "status": 400,
  "error": "Invalid PIN",
  "message": "Current PIN is incorrect",
  "details": {
    "attemptsRemaining": 2,
    "lockoutTime": "24 hours"
  }
}
```

### Card Blocked (423)

```json
{
  "status": 423,
  "error": "Card Blocked",
  "message": "Card is currently blocked and cannot be used",
  "details": {
    "blockReason": "SUSPICIOUS_ACTIVITY",
    "blockDate": "2025-08-12T10:00:00",
    "contactSupport": "+1-800-123-4567"
  }
}
```

### Spending Limit Exceeded (400)

```json
{
  "status": 400,
  "error": "Limit Exceeded",
  "message": "Daily spending limit exceeded",
  "details": {
    "dailyLimit": 2000.00,
    "usedToday": 1950.00,
    "requestedAmount": 100.00
  }
}
```

---

## 🧪 Usage Examples

### Request New Debit Card

```bash
curl -X POST http://localhost:8080/api/v1/cards/request \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${JWT_TOKEN}" \
  -d '{
    "cardType": "DEBIT",
    "accountId": 1,
    "deliveryAddress": {
      "street": "123 Main Street",
      "city": "New York",
      "state": "NY",
      "zipCode": "10001",
      "country": "USA"
    }
  }'
```

### Activate Card

```bash
curl -X POST http://localhost:8080/api/v1/cards/1/activate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${JWT_TOKEN}" \
  -d '{
    "activationCode": "ABC123456",
    "newPin": "1234",
    "confirmPin": "1234"
  }'
```

### Block Card

```bash
curl -X POST http://localhost:8080/api/v1/cards/1/block \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${JWT_TOKEN}" \
  -d '{
    "action": "BLOCK",
    "reason": "LOST",
    "comment": "Lost wallet at airport"
  }'
```

### Update Card Settings

```bash
curl -X PUT http://localhost:8080/api/v1/cards/1/settings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${JWT_TOKEN}" \
  -d '{
    "contactlessEnabled": true,
    "internationalTransactions": false,
    "dailySpendingLimit": 1500.00,
    "notifications": {
      "transactionAlerts": true,
      "largeTransactionThreshold": 100.00
    }
  }'
```

---

## 📈 Performance Considerations

• **Transaction Processing**: Real-time authorization within 2 seconds
• **Fraud Detection**: ML-based risk scoring
• **Database Indexing**: Index on card_number, user_id, transaction_date
• **Caching Strategy**: Cache card settings and limits
• **API Rate Limiting**: Prevent abuse and ensure availability
