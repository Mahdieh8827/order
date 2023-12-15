# Payment Processing Service

This project implements a payment processing service for merchants using Java and the Spring framework. The service exposes a REST API that accepts HTTP requests and responds with JSON payloads.

## Prerequisites
To build and run this application, you need to have the following installed:

- Java Development Kit (JDK) 11 or later: [Download](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html)
- Apache Maven: [Download](https://maven.apache.org/download.cgi)
- Docker: [Download](https://www.docker.com/products/docker-desktop)

## Getting Started

Follow these instructions to get the project up and running on your local machine.<br/>
1.Clone the repository from GitHub:
```bash
git clone ...
```
2.Navigate to the project directory

3.Build the project using Maven:
```bash
mvn clean install
```
4.Run Docker:
```bash
docker-compose up -d
```
5.Run Spring boot application:
```bash
mvn spring-boot:run
```

6.Run Tests:
```bash
mvn test
```

## Endpoints

### Payment Endpoints

#### New Payment: POST /api/payment/process
- Request: JSON payload with amount, customer name, credit card number,merchant Id, and expiry date
- Response: paymentId, status
- Payments will fail if the credit card number starts with 5.

#### Refund Payment: POST /api/payment/refund/{paymentId}
- Request: Empty
- Response: paymentId, status
- Refund is only possible for payments from the same merchant.</br>
- Refund is only possible for Successful payments.</br>
- Refund is only possible for payments that have not already been refunded.</br>

#### View Payment: GET /api/payment/{paymentId}
- Request: Empty
- Response: payment details based on paymentId, including refund records if applicable.</br>
- View is only allowed for payments from the same merchant.

#### List Payments: GET /api/payment
- Parameters: sort by date, filter by customer name
- Response: List of payments.

#### Show Merchant Payments Statistics: GET /api/payment/statistics
- Request: Empty
- Response: JSON payload with total payments count, total payments amount, total fees amount for the merchant.</br>
- payload with total payments with successful status and refunds.

## Security

All payment endpoints are secured via JWT token authentication/authorization.</br>
Set PAYMENT_MERCHANT as the default role for accessing all APIs.

#### Generate JWT Token for a Merchant: POST /api/authenticate/{merchantId}
- Request: Empty
- Response: JWT token
- generate JWT token with merchantId and PAYMENT_MERCHANT GrantedAuthority

## Merchant Table Initialization
Included in the application is a Flyway migration file that initializes the Merchant table. Two merchants with IDs 1000 and 1001 are created for testing purposes.

### Flyway Migration File:
Location: [ db/migration: V1__initial_schema.sql ]
Purpose: Create initial merchant entries (ID: 1000, 1001), Please be aware if you are going to test via Postman, you need to change the merchantId in the request body to 1000 or 1001.
---

# Assumptions
- I incorporate a dedicated table to store information about merchants, ensuring a systematic record of every payment.
- Additionally, I treat refunds as negative payments, allowing them to be distinct yet seamlessly integrated into the payment system.
- Moreover, each successful payment has the potential for a corresponding refund, with a one-time limit for refund transactions.
- Statistics is only available for all successful and Refund payments.