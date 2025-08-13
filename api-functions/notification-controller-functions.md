# NotificationController Functions

## Overview

The NotificationController manages all notification operations including sending, retrieving, marking as read, and managing notification preferences.

**Base Path**: `/api/v1/notifications`

---

## 🔔 Functions List

### 1. **Get User Notifications**

- **Endpoint**: `GET /notifications`
- **Purpose**: Retrieve notifications for authenticated user
- **Access Level**: Protected
- **Required Role**: CUSTOMER, ADMIN, TELLER, LOAN_OFFICER

#### Query Options

- `page`: number (optional, default: 0)
- `size`: number (optional, default: 20)
- `isRead`: boolean (optional, filter by read status)
- `priority`: string (optional, HIGH|MEDIUM|LOW)
- `category`: string (optional, filter by category)
- `startDate`: date (optional, date range filter)
- `endDate`: date (optional, date range filter)

#### Notifications Response

```json
{
  "content": [
    {
      "id": 1,
      "title": "Transaction Alert",
      "message": "Your account ending in 7890 was debited $125.50 for Electric Company payment",
      "category": "TRANSACTION",
      "priority": "MEDIUM",
      "isRead": false,
      "createdAt": "2025-08-12T14:30:00",
      "expiryDate": "2025-08-19T14:30:00",
      "actionRequired": false,
      "actionUrl": "/transactions/1",
      "metadata": {
        "transactionId": "TXN1234567890",
        "amount": 125.50,
        "accountId": 1
      }
    }
  ],
  "totalElements": 15,
  "totalPages": 1,
  "size": 20,
  "number": 0,
  "unreadCount": 3,
  "summary": {
    "totalNotifications": 15,
    "unreadNotifications": 3,
    "highPriorityUnread": 1,
    "categoryCounts": {
      "TRANSACTION": 8,
      "SECURITY": 2,
      "ACCOUNT": 3,
      "SYSTEM": 2
    }
  }
}
```

#### Processing Logic

• Returns paginated notification list
• Filters by read status and categories
• Includes notification metadata
• Shows unread count summary
• Orders by priority then date

---

### 2. **Get Notification Details**

- **Endpoint**: `GET /notifications/{notificationId}`
- **Purpose**: Retrieve detailed information for specific notification
- **Access Level**: Protected
- **Required Role**: CUSTOMER (own notifications), ADMIN, TELLER

#### Path Parameter

- `notificationId` (Long, required)

#### Detailed Response

```json
{
  "id": 1,
  "title": "Transaction Alert",
  "message": "Your account ending in 7890 was debited $125.50 for Electric Company payment",
  "longMessage": "A payment of $125.50 was successfully processed from your checking account (ACC1234567890) to Electric Company on August 12, 2025 at 2:30 PM. Your remaining balance is $2,374.50.",
  "category": "TRANSACTION",
  "priority": "MEDIUM",
  "isRead": false,
  "createdAt": "2025-08-12T14:30:00",
  "readAt": null,
  "expiryDate": "2025-08-19T14:30:00",
  "actionRequired": false,
  "actionUrl": "/transactions/1",
  "actionText": "View Transaction",
  "sender": {
    "type": "SYSTEM",
    "name": "Banking System"
  },
  "deliveryChannels": ["IN_APP", "EMAIL"],
  "deliveryStatus": {
    "inApp": "DELIVERED",
    "email": "SENT",
    "sms": "NOT_SENT"
  },
  "metadata": {
    "transactionId": "TXN1234567890",
    "amount": 125.50,
    "accountId": 1,
    "payeeId": 5,
    "payeeName": "Electric Company"
  },
  "relatedNotifications": [2, 3]
}
```

#### Processing Logic

• Validates notification access permissions
• Returns complete notification details
• Includes delivery status information
• Shows related notifications
• Provides action links if applicable

---

### 3. **Mark Notification as Read**

- **Endpoint**: `POST /notifications/{notificationId}/read`
- **Purpose**: Mark a specific notification as read
- **Access Level**: Protected
- **Required Role**: CUSTOMER (own notifications), ADMIN, TELLER

#### Path Parameter

- `notificationId` (Long, required)

#### Read Response

```json
{
  "notificationId": 1,
  "isRead": true,
  "readAt": "2025-08-12T18:00:00",
  "message": "Notification marked as read"
}
```

#### Processing Logic

• Validates notification ownership
• Updates read status and timestamp
• Decrements unread count
• Records read activity
• Returns confirmation

---

### 4. **Mark All Notifications as Read**

- **Endpoint**: `POST /notifications/read-all`
- **Purpose**: Mark all user notifications as read
- **Access Level**: Protected
- **Required Role**: CUSTOMER, ADMIN, TELLER

#### No Parameters Required

#### Bulk Read Response

```json
{
  "markedAsRead": 5,
  "totalNotifications": 15,
  "readAt": "2025-08-12T18:15:00",
  "message": "All notifications marked as read"
}
```

#### Processing Logic

• Marks all unread notifications as read
• Updates read timestamps
• Resets unread counter
• Records bulk read activity
• Returns count of updated notifications

---

### 5. **Delete Notification**

- **Endpoint**: `DELETE /notifications/{notificationId}`
- **Purpose**: Delete a specific notification
- **Access Level**: Protected
- **Required Role**: CUSTOMER (own notifications), ADMIN, TELLER

#### Path Parameter

- `notificationId` (Long, required)

#### Deletion Response

```json
{
  "notificationId": 1,
  "deleted": true,
  "deletedAt": "2025-08-12T18:30:00",
  "message": "Notification deleted successfully"
}
```

#### Processing Logic

• Validates notification ownership
• Soft-deletes notification record
• Updates notification counters
• Records deletion activity
• Returns confirmation

---

### 6. **Send Notification (Admin/System)**

- **Endpoint**: `POST /notifications/send`
- **Purpose**: Send notification to specific user or group
- **Access Level**: Protected
- **Required Role**: ADMIN, SYSTEM

#### Send Request

```json
{
  "recipientId": "number (optional, specific user)",
  "recipientGroup": "string (optional, ALL|CUSTOMERS|TELLERS)",
  "title": "string (required)",
  "message": "string (required)",
  "longMessage": "string (optional)",
  "category": "TRANSACTION|SECURITY|ACCOUNT|SYSTEM|MARKETING",
  "priority": "HIGH|MEDIUM|LOW",
  "actionRequired": "boolean (default: false)",
  "actionUrl": "string (optional)",
  "actionText": "string (optional)",
  "expiryDate": "date (optional)",
  "deliveryChannels": ["IN_APP", "EMAIL", "SMS"],
  "scheduleDate": "date (optional, for scheduled delivery)",
  "metadata": "object (optional)"
}
```

#### Send Response

```json
{
  "notificationId": 10,
  "title": "System Maintenance Notice",
  "message": "Scheduled maintenance will occur on August 15, 2025 from 2:00 AM to 4:00 AM EST",
  "category": "SYSTEM",
  "priority": "HIGH",
  "recipientCount": 1250,
  "deliveryChannels": ["IN_APP", "EMAIL"],
  "scheduledAt": "2025-08-12T20:00:00",
  "estimatedDelivery": "2025-08-12T20:05:00",
  "status": "QUEUED",
  "message": "Notification queued for delivery"
}
```

#### Processing Logic

• Validates administrative authorization
• Queues notification for delivery
• Determines recipient list
• Schedules delivery across channels
• Records notification creation
• Returns delivery status

---

### 7. **Get Notification Statistics**

- **Endpoint**: `GET /notifications/stats`
- **Purpose**: Retrieve notification statistics and analytics
- **Access Level**: Protected
- **Required Role**: CUSTOMER (own stats), ADMIN, TELLER

#### Query Options

- `period`: string (optional, DAILY|WEEKLY|MONTHLY, default: MONTHLY)
- `startDate`: date (optional)
- `endDate`: date (optional)

#### Statistics Response

```json
{
  "userId": 1,
  "period": "MONTHLY",
  "startDate": "2025-08-01",
  "endDate": "2025-08-31",
  "totalNotifications": 45,
  "readNotifications": 38,
  "unreadNotifications": 7,
  "deletedNotifications": 12,
  "readRate": 84.4,
  "averageReadTime": "2.5 hours",
  "categoryBreakdown": {
    "TRANSACTION": 25,
    "SECURITY": 8,
    "ACCOUNT": 7,
    "SYSTEM": 5
  },
  "priorityBreakdown": {
    "HIGH": 3,
    "MEDIUM": 32,
    "LOW": 10
  },
  "deliveryChannelStats": {
    "inApp": {
      "sent": 45,
      "delivered": 45,
      "read": 38
    },
    "email": {
      "sent": 20,
      "delivered": 19,
      "opened": 15
    },
    "sms": {
      "sent": 5,
      "delivered": 5,
      "read": 5
    }
  },
  "engagementMetrics": {
    "clickThroughRate": 12.5,
    "actionTakenRate": 8.3,
    "mostEngagedCategory": "SECURITY"
  }
}
```

#### Processing Logic

• Calculates notification metrics
• Analyzes user engagement
• Provides delivery statistics
• Shows category preferences
• Tracks read patterns

---

### 8. **Update Notification Preferences**

- **Endpoint**: `PUT /notifications/preferences`
- **Purpose**: Update user notification preferences
- **Access Level**: Protected
- **Required Role**: CUSTOMER, ADMIN, TELLER

#### Preferences Request

```json
{
  "channels": {
    "inApp": "boolean (default: true)",
    "email": "boolean (default: true)",
    "sms": "boolean (default: false)",
    "push": "boolean (default: true)"
  },
  "categories": {
    "TRANSACTION": {
      "enabled": "boolean (default: true)",
      "threshold": "number (optional, minimum amount)",
      "channels": ["IN_APP", "EMAIL"]
    },
    "SECURITY": {
      "enabled": "boolean (default: true)",
      "channels": ["IN_APP", "EMAIL", "SMS"]
    },
    "ACCOUNT": {
      "enabled": "boolean (default: true)",
      "channels": ["IN_APP", "EMAIL"]
    },
    "SYSTEM": {
      "enabled": "boolean (default: true)",
      "channels": ["IN_APP"]
    },
    "MARKETING": {
      "enabled": "boolean (default: false)",
      "channels": ["EMAIL"]
    }
  },
  "quietHours": {
    "enabled": "boolean (default: false)",
    "startTime": "string (optional, HH:MM format)",
    "endTime": "string (optional, HH:MM format)",
    "timezone": "string (optional)"
  },
  "frequency": {
    "maxPerDay": "number (optional, default: unlimited)",
    "digestMode": "boolean (default: false)",
    "digestTime": "string (optional, HH:MM format)"
  }
}
```

#### Preferences Response

```json
{
  "userId": 1,
  "channels": {
    "inApp": true,
    "email": true,
    "sms": false,
    "push": true
  },
  "categories": {
    "TRANSACTION": {
      "enabled": true,
      "threshold": 50.00,
      "channels": ["IN_APP", "EMAIL"]
    },
    "SECURITY": {
      "enabled": true,
      "channels": ["IN_APP", "EMAIL", "SMS"]
    },
    "ACCOUNT": {
      "enabled": true,
      "channels": ["IN_APP", "EMAIL"]
    },
    "SYSTEM": {
      "enabled": true,
      "channels": ["IN_APP"]
    },
    "MARKETING": {
      "enabled": false,
      "channels": []
    }
  },
  "quietHours": {
    "enabled": true,
    "startTime": "22:00",
    "endTime": "08:00",
    "timezone": "America/New_York"
  },
  "frequency": {
    "maxPerDay": 20,
    "digestMode": false
  },
  "updatedAt": "2025-08-12T19:00:00",
  "message": "Notification preferences updated successfully"
}
```

#### Processing Logic

• Updates notification settings
• Validates preference constraints
• Applies delivery rules
• Records preference changes
• Confirms update success

---

## 🔒 Security Features

### Access Control

• **User Isolation**: Users only see own notifications
• **Role-Based Access**: Admin/Teller expanded access
• **Content Filtering**: Sensitive data protection
• **Permission Validation**: Strict authorization checks

### Delivery Security

• **Channel Encryption**: Secure delivery channels
• **Content Validation**: Message content sanitization
• **Rate Limiting**: Prevent notification spam
• **Audit Trail**: Complete delivery tracking

### Privacy Protection

• **Data Minimization**: Only necessary information
• **Retention Policies**: Automatic cleanup
• **Consent Management**: User preference respect
• **Anonymization**: Statistical data protection

---

## 📊 Error Responses

### Notification Not Found (404)

```json
{
  "status": 404,
  "error": "Notification Not Found",
  "message": "Notification with ID 999 not found or access denied"
}
```

### Invalid Preferences (400)

```json
{
  "status": 400,
  "error": "Invalid Preferences",
  "message": "Invalid notification preference configuration",
  "details": {
    "quietHours": "End time must be after start time",
    "threshold": "Threshold must be positive number"
  }
}
```

### Delivery Failed (500)

```json
{
  "status": 500,
  "error": "Delivery Failed",
  "message": "Failed to deliver notification via requested channel",
  "details": {
    "channel": "EMAIL",
    "reason": "Invalid email address",
    "retryScheduled": true
  }
}
```

---

## 🧪 Usage Examples

### Get User Notifications

```bash
curl -X GET "http://localhost:8080/api/v1/notifications?page=0&size=10&isRead=false" \
  -H "Authorization: Bearer ${JWT_TOKEN}"
```

### Mark Notification as Read

```bash
curl -X POST http://localhost:8080/api/v1/notifications/1/read \
  -H "Authorization: Bearer ${JWT_TOKEN}"
```

### Update Notification Preferences

```bash
curl -X PUT http://localhost:8080/api/v1/notifications/preferences \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${JWT_TOKEN}" \
  -d '{
    "channels": {
      "inApp": true,
      "email": true,
      "sms": false
    },
    "categories": {
      "TRANSACTION": {
        "enabled": true,
        "threshold": 100.00,
        "channels": ["IN_APP", "EMAIL"]
      }
    }
  }'
```

### Send System Notification (Admin)

```bash
curl -X POST http://localhost:8080/api/v1/notifications/send \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${ADMIN_JWT_TOKEN}" \
  -d '{
    "recipientGroup": "ALL",
    "title": "System Maintenance Notice",
    "message": "Scheduled maintenance tonight from 2:00 AM to 4:00 AM EST",
    "category": "SYSTEM",
    "priority": "HIGH",
    "deliveryChannels": ["IN_APP", "EMAIL"]
  }'
```

---

## 📈 Performance Considerations

• **Batch Processing**: Bulk notification delivery
• **Queue Management**: Asynchronous notification processing
• **Database Indexing**: Index on user_id, created_at, is_read
• **Caching Strategy**: Cache user preferences
• **Delivery Optimization**: Smart channel selection based on user activity
