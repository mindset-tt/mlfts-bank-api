# LoanController Functions

## Overview

The LoanController manages loan operations including applications, approvals, payments, and loan account management.

**Base Path**: `/api/v1/loans`

---

## 💼 Functions List

### 1. **Apply for Loan**

- **Endpoint**: `POST /loans/apply`
- **Purpose**: Submit a new loan application
- **Access Level**: Protected
- **Required Role**: CUSTOMER, ADMIN, TELLER

#### Parameters

```json
{
  "loanType": "PERSONAL|MORTGAGE|AUTO|BUSINESS|STUDENT",
  "loanAmount": "number (required, > 0)",
  "loanPurpose": "string (required)",
  "employmentStatus": "EMPLOYED|SELF_EMPLOYED|UNEMPLOYED|RETIRED",
  "annualIncome": "number (required)",
  "creditScore": "number (optional)",
  "collateralValue": "number (optional)",
  "requestedTerm": "number (required, months)",
  "documents": ["array of document IDs"]
}
```

#### Response

```json
{
  "id": 1,
  "loanApplicationId": "LOAN2025001",
  "applicantId": 1,
  "loanType": "PERSONAL",
  "loanAmount": 50000.00,
  "requestedTerm": 36,
  "loanPurpose": "Home renovation",
  "status": "PENDING_REVIEW",
  "applicationDate": "2025-08-12T14:30:00",
  "expectedProcessingTime": "3-5 business days",
  "requiredDocuments": [
    "Income proof",
    "Employment letter",
    "Bank statements"
  ],
  "nextSteps": "Your application is under review. We will contact you within 2 business days."
}
```

#### Business Logic

• Validates loan eligibility criteria
• Performs initial credit assessment
• Calculates preliminary loan terms
• Assigns application to loan officer
• Generates application reference number
• Triggers document verification process
• Sends application confirmation notification

---

### 2. **Get User Loans**

- **Endpoint**: `GET /loans/user`
- **Purpose**: Retrieve all loans for authenticated user
- **Access Level**: Protected
- **Required Role**: CUSTOMER, ADMIN, TELLER, LOAN_OFFICER

#### Parameters

- **Query Parameters**:
  - `page`: number (optional, default: 0)
  - `size`: number (optional, default: 20)
  - `status`: string (optional, filter by status)
  - `loanType`: string (optional, filter by type)

#### Response

```json
{
  "content": [
    {
      "id": 1,
      "loanApplicationId": "LOAN2025001",
      "loanType": "PERSONAL",
      "loanAmount": 50000.00,
      "approvedAmount": 45000.00,
      "currentBalance": 38000.00,
      "status": "ACTIVE",
      "interestRate": 8.5,
      "monthlyPayment": 1456.32,
      "nextPaymentDate": "2025-09-15",
      "remainingPayments": 24,
      "applicationDate": "2025-08-12",
      "approvalDate": "2025-08-15"
    }
  ],
  "totalElements": 3,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

#### Business Logic

• Returns paginated loan list
• Filters by user ownership or TELLER access
• Includes current balance and payment status
• Shows upcoming payment information
• Calculates loan progress metrics

---

### 3. **Get Loan Details**

- **Endpoint**: `GET /loans/{loanId}`
- **Purpose**: Retrieve detailed information for specific loan
- **Access Level**: Protected
- **Required Role**: CUSTOMER (own loans), ADMIN, TELLER, LOAN_OFFICER

#### Parameters

- **Path Variable**: `loanId` (Long, required)

#### Response

```json
{
  "id": 1,
  "loanApplicationId": "LOAN2025001",
  "applicantDetails": {
    "userId": 1,
    "fullName": "John Doe",
    "email": "john.doe@example.com",
    "phone": "+1234567890"
  },
  "loanDetails": {
    "loanType": "PERSONAL",
    "loanAmount": 50000.00,
    "approvedAmount": 45000.00,
    "currentBalance": 38000.00,
    "interestRate": 8.5,
    "termMonths": 36,
    "remainingPayments": 24,
    "monthlyPayment": 1456.32,
    "nextPaymentDate": "2025-09-15",
    "status": "ACTIVE"
  },
  "paymentHistory": [
    {
      "paymentDate": "2025-08-15",
      "amount": 1456.32,
      "principal": 1156.32,
      "interest": 300.00,
      "remainingBalance": 38000.00
    }
  ],
  "collateral": {
    "type": "Property",
    "value": 80000.00,
    "description": "Residential property at 123 Main St"
  },
  "loanOfficer": {
    "id": 5,
    "name": "Jane Smith",
    "email": "jane.smith@bank.com",
    "phone": "+1234567891"
  }
}
```

#### Business Logic

• Validates loan access permissions
• Returns comprehensive loan information
• Includes payment history and schedule
• Shows collateral and guarantor details
• Provides loan officer contact information

---

### 4. **Make Loan Payment**

- **Endpoint**: `POST /loans/{loanId}/payment`
- **Purpose**: Make a payment towards loan
- **Access Level**: Protected
- **Required Role**: CUSTOMER, ADMIN, TELLER

#### Parameters

- **Path Variable**: `loanId` (Long, required)
- **Request Body**:

```json
{
  "paymentAmount": "number (required, > 0)",
  "paymentMethod": "AUTO_DEBIT|MANUAL|CASH|CHECK|ONLINE",
  "sourceAccountId": "number (optional, for auto-debit)",
  "paymentType": "REGULAR|EXTRA_PRINCIPAL|FULL_PAYOFF"
}
```

#### Response

```json
{
  "paymentId": 1,
  "loanId": 1,
  "paymentAmount": 1456.32,
  "principalAmount": 1156.32,
  "interestAmount": 300.00,
  "paymentDate": "2025-08-15T10:00:00",
  "paymentMethod": "AUTO_DEBIT",
  "newBalance": 36843.68,
  "nextPaymentDate": "2025-09-15",
  "remainingPayments": 23,
  "receiptNumber": "PAY2025001234",
  "status": "PROCESSED"
}
```

#### Business Logic

• Validates loan status and payment eligibility
• Calculates principal and interest allocation
• Updates loan balance and payment schedule
• Processes automatic debit if configured
• Generates payment receipt
• Updates credit reporting
• Sends payment confirmation

---

### 5. **Get Payment Schedule**

- **Endpoint**: `GET /loans/{loanId}/schedule`
- **Purpose**: Retrieve payment schedule for loan
- **Access Level**: Protected
- **Required Role**: CUSTOMER (own loans), ADMIN, TELLER, LOAN_OFFICER

#### Parameters

- **Path Variable**: `loanId` (Long, required)
- **Query Parameters**:
  - `showPaid`: boolean (optional, default: false)
  - `monthsAhead`: number (optional, default: 12)

#### Response

```json
{
  "loanId": 1,
  "totalPayments": 36,
  "remainingPayments": 24,
  "currentBalance": 38000.00,
  "totalInterest": 2428.52,
  "schedule": [
    {
      "paymentNumber": 13,
      "dueDate": "2025-09-15",
      "paymentAmount": 1456.32,
      "principalAmount": 1156.32,
      "interestAmount": 300.00,
      "remainingBalance": 36843.68,
      "status": "UPCOMING"
    },
    {
      "paymentNumber": 14,
      "dueDate": "2025-10-15",
      "paymentAmount": 1456.32,
      "principalAmount": 1164.58,
      "interestAmount": 291.74,
      "remainingBalance": 35679.10,
      "status": "SCHEDULED"
    }
  ],
  "summaryStats": {
    "totalPaid": 17000.00,
    "totalPrincipalPaid": 12000.00,
    "totalInterestPaid": 5000.00,
    "percentComplete": 33.3
  }
}
```

#### Business Logic

• Calculates amortization schedule
• Shows paid vs upcoming payments
• Includes principal/interest breakdown
• Provides loan progress statistics
• Handles variable rate adjustments

---

### 6. **Approve/Reject Loan**

- **Endpoint**: `POST /loans/{loanId}/decision`
- **Purpose**: Approve or reject loan application
- **Access Level**: Protected
- **Required Role**: ADMIN, LOAN_OFFICER, MANAGER

#### Parameters

- **Path Variable**: `loanId` (Long, required)
- **Request Body**:

```json
{
  "decision": "APPROVE|REJECT|REQUEST_MORE_INFO",
  "approvedAmount": "number (required if approved)",
  "interestRate": "number (required if approved)",
  "termMonths": "number (required if approved)",
  "conditions": ["array of approval conditions"],
  "rejectionReason": "string (required if rejected)",
  "comments": "string (optional)"
}
```

#### Response

```json
{
  "loanId": 1,
  "decision": "APPROVE",
  "approvedAmount": 45000.00,
  "interestRate": 8.5,
  "termMonths": 36,
  "monthlyPayment": 1456.32,
  "conditions": [
    "Property insurance required",
    "Income verification quarterly"
  ],
  "approvalDate": "2025-08-15T09:00:00",
  "approvedBy": "LOAN_OFFICER_001",
  "disbursementDate": "2025-08-20",
  "firstPaymentDate": "2025-09-15"
}
```

#### Business Logic

• Validates loan officer authorization
• Performs final credit and risk assessment
• Calculates loan terms and payments
• Updates loan status and records decision
• Generates loan agreement documents
• Schedules disbursement process
• Notifies applicant of decision

---

### 7. **Set Up Auto Payment**

- **Endpoint**: `POST /loans/{loanId}/auto-payment`
- **Purpose**: Configure automatic loan payments
- **Access Level**: Protected
- **Required Role**: CUSTOMER, ADMIN, TELLER

#### Parameters

- **Path Variable**: `loanId` (Long, required)
- **Request Body**:

```json
{
  "sourceAccountId": "number (required)",
  "paymentAmount": "number (required)",
  "paymentDay": "number (1-28, required)",
  "isActive": "boolean (default: true)"
}
```

#### Response

```json
{
  "autoPaymentId": 1,
  "loanId": 1,
  "sourceAccountId": 2,
  "paymentAmount": 1456.32,
  "paymentDay": 15,
  "nextPaymentDate": "2025-09-15",
  "isActive": true,
  "setupDate": "2025-08-12T15:00:00",
  "status": "ACTIVE"
}
```

#### Business Logic

• Validates source account ownership
• Ensures sufficient recurring balance
• Configures automatic payment schedule
• Sets up payment processing triggers
• Provides payment failure handling
• Allows modification and cancellation

---

### 8. **Calculate Loan Payment**

- **Endpoint**: `POST /loans/calculate`
- **Purpose**: Calculate loan payment estimates
- **Access Level**: Public
- **Required Role**: None

#### Parameters

```json
{
  "loanAmount": "number (required, > 0)",
  "interestRate": "number (required, > 0)",
  "termMonths": "number (required, > 0)",
  "loanType": "PERSONAL|MORTGAGE|AUTO|BUSINESS|STUDENT",
  "downPayment": "number (optional)"
}
```

#### Response

```json
{
  "loanAmount": 50000.00,
  "downPayment": 5000.00,
  "financedAmount": 45000.00,
  "interestRate": 8.5,
  "termMonths": 36,
  "monthlyPayment": 1456.32,
  "totalPayments": 52427.52,
  "totalInterest": 7427.52,
  "annualPayments": 17475.84,
  "payoffDate": "2028-08-15",
  "amortizationSample": [
    {
      "month": 1,
      "payment": 1456.32,
      "principal": 1137.50,
      "interest": 318.82,
      "balance": 43862.50
    }
  ]
}
```

#### Business Logic

• Calculates loan payment using amortization formula
• Includes total cost of loan
• Provides sample amortization schedule
• Considers loan type specific rates
• Handles down payment calculations

---

## 🔒 Security Features

### Loan Authorization

• **Multi-level Approval**: Different limits for different roles
• **Credit Verification**: Integrated credit bureau checks
• **Income Verification**: Automated income validation
• **Collateral Assessment**: Professional valuation requirements

### Risk Management

• **Debt-to-Income Ratio**: Automated DTI calculations
• **Credit Score Requirements**: Minimum score thresholds
• **Loan-to-Value Limits**: Maximum LTV ratios
• **Employment Verification**: Income stability checks

### Compliance

• **Regulatory Reporting**: Automated compliance reporting
• **Interest Rate Limits**: Legal rate compliance
• **Documentation Requirements**: Complete audit trail
• **Privacy Protection**: Sensitive data encryption

---

## 📊 Error Responses

### Insufficient Credit Score (400)

```json
{
  "status": 400,
  "error": "Credit Score Insufficient",
  "message": "Credit score does not meet minimum requirements",
  "details": {
    "currentScore": 580,
    "minimumRequired": 650,
    "loanType": "PERSONAL"
  }
}
```

### Loan Not Found (404)

```json
{
  "status": 404,
  "error": "Loan Not Found",
  "message": "Loan with ID 999 not found or access denied"
}
```

### Payment Failed (400)

```json
{
  "status": 400,
  "error": "Payment Failed",
  "message": "Insufficient funds in source account",
  "details": {
    "paymentAmount": 1456.32,
    "availableBalance": 1200.00,
    "accountId": 2
  }
}
```

---

## 🧪 Usage Examples

### Apply for Personal Loan

```bash
curl -X POST http://localhost:8080/api/v1/loans/apply \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${JWT_TOKEN}" \
  -d '{
    "loanType": "PERSONAL",
    "loanAmount": 50000.00,
    "loanPurpose": "Home renovation",
    "employmentStatus": "EMPLOYED",
    "annualIncome": 75000.00,
    "requestedTerm": 36
  }'
```

### Make Loan Payment

```bash
curl -X POST http://localhost:8080/api/v1/loans/1/payment \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${JWT_TOKEN}" \
  -d '{
    "paymentAmount": 1456.32,
    "paymentMethod": "AUTO_DEBIT",
    "sourceAccountId": 2,
    "paymentType": "REGULAR"
  }'
```

### Calculate Loan Payment

```bash
curl -X POST http://localhost:8080/api/v1/loans/calculate \
  -H "Content-Type: application/json" \
  -d '{
    "loanAmount": 50000.00,
    "interestRate": 8.5,
    "termMonths": 36,
    "loanType": "PERSONAL"
  }'
```

---

## 📈 Performance Considerations

• **Credit Bureau Integration**: Async credit score checks
• **Payment Processing**: Batch payment processing
• **Document Storage**: Optimized file storage
• **Calculation Caching**: Cache loan calculations
• **Index Optimization**: Index on user_id, status, due_date
