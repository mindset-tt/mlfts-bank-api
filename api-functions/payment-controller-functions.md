# PaymentController Functions

## Overview

The PaymentController handles bill payments, money transfers, recurring payments, and payment processing operations.

**Base Path**: `/api/v1/payments`

---

## 💸 Functions List

### 1. **Create Payment**

- **Endpoint**: `POST /payments/create`
- **Purpose**: Create a new payment to payee
- **Access Level**: Protected
- **Required Role**: CUSTOMER, ADMIN, TELLER

#### Request Body

```json
{
  "sourceAccountId": "number (required)",
  "payeeId": "number (optional, for saved payees)",
  "payeeDetails": {
    "name": "string (required if no payeeId)",
    "accountNumber": "string (required if no payeeId)",
    "routingNumber": "string (optional)",
    "bankName": "string (optional)"
  },
  "amount": "number (required, > 0)",
  "paymentDate": "date (optional, default: today)",
  "description": "string (optional)",
  "paymentType": "BILL_PAY|TRANSFER|P2P|BUSINESS",
  "isRecurring": "boolean (default: false)",
  "recurringDetails": {
    "frequency": "WEEKLY|MONTHLY|QUARTERLY|YEARLY",
    "endDate": "date (optional)",
    "maxPayments": "number (optional)"
  }
}
```

#### Success Response

```json
{
  "id": 1,
  "paymentId": "PAY2025001234",
  "sourceAccountId": 1,
  "sourceAccountNumber": "ACC1234567890",
  "payeeId": 5,
  "payeeName": "Electric Company",
  "amount": 125.50,
  "paymentDate": "2025-08-15",
  "scheduledDate": "2025-08-15",
  "status": "SCHEDULED",
  "paymentType": "BILL_PAY",
  "description": "Monthly electric bill",
  "referenceNumber": "REF789123456",
  "fee": 0.00,
  "estimatedArrival": "2025-08-17",
  "confirmationNumber": "CONF2025001234",
  "createdAt": "2025-08-12T10:00:00"
}
```

#### Core Business Logic

• Validates source account ownership and balance
• Verifies payee information and account details
• Calculates applicable fees
• Schedules payment for specified date
• Generates unique payment reference
• Creates audit trail entry
• Sends payment confirmation notification

---

### 2. **Get User Payments**

- **Endpoint**: `GET /payments/user`
- **Purpose**: Retrieve payment history for authenticated user
- **Access Level**: Protected
- **Required Role**: CUSTOMER, ADMIN, TELLER

#### Query Options

- `page`: number (optional, default: 0)
- `size`: number (optional, default: 20)
- `status`: string (optional, filter by status)
- `paymentType`: string (optional, filter by type)
- `startDate`: date (optional, date range filter)
- `endDate`: date (optional, date range filter)
- `payeeId`: number (optional, filter by payee)

#### Paginated Response

```json
{
  "content": [
    {
      "id": 1,
      "paymentId": "PAY2025001234",
      "amount": 125.50,
      "payeeName": "Electric Company",
      "paymentDate": "2025-08-15",
      "status": "COMPLETED",
      "paymentType": "BILL_PAY",
      "description": "Monthly electric bill",
      "referenceNumber": "REF789123456"
    }
  ],
  "totalElements": 25,
  "totalPages": 2,
  "size": 20,
  "number": 0,
  "summary": {
    "totalPaid": 2456.78,
    "averagePayment": 98.27,
    "paymentCount": 25,
    "mostUsedPayee": "Electric Company"
  }
}
```

#### Core Business Logic

• Returns paginated payment history
• Filters by user ownership or TELLER access
• Applies date range and status filters
• Includes payment summary analytics
• Orders by payment date (newest first)

---

### 3. **Get Payment Details**

- **Endpoint**: `GET /payments/{paymentId}`
- **Purpose**: Retrieve detailed information for specific payment
- **Access Level**: Protected
- **Required Role**: CUSTOMER (own payments), ADMIN, TELLER

#### Path Parameter

- `paymentId` (Long, required)

#### Complete Details Response

```json
{
  "id": 1,
  "paymentId": "PAY2025001234",
  "sourceAccount": {
    "accountId": 1,
    "accountNumber": "ACC1234567890",
    "accountType": "CHECKING",
    "balanceBefore": 2500.00,
    "balanceAfter": 2374.50
  },
  "payee": {
    "payeeId": 5,
    "name": "Electric Company",
    "accountNumber": "ELEC987654321",
    "bankName": "Utility Bank",
    "payeeType": "UTILITY"
  },
  "paymentDetails": {
    "amount": 125.50,
    "fee": 0.00,
    "totalDebit": 125.50,
    "paymentDate": "2025-08-15",
    "scheduledDate": "2025-08-15",
    "processedDate": "2025-08-15T14:30:00",
    "estimatedArrival": "2025-08-17"
  },
  "status": "COMPLETED",
  "paymentType": "BILL_PAY",
  "description": "Monthly electric bill",
  "referenceNumber": "REF789123456",
  "confirmationNumber": "CONF2025001234",
  "trackingInfo": {
    "initiated": "2025-08-15T09:00:00",
    "processed": "2025-08-15T14:30:00",
    "sent": "2025-08-15T14:35:00",
    "delivered": "2025-08-17T10:15:00"
  },
  "isRecurring": false,
  "createdAt": "2025-08-12T10:00:00",
  "createdBy": "USER"
}
```

#### Core Business Logic

• Validates payment access permissions
• Returns complete payment audit trail
• Includes account balance changes
• Shows payment processing timeline
• Provides tracking information

---

### 4. **Cancel Payment**

- **Endpoint**: `POST /payments/{paymentId}/cancel`
- **Purpose**: Cancel a scheduled payment
- **Access Level**: Protected
- **Required Role**: CUSTOMER, ADMIN, TELLER

#### Path Parameter

- `paymentId` (Long, required)

#### Request Body

```json
{
  "reason": "string (required)",
  "notifyPayee": "boolean (optional, default: false)"
}
```

#### Cancellation Response

```json
{
  "paymentId": "PAY2025001234",
  "originalAmount": 125.50,
  "status": "CANCELLED",
  "cancellationDate": "2025-08-13T11:00:00",
  "reason": "Duplicate payment",
  "cancellationFee": 0.00,
  "refundAmount": 125.50,
  "refundDate": "2025-08-13",
  "message": "Payment cancelled successfully"
}
```

#### Core Business Logic

• Validates payment is cancellable (not yet processed)
• Checks cancellation cutoff times
• Reverses account debit if already processed
• Applies cancellation fees if applicable
• Records cancellation reason
• Notifies payee if requested

---

### 5. **Add Payee**

- **Endpoint**: `POST /payments/payees`
- **Purpose**: Add a new payee for future payments
- **Access Level**: Protected
- **Required Role**: CUSTOMER, ADMIN, TELLER

#### Request Body

```json
{
  "name": "string (required)",
  "nickname": "string (optional)",
  "accountNumber": "string (required)",
  "routingNumber": "string (optional)",
  "bankName": "string (optional)",
  "payeeType": "INDIVIDUAL|BUSINESS|UTILITY|GOVERNMENT",
  "address": {
    "street": "string (optional)",
    "city": "string (optional)",
    "state": "string (optional)",
    "zipCode": "string (optional)"
  },
  "defaultAmount": "number (optional)",
  "category": "string (optional)"
}
```

#### Payee Creation Response

```json
{
  "id": 6,
  "name": "Water Department",
  "nickname": "Water Bill",
  "accountNumber": "WATER123456789",
  "bankName": "Municipal Bank",
  "payeeType": "UTILITY",
  "address": {
    "street": "100 City Hall Plaza",
    "city": "New York",
    "state": "NY",
    "zipCode": "10007"
  },
  "defaultAmount": 75.00,
  "category": "Utilities",
  "isActive": true,
  "verificationStatus": "PENDING",
  "createdAt": "2025-08-12T11:00:00"
}
```

#### Core Business Logic

• Validates payee information format
• Performs duplicate payee check
• Verifies account number format
• Initiates payee verification process
• Adds to user's payee list
• Sends verification notification

---

### 6. **Get User Payees**

- **Endpoint**: `GET /payments/payees`
- **Purpose**: Retrieve all saved payees for user
- **Access Level**: Protected
- **Required Role**: CUSTOMER, ADMIN, TELLER

#### Query Options

- `page`: number (optional, default: 0)
- `size`: number (optional, default: 50)
- `category`: string (optional, filter by category)
- `payeeType`: string (optional, filter by type)
- `isActive`: boolean (optional, default: true)

#### Payees List Response

```json
{
  "content": [
    {
      "id": 5,
      "name": "Electric Company",
      "nickname": "Electric Bill",
      "payeeType": "UTILITY",
      "category": "Utilities",
      "defaultAmount": 125.00,
      "lastPaymentDate": "2025-07-15",
      "lastPaymentAmount": 125.50,
      "totalPayments": 12,
      "isActive": true,
      "verificationStatus": "VERIFIED"
    }
  ],
  "totalElements": 8,
  "totalPages": 1,
  "size": 50,
  "number": 0
}
```

#### Core Business Logic

• Returns user's saved payees
• Filters by category and type
• Includes payment history summary
• Shows verification status
• Orders by frequency of use

---

### 7. **Schedule Recurring Payment**

- **Endpoint**: `POST /payments/recurring`
- **Purpose**: Set up recurring payment schedule
- **Access Level**: Protected
- **Required Role**: CUSTOMER, ADMIN, TELLER

#### Request Body

```json
{
  "sourceAccountId": "number (required)",
  "payeeId": "number (required)",
  "amount": "number (required, > 0)",
  "frequency": "WEEKLY|MONTHLY|QUARTERLY|YEARLY",
  "startDate": "date (required)",
  "endDate": "date (optional)",
  "maxPayments": "number (optional)",
  "description": "string (optional)",
  "autoAdjustForHolidays": "boolean (default: true)"
}
```

#### Recurring Setup Response

```json
{
  "recurringPaymentId": 1,
  "sourceAccountId": 1,
  "payeeId": 5,
  "payeeName": "Electric Company",
  "amount": 125.00,
  "frequency": "MONTHLY",
  "startDate": "2025-09-01",
  "endDate": "2026-08-31",
  "maxPayments": 12,
  "nextPaymentDate": "2025-09-01",
  "remainingPayments": 12,
  "isActive": true,
  "autoAdjustForHolidays": true,
  "description": "Monthly electric bill autopay",
  "createdAt": "2025-08-12T12:00:00",
  "estimatedAnnualTotal": 1500.00
}
```

#### Core Business Logic

• Validates recurring payment setup
• Calculates payment schedule dates
• Handles holiday adjustments
• Sets up automated processing
• Allows modification and cancellation
• Sends setup confirmation

---

### 8. **Process Payment**

- **Endpoint**: `POST /payments/{paymentId}/process`
- **Purpose**: Manually process a scheduled payment
- **Access Level**: Protected
- **Required Role**: ADMIN, TELLER

#### Path Parameter

- `paymentId` (Long, required)

#### Request Body

```json
{
  "processImmediately": "boolean (default: false)",
  "overrideHolds": "boolean (default: false)",
  "authorizedBy": "string (required)"
}
```

#### Processing Response

```json
{
  "paymentId": "PAY2025001234",
  "previousStatus": "SCHEDULED",
  "currentStatus": "PROCESSING",
  "processedDate": "2025-08-12T15:00:00",
  "processedBy": "TELLER_001",
  "transactionId": "TXN1234567890",
  "estimatedCompletion": "2025-08-14T17:00:00",
  "trackingNumber": "TRACK789456123",
  "message": "Payment processing initiated"
}
```

#### Core Business Logic

• Validates payment processing authorization
• Checks account balance and holds
• Initiates payment processing workflow
• Generates tracking information
• Updates payment status
• Records processing audit trail

---

## 🔒 Security Features

### Payment Authorization

• **Multi-Factor Authentication**: SMS/app verification for large payments
• **Transaction Limits**: Daily/monthly payment limits
• **Velocity Monitoring**: Rapid payment detection
• **Fraud Scoring**: AI-based risk assessment

### Data Protection

• **Account Encryption**: Payee account number encryption
• **PII Security**: Personal information protection
• **Audit Logging**: Complete payment audit trail
• **Access Controls**: Role-based payment access

### Compliance

• **BSA Reporting**: Bank Secrecy Act compliance
• **CTR Filing**: Currency Transaction Reports
• **SAR Generation**: Suspicious Activity Reports
• **Reg E Protection**: Electronic Fund Transfer regulations

---

## 📊 Error Responses

### Insufficient Funds (400)

```json
{
  "status": 400,
  "error": "Insufficient Funds",
  "message": "Account balance insufficient for payment",
  "details": {
    "availableBalance": 100.00,
    "paymentAmount": 125.50,
    "accountId": 1
  }
}
```

### Payment Limit Exceeded (400)

```json
{
  "status": 400,
  "error": "Limit Exceeded",
  "message": "Daily payment limit exceeded",
  "details": {
    "dailyLimit": 5000.00,
    "usedToday": 4900.00,
    "requestedAmount": 125.50
  }
}
```

### Payee Not Found (404)

```json
{
  "status": 404,
  "error": "Payee Not Found",
  "message": "Payee with ID 999 not found or access denied"
}
```

### Payment Already Processed (409)

```json
{
  "status": 409,
  "error": "Payment Already Processed",
  "message": "Payment cannot be modified - already processed",
  "details": {
    "paymentId": "PAY2025001234",
    "status": "COMPLETED",
    "processedDate": "2025-08-12T10:00:00"
  }
}
```

---

## 🧪 Usage Examples

### Create Bill Payment

```bash
curl -X POST http://localhost:8080/api/v1/payments/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${JWT_TOKEN}" \
  -d '{
    "sourceAccountId": 1,
    "payeeId": 5,
    "amount": 125.50,
    "paymentDate": "2025-08-15",
    "description": "Monthly electric bill",
    "paymentType": "BILL_PAY"
  }'
```

### Add New Payee

```bash
curl -X POST http://localhost:8080/api/v1/payments/payees \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${JWT_TOKEN}" \
  -d '{
    "name": "Water Department",
    "nickname": "Water Bill",
    "accountNumber": "WATER123456789",
    "payeeType": "UTILITY",
    "defaultAmount": 75.00,
    "category": "Utilities"
  }'
```

### Set Up Recurring Payment

```bash
curl -X POST http://localhost:8080/api/v1/payments/recurring \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${JWT_TOKEN}" \
  -d '{
    "sourceAccountId": 1,
    "payeeId": 5,
    "amount": 125.00,
    "frequency": "MONTHLY",
    "startDate": "2025-09-01",
    "description": "Monthly electric bill autopay"
  }'
```

### Cancel Payment

```bash
curl -X POST http://localhost:8080/api/v1/payments/1/cancel \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${JWT_TOKEN}" \
  -d '{
    "reason": "Duplicate payment scheduled",
    "notifyPayee": false
  }'
```

---

## 📈 Performance Considerations

• **Payment Processing**: Batch processing for efficiency
• **Database Indexing**: Index on user_id, payee_id, payment_date, status
• **Caching Strategy**: Cache frequently used payees
• **Rate Limiting**: Prevent payment spam and abuse
• **Queue Management**: Asynchronous payment processing for high volume
