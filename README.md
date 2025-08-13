# 🏦 Mlfts Bank API - Backend Documentation

## 📋 Table of Contents
1. [System Overview](#system-overview)
2. [Architecture Diagrams](#architecture-diagrams)
3. [Entity Relationship Diagram](#entity-relationship-diagram)
4. [Use Case Diagram](#use-case-diagram)
5. [Sequence Diagrams](#sequence-diagrams)
6. [Activity Diagrams](#activity-diagrams)
7. [System Architecture](#system-architecture)
8. [API Functions Documentation](#api-functions-documentation)
9. [Database Design](#database-design)
10. [Security Implementation](#security-implementation)
11. [Test Coverage Reports](#test-coverage-reports)
12. [Installation & Setup](#installation--setup)

---

## 🎯 System Overview

The Mlfts Bank API is a robust Spring Boot backend application that provides complete banking operations including account management, transaction processing, loan management, card services, and payment processing with role-based access control.

### Key Features
- **Multi-Role Authentication**: CUSTOMER, ADMIN, MANAGER, TELLER, LOAN_OFFICER, SECURITY_OFFICER
- **Account Management**: Checking, Savings, Business accounts
- **Transaction Processing**: Deposits, Withdrawals, Transfers
- **Loan Management**: Personal, Home, Auto, Business loans
- **Card Services**: Debit/Credit card management
- **Payment Processing**: Bill payments, Internal/External transfers
- **Security**: JWT authentication, Role-based access, Audit logging
- **Notification System**: Real-time alerts and notifications

### Technology Stack
- **Framework**: Spring Boot 3.2.0
- **Security**: Spring Security with JWT
- **Database**: SQL Server (Primary), H2 (Development)
- **ORM**: Hibernate/JPA
- **Documentation**: Swagger/OpenAPI 3
- **Build Tool**: Maven
- **Java Version**: 17+

---

## 🏗️ Architecture Diagrams

### System Architecture Flow Chart

```mermaid
flowchart TD
    A[Client Application] --> B[Spring Security Filter]
    B --> C{JWT Valid?}
    C -->|No| D[Return 401]
    C -->|Yes| E[Controller Layer]
    E --> F[Service Layer]
    F --> G[Repository Layer]
    G --> H[(Database)]
    F --> I[External Services]
    F --> J[Audit Service]
    J --> K[(Audit Logs)]
    
    subgraph "Controllers"
        E1[AuthController]
        E2[AccountController]
        E3[TransactionController]
        E4[LoanController]
        E5[CardController]
        E6[PaymentController]
        E7[UserController]
        E8[NotificationController]
    end
    
    subgraph "Services"
        F1[AuthService]
        F2[AccountService]
        F3[TransactionService]
        F4[LoanService]
        F5[CardService]
        F6[PaymentService]
        F7[UserService]
        F8[NotificationService]
        F9[AuditService]
    end
    
    subgraph "Repositories"
        G1[UserRepository]
        G2[AccountRepository]
        G3[TransactionRepository]
        G4[LoanRepository]
        G5[CardRepository]
        G6[PaymentRepository]
        G7[AuditRepository]
    end
```

### Layered Architecture

```mermaid
graph TB
    subgraph "Presentation Layer"
        A1[REST Controllers]
        A2[Exception Handlers]
        A3[Security Filters]
    end
    
    subgraph "Business Logic Layer"
        B1[Service Classes]
        B2[Business Rules]
        B3[Validation Logic]
    end
    
    subgraph "Data Access Layer"
        C1[Repository Interfaces]
        C2[JPA Entities]
        C3[Database Queries]
    end
    
    subgraph "Infrastructure Layer"
        D1[Security Configuration]
        D2[JWT Utilities]
        D3[Audit Logging]
        D4[Email Service]
    end
    
    A1 --> B1
    A2 --> B1
    B1 --> C1
    B1 --> D3
    C1 --> C2
    D1 --> A3
```

---

## 📊 Entity Relationship Diagram

```mermaid
erDiagram
    USER {
        bigint id PK
        string username UK
        string email UK
        string password
        string first_name
        string last_name
        string phone_number
        date date_of_birth
        string address
        string city
        string state
        string postal_code
        string country
        enum role
        boolean is_enabled
        boolean is_account_non_locked
        boolean kyc_verified
        datetime created_at
        datetime updated_at
    }
    
    ACCOUNT {
        bigint id PK
        string account_number UK
        enum account_type
        decimal balance
        decimal available_balance
        decimal overdraft_limit
        decimal minimum_balance
        decimal interest_rate
        decimal monthly_maintenance_fee
        boolean is_active
        boolean is_frozen
        date opened_date
        date closed_date
        bigint user_id FK
        datetime created_at
        datetime updated_at
    }
    
    TRANSACTION {
        bigint id PK
        string transaction_id UK
        enum transaction_type
        decimal amount
        string description
        string reference_number
        enum status
        bigint from_account_id FK
        bigint to_account_id FK
        bigint user_id FK
        datetime transaction_date
        datetime created_at
        datetime updated_at
    }
    
    CARD {
        bigint id PK
        string card_number UK
        enum card_type
        enum card_status
        string cardholder_name
        date expiry_date
        string cvv
        decimal daily_limit
        decimal monthly_limit
        boolean is_contactless_enabled
        boolean is_international_enabled
        boolean is_online_enabled
        bigint account_id FK
        bigint user_id FK
        datetime created_at
        datetime updated_at
    }
    
    LOAN {
        bigint id PK
        string loan_number UK
        enum loan_type
        decimal amount
        decimal interest_rate
        integer term_months
        decimal monthly_payment
        decimal outstanding_balance
        enum status
        string purpose
        decimal annual_income
        enum employment_status
        bigint account_id FK
        bigint user_id FK
        date application_date
        date approval_date
        date disbursement_date
        datetime created_at
        datetime updated_at
    }
    
    PAYMENT {
        bigint id PK
        string payment_id UK
        enum payment_type
        decimal amount
        string description
        enum status
        string recipient_name
        string recipient_account
        bigint from_account_id FK
        bigint to_account_id FK
        bigint user_id FK
        datetime payment_date
        datetime created_at
        datetime updated_at
    }
    
    AUDIT_LOG {
        bigint id PK
        string action
        string module
        string entity_type
        bigint entity_id
        string description
        text old_values
        text new_values
        enum severity
        string ip_address
        string user_agent
        bigint user_id FK
        datetime timestamp
        datetime created_at
        datetime updated_at
    }
    
    LOGIN_ATTEMPT {
        bigint id PK
        string username
        boolean is_successful
        string ip_address
        string user_agent
        string device
        string location
        string failure_reason
        bigint user_id FK
        datetime attempt_time
        datetime created_at
        datetime updated_at
    }
    
    NOTIFICATION {
        bigint id PK
        enum type
        string title
        text message
        enum status
        enum priority
        string channel
        bigint user_id FK
        datetime sent_at
        datetime read_at
        datetime created_at
        datetime updated_at
    }
    
    %% Relationships
    USER ||--o{ ACCOUNT : owns
    USER ||--o{ TRANSACTION : performs
    USER ||--o{ CARD : holds
    USER ||--o{ LOAN : applies
    USER ||--o{ PAYMENT : makes
    USER ||--o{ AUDIT_LOG : generates
    USER ||--o{ LOGIN_ATTEMPT : attempts
    USER ||--o{ NOTIFICATION : receives
    
    ACCOUNT ||--o{ TRANSACTION : "source target"
    ACCOUNT ||--o{ CARD : linked
    ACCOUNT ||--o{ LOAN : linked
    ACCOUNT ||--o{ PAYMENT : "source target"
```

---

## 👥 Use Case Diagram

```mermaid
graph TB
    subgraph "Banking System Use Cases"
        subgraph "Customer Use Cases"
            UC1[Register Account]
            UC2[Login]
            UC3[View Account Balance]
            UC4[Create Bank Account]
            UC5[Deposit Money]
            UC6[Withdraw Money]
            UC7[Transfer Money]
            UC8[Apply for Loan]
            UC9[Request Card]
            UC10[Pay Bills]
            UC11[View Transactions]
            UC12[Update Profile]
        end
        
        subgraph "Teller Use Cases"
            UC13[Assist Customer Registration]
            UC14[Process Deposits/Withdrawals]
            UC15[Handle Account Issues]
            UC16[Reset Customer Password]
            UC17[Search Customer Records]
            UC18[Process Loan Applications]
            UC19[Issue/Block Cards]
            UC20[Unlock Customer Accounts]
        end
        
        subgraph "Admin Use Cases"
            UC21[Manage Users]
            UC22[View System Reports]
            UC23[Configure System Settings]
            UC24[Monitor Transactions]
            UC25[Approve High-Value Transactions]
            UC26[Generate Audit Reports]
            UC27[Manage User Roles]
        end
        
        subgraph "System Use Cases"
            UC28[Validate Transactions]
            UC29[Send Notifications]
            UC30[Log Activities]
            UC31[Calculate Interest]
            UC32[Generate Reports]
            UC33[Backup Data]
        end
    end
    
    %% Actors
    Customer --> UC1
    Customer --> UC2
    Customer --> UC3
    Customer --> UC4
    Customer --> UC5
    Customer --> UC6
    Customer --> UC7
    Customer --> UC8
    Customer --> UC9
    Customer --> UC10
    Customer --> UC11
    Customer --> UC12
    
    Teller --> UC13
    Teller --> UC14
    Teller --> UC15
    Teller --> UC16
    Teller --> UC17
    Teller --> UC18
    Teller --> UC19
    Teller --> UC20
    
    Admin --> UC21
    Admin --> UC22
    Admin --> UC23
    Admin --> UC24
    Admin --> UC25
    Admin --> UC26
    Admin --> UC27
    
    System --> UC28
    System --> UC29
    System --> UC30
    System --> UC31
    System --> UC32
    System --> UC33
```

---

## 🔄 Sequence Diagrams

### User Authentication Sequence

```mermaid
sequenceDiagram
    participant C as Client
    participant AC as AuthController
    participant AS as AuthService
    participant US as UserService
    participant JWT as JwtUtil
    participant DB as Database
    participant AL as AuditService
    
    C->>AC: POST /auth/login {username, password}
    AC->>AS: authenticate(loginRequest)
    AS->>US: loadUserByUsername(username)
    US->>DB: findByUsername(username)
    DB-->>US: User entity
    US-->>AS: UserDetails
    AS->>AS: validatePassword()
    alt Authentication Success
        AS->>JWT: generateToken(userDetails)
        JWT-->>AS: JWT Token
        AS->>AL: logActivity("LOGIN_SUCCESS")
        AS-->>AC: AuthResponse with token
        AC-->>C: 200 OK {accessToken, refreshToken}
    else Authentication Failed
        AS->>AL: logActivity("LOGIN_FAILED")
        AS-->>AC: AuthenticationException
        AC-->>C: 401 Unauthorized
    end
```

### Account Creation Sequence

```mermaid
sequenceDiagram
    participant C as Client
    participant ACC as AccountController
    participant ACS as AccountService
    participant US as UserService
    participant AR as AccountRepository
    participant NG as NumberGenerator
    participant AL as AuditService
    participant DB as Database
    
    C->>ACC: POST /accounts {accountType, initialBalance}
    ACC->>ACC: getCurrentUserId()
    ACC->>US: getUserByUsername(username)
    US-->>ACC: UserResponse
    ACC->>ACS: createAccount(userId, request)
    ACS->>NG: generateAccountNumber()
    NG-->>ACS: Account Number
    ACS->>ACS: validateInitialBalance()
    ACS->>AR: save(account)
    AR->>DB: INSERT account
    DB-->>AR: Saved account
    AR-->>ACS: Account entity
    ACS->>AL: logActivity("ACCOUNT_CREATED")
    ACS-->>ACC: AccountResponse
    ACC-->>C: 200 OK {account details}
```

### Money Transfer Sequence

```mermaid
sequenceDiagram
    participant C as Client
    participant TC as TransactionController
    participant TS as TransactionService
    participant AS as AccountService
    participant TR as TransactionRepository
    participant AL as AuditService
    participant NS as NotificationService
    participant DB as Database
    
    C->>TC: POST /transactions/transfer {fromAccount, toAccount, amount}
    TC->>TS: processTransfer(transferRequest)
    TS->>AS: validateAccount(fromAccount)
    AS-->>TS: Account validation
    TS->>AS: validateAccount(toAccount)
    AS-->>TS: Account validation
    TS->>AS: checkBalance(fromAccount, amount)
    AS-->>TS: Balance check result
    
    alt Sufficient Balance
        TS->>AS: debitAccount(fromAccount, amount)
        AS->>DB: UPDATE account balance
        TS->>AS: creditAccount(toAccount, amount)
        AS->>DB: UPDATE account balance
        TS->>TR: saveTransaction(transaction)
        TR->>DB: INSERT transaction
        TS->>AL: logActivity("TRANSFER_SUCCESS")
        TS->>NS: sendTransferNotification()
        TS-->>TC: TransactionResponse
        TC-->>C: 200 OK {transaction details}
    else Insufficient Balance
        TS->>AL: logActivity("TRANSFER_FAILED")
        TS-->>TC: InsufficientFundsException
        TC-->>C: 400 Bad Request
    end
```

---

## 📈 Activity Diagrams

### Loan Application Process

```mermaid
flowchart TD
    A[Start: Customer Loan Application] --> B[Submit Loan Application]
    B --> C{Validate Application Data}
    C -->|Invalid| D[Return Validation Errors]
    C -->|Valid| E[Check Customer Eligibility]
    E --> F{Meets Basic Criteria?}
    F -->|No| G[Reject Application]
    F -->|Yes| H[Calculate Credit Score]
    H --> I{Credit Score >= 600?}
    I -->|No| J[Reject - Low Credit Score]
    I -->|Yes| K[Verify Income]
    K --> L{Income Verification OK?}
    L -->|No| M[Request Additional Documents]
    L -->|Yes| N[Calculate Loan Terms]
    N --> O[Manager/Loan Officer Review]
    O --> P{Approved?}
    P -->|No| Q[Reject with Reason]
    P -->|Yes| R[Generate Loan Agreement]
    R --> S[Send Approval Notification]
    S --> T[Customer Accepts Terms?]
    T -->|No| U[Cancel Application]
    T -->|Yes| V[Disburse Funds]
    V --> W[Create Loan Account]
    W --> X[Send Welcome Kit]
    X --> Y[End: Loan Active]
    
    D --> Z[End: Application Failed]
    G --> Z
    J --> Z
    Q --> Z
    U --> Z
```

### Transaction Processing Flow

```mermaid
flowchart TD
    A[Start: Transaction Request] --> B[Validate Request Format]
    B --> C{Valid Format?}
    C -->|No| D[Return Format Error]
    C -->|Yes| E[Authenticate User]
    E --> F{User Authorized?}
    F -->|No| G[Return Auth Error]
    F -->|Yes| H[Validate Account Access]
    H --> I{Account Owner?}
    I -->|No| J[Return Access Denied]
    I -->|Yes| K[Check Account Status]
    K --> L{Account Active?}
    L -->|No| M[Return Account Blocked]
    L -->|Yes| N[Validate Transaction Limits]
    N --> O{Within Limits?}
    O -->|No| P[Return Limit Exceeded]
    O -->|Yes| Q{Transaction Type?}
    
    Q -->|Deposit| R[Process Deposit]
    Q -->|Withdrawal| S[Check Balance]
    Q -->|Transfer| T[Validate Target Account]
    
    S --> U{Sufficient Balance?}
    U -->|No| V[Return Insufficient Funds]
    U -->|Yes| W[Process Withdrawal]
    
    T --> X{Target Account Valid?}
    X -->|No| Y[Return Invalid Account]
    X -->|Yes| Z[Process Transfer]
    
    R --> AA[Update Account Balance]
    W --> AA
    Z --> AA
    AA --> BB[Record Transaction]
    BB --> CC[Send Notification]
    CC --> DD[Update Audit Log]
    DD --> EE[Return Success Response]
    EE --> FF[End: Transaction Complete]
    
    D --> GG[End: Transaction Failed]
    G --> GG
    J --> GG
    M --> GG
    P --> GG
    V --> GG
    Y --> GG
```

---

## 🔧 System Architecture

### Component Architecture

```mermaid
graph TB
    subgraph "Client Layer"
        A1[Web Application]
        A2[Mobile App]
        A3[Third-party Systems]
    end
    
    subgraph "API Gateway Layer"
        B1[Load Balancer]
        B2[Rate Limiting]
        B3[API Versioning]
    end
    
    subgraph "Security Layer"
        C1[JWT Authentication]
        C2[Role-based Authorization]
        C3[CORS Configuration]
        C4[CSRF Protection]
    end
    
    subgraph "Controller Layer"
        D1[AuthController]
        D2[AccountController]
        D3[TransactionController]
        D4[LoanController]
        D5[CardController]
        D6[PaymentController]
        D7[UserController]
        D8[NotificationController]
    end
    
    subgraph "Service Layer"
        E1[AuthenticationService]
        E2[AccountService]
        E3[TransactionService]
        E4[LoanService]
        E5[CardService]
        E6[PaymentService]
        E7[UserService]
        E8[NotificationService]
        E9[AuditService]
    end
    
    subgraph "Repository Layer"
        F1[UserRepository]
        F2[AccountRepository]
        F3[TransactionRepository]
        F4[LoanRepository]
        F5[CardRepository]
        F6[PaymentRepository]
        F7[AuditRepository]
        F8[NotificationRepository]
    end
    
    subgraph "Database Layer"
        G1[(SQL Server)]
        G2[(H2 - Development)]
    end
    
    subgraph "External Services"
        H1[Email Service]
        H2[SMS Service]
        H3[Payment Gateway]
        H4[Credit Bureau]
    end
    
    A1 --> B1
    A2 --> B1
    A3 --> B1
    B1 --> C1
    C1 --> D1
    C1 --> D2
    C1 --> D3
    C1 --> D4
    C1 --> D5
    C1 --> D6
    C1 --> D7
    C1 --> D8
    
    D1 --> E1
    D2 --> E2
    D3 --> E3
    D4 --> E4
    D5 --> E5
    D6 --> E6
    D7 --> E7
    D8 --> E8
    
    E1 --> F1
    E2 --> F2
    E3 --> F3
    E4 --> F4
    E5 --> F5
    E6 --> F6
    E7 --> F7
    E8 --> F8
    E9 --> F7
    
    F1 --> G1
    F2 --> G1
    F3 --> G1
    F4 --> G1
    F5 --> G1
    F6 --> G1
    F7 --> G1
    F8 --> G1
    
    E8 --> H1
    E8 --> H2
    E6 --> H3
    E4 --> H4
```

---

## 📚 API Functions Documentation

All API functions are documented in detail in the `api-functions` folder:

### Controller Functions
- [AuthController Functions](./api-functions/auth-controller-functions.md)
- [AccountController Functions](./api-functions/account-controller-functions.md)
- [TransactionController Functions](./api-functions/transaction-controller-functions.md)
- [LoanController Functions](./api-functions/loan-controller-functions.md)
- [CardController Functions](./api-functions/card-controller-functions.md)
- [PaymentController Functions](./api-functions/payment-controller-functions.md)
- [UserController Functions](./api-functions/user-controller-functions.md)
- [NotificationController Functions](./api-functions/notification-controller-functions.md)

### Service Functions
- [AuthenticationService Functions](./api-functions/authentication-service-functions.md)
- [AccountService Functions](./api-functions/account-service-functions.md)
- [TransactionService Functions](./api-functions/transaction-service-functions.md)
- [LoanService Functions](./api-functions/loan-service-functions.md)
- [CardService Functions](./api-functions/card-service-functions.md)
- [PaymentService Functions](./api-functions/payment-service-functions.md)
- [UserService Functions](./api-functions/user-service-functions.md)
- [NotificationService Functions](./api-functions/notification-service-functions.md)
- [AuditService Functions](./api-functions/audit-service-functions.md)

### Repository Functions
- [Repository Functions](./api-functions/repository-functions.md)

### Utility Functions
- [Security Utilities](./api-functions/security-utilities.md)
- [JWT Utilities](./api-functions/jwt-utilities.md)
- [Number Generator](./api-functions/number-generator.md)

---

## 🗄️ Database Design

### Primary Entities

| Entity | Purpose | Key Attributes |
|--------|---------|----------------|
| **User** | Store user account information | username, email, role, profile data |
| **Account** | Bank accounts for users | account_number, type, balance, limits |
| **Transaction** | Record all financial transactions | amount, type, status, accounts involved |
| **Card** | Debit/Credit cards | card_number, type, limits, security settings |
| **Loan** | Loan applications and accounts | amount, terms, interest_rate, status |
| **Payment** | Payment processing records | payment_type, amount, recipients |
| **AuditLog** | System activity logging | action, entity changes, user activity |
| **Notification** | User notifications | type, message, delivery status |

### Database Indexes

```sql
-- Performance indexes
CREATE INDEX idx_user_username ON users(username);
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_account_user_id ON accounts(user_id);
CREATE INDEX idx_account_number ON accounts(account_number);
CREATE INDEX idx_transaction_user_id ON transactions(user_id);
CREATE INDEX idx_transaction_date ON transactions(transaction_date);
CREATE INDEX idx_card_user_id ON cards(user_id);
CREATE INDEX idx_card_account_id ON cards(account_id);
CREATE INDEX idx_loan_user_id ON loans(user_id);
CREATE INDEX idx_audit_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_timestamp ON audit_logs(timestamp);
```

---

## 🔐 Security Implementation

### Authentication & Authorization

#### JWT Token Structure
```json
{
  "header": {
    "alg": "HS512",
    "typ": "JWT"
  },
  "payload": {
    "sub": "username",
    "roles": ["CUSTOMER"],
    "iat": 1234567890,
    "exp": 1234567890
  }
}
```

#### Role-Based Access Control

| Role | Permissions |
|------|-------------|
| **CUSTOMER** | Own account management, transactions, loans, cards |
| **TELLER** | Customer service, account assistance, basic operations |
| **ADMIN** | Full system access, user management, configuration |
| **MANAGER** | Oversight, reporting, approval workflows |
| **LOAN_OFFICER** | Loan processing, approval, management |
| **SECURITY_OFFICER** | Security monitoring, fraud detection |

#### Security Features
- **Password Encryption**: BCrypt hashing
- **Token Expiration**: Configurable JWT expiration
- **Rate Limiting**: Request throttling
- **Account Lockout**: Failed login protection
- **Audit Logging**: Complete activity tracking
- **Input Validation**: Request sanitization
- **CORS Configuration**: Cross-origin protection

---

## 🧪 Test Coverage Reports

### JaCoCo Test Coverage Integration

This project includes comprehensive test coverage reporting using **JaCoCo 0.8.13** with Java 17 compatibility.

#### Running Tests with Coverage

```bash
# Run all tests with coverage report
mvn clean test

# Run specific test class
mvn test -Dtest=AccountServiceTest

# Generate coverage report only
mvn jacoco:report
```

#### Coverage Report Locations

After running tests, coverage reports are generated in multiple formats:

- **HTML Report**: `target/site/jacoco/index.html` - Interactive web report
- **XML Report**: `target/site/jacoco/jacoco.xml` - CI/CD integration
- **CSV Report**: `target/site/jacoco/jacoco.csv` - Data analysis

#### Coverage Configuration

JaCoCo is configured with coverage thresholds and exclusions:

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.13</version>
    <configuration>
        <excludes>
            <exclude>**/dto/**</exclude>
            <exclude>**/config/**</exclude>
            <exclude>**/Application.class</exclude>
        </excludes>
        <rules>
            <rule>
                <element>BUNDLE</element>
                <limits>
                    <limit>
                        <counter>INSTRUCTION</counter>
                        <value>COVEREDRATIO</value>
                        <minimum>0.50</minimum>
                    </limit>
                </limits>
            </rule>
        </rules>
    </configuration>
</plugin>
```

#### Current Coverage Status

- **Total Classes Analyzed**: 61
- **Test Classes**: 2 (BankingSystemApplicationTests, AccountServiceTest)
- **Test Methods**: 8 total test cases
- **AccountService Coverage**: ~60% instruction coverage, 66% line coverage

#### Coverage Highlights by Package

| Package | Coverage Focus | Status |
|---------|----------------|--------|
| **Controller Layer** | API endpoint testing | Planned |
| **Service Layer** | Business logic coverage | AccountService: ✅ 60% |
| **Repository Layer** | Data access testing | Planned |
| **Security Layer** | Authentication testing | Planned |
| **Utility Classes** | Helper function testing | Planned |

#### Viewing Coverage Reports

1. **Open HTML Report**: Navigate to `target/site/jacoco/index.html` in your browser
2. **Drill Down**: Click on packages, classes, and methods for detailed coverage
3. **Color Coding**: 
   - 🟢 Green: Well covered code
   - 🟡 Yellow: Partially covered code  
   - 🔴 Red: Uncovered code

#### Integration with Build Process

Coverage reports are automatically generated during:
- Maven `test` phase
- CI/CD pipeline execution
- Development builds
- Release verification

#### Test Coverage Best Practices

1. **Unit Tests**: Focus on individual service methods
2. **Integration Tests**: Test controller-service interactions  
3. **Boundary Testing**: Validate edge cases and error conditions
4. **Security Testing**: Verify authentication and authorization
5. **Performance Testing**: Ensure acceptable response times

---

## 🚀 Installation & Setup

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- SQL Server 2019+ (or H2 for development)
- IDE (IntelliJ IDEA, Eclipse, VS Code)

### Setup Steps

1. **Clone Repository**
```bash
git clone <repository-url>
cd banking-system
```

2. **Database Configuration**
```properties
# SQL Server (Production)
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=BankingSystemDB;encrypt=true;trustServerCertificate=true
spring.datasource.username=sa
spring.datasource.password=your_password

# H2 (Development)
spring.datasource.url=jdbc:h2:mem:bankingdb
spring.h2.console.enabled=true
```

3. **Build Application**
```bash
mvn clean compile
```

4. **Run Application**
```bash
mvn spring-boot:run
```

5. **Access Application**
- API Base URL: `http://localhost:8080/api/v1`
- Swagger UI: `http://localhost:8080/api/v1/swagger-ui.html`
- H2 Console: `http://localhost:8080/api/v1/h2-console`

### Environment Configuration

#### Development
```properties
spring.profiles.active=dev
logging.level.com.bankingsystem=DEBUG
```

#### Production
```properties
spring.profiles.active=prod
logging.level.com.bankingsystem=INFO
```

---

## 📊 API Documentation

### Base URL
```
http://localhost:8080/api/v1
```

### Authentication Endpoints
- `POST /auth/register` - User registration
- `POST /auth/login` - User login
- `POST /auth/refresh` - Token refresh
- `POST /auth/logout` - User logout

### Account Management
- `GET /accounts` - Get user accounts
- `POST /accounts` - Create new account
- `GET /accounts/{id}` - Get account details
- `PUT /accounts/{id}/freeze` - Freeze account
- `PUT /accounts/{id}/unfreeze` - Unfreeze account

### Transaction Management
- `GET /transactions/user` - Get user transactions
- `POST /transactions/deposit` - Deposit money
- `POST /transactions/withdraw` - Withdraw money
- `POST /transactions/transfer` - Transfer money

### Complete API documentation is available in Swagger UI after starting the application.

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

---

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

---

## API Documentation

### Detailed Function Documentation
Complete API function documentation is available in the `api-functions` folder:

- **[Authentication Functions](api-functions/auth-controller-functions.md)** - User registration, login, password management
- **[Account Functions](api-functions/account-controller-functions.md)** - Account management, balance checks, settings
- **[Transaction Functions](api-functions/transaction-controller-functions.md)** - Deposits, withdrawals, transfers, statements
- **[Loan Functions](api-functions/loan-controller-functions.md)** - Loan applications, approvals, payments, schedules
- **[Card Functions](api-functions/card-controller-functions.md)** - Card management, activation, blocking, transactions
- **[Payment Functions](api-functions/payment-controller-functions.md)** - Bill payments, payee management, recurring payments
- **[User Functions](api-functions/user-controller-functions.md)** - Profile management, password reset, user search
- **[Notification Functions](api-functions/notification-controller-functions.md)** - Notification delivery, preferences, analytics

Each documentation file includes:
- Function descriptions and business logic
- Request/response examples with parameters
- Security features and role requirements
- Error handling and response codes
- cURL usage examples for testing
- Performance considerations

### API Overview
- **Total Controllers**: 8
- **Total Functions**: 64 documented endpoints
- **Authentication**: JWT-based with role-based access control
- **Base URL**: `http://localhost:8080/api/v1`

---

## 📞 Support

For support and questions:
- Email: tvntving@gmail.com
- Documentation: [API Docs](http://localhost:8080/api/v1/swagger-ui.html)
- Function Docs: [API Functions](api-functions/README.md)
- Issues: GitHub Issues

---

*Last Updated: August 12, 2025*
