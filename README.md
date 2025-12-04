# Flight Booking Microservices System
A Production-Grade Microservices project built using Spring Boot, Spring Cloud, Eureka, API Gateway, MongoDB, RabbitMQ, SonarQube, and Jacoco.

## Project Overview

| Service                       | Description                                                   |
| ----------------------------- | ------------------------------------------------------------- |
|   API Gateway                 | Single entry point to all backend microservices               |
|   Service Registry (Eureka)   | Service discovery for all microservices                       |
|   Config Server               | Centralized configuration management                          |
|   Flight Service              | CRUD operations for flights + seat availability management    |
|   Booking Service             | Booking, cancellation, payment simulation, email notification |


## Architecture Diagram

                   +------------------------+
                   |     CONFIG SERVER      |
                   |  (Centralized Config)  |
                   +-----------+------------+
                               |
                  +------------v-------------+
                  |    SERVICE REGISTRY      |
                  |     (Eureka Server)      |
                  +------------+-------------+
                               |
   +---------------------------+---------------------------+
   |                           |                           |
+--v--+                   +----v----+                +-----v-----+
|API  |                   | FLIGHT  |                | BOOKING    |
|GATE-|  REST Calls via   | SERVICE |  Feign Client  | SERVICE    |
|WAY  |<------------------|        |<--------------->|            |
+--+--+                   +----+---+                +-----+------+
                               |                           |
                               |                           |
                               +-------------+-------------+
                                             |
                                      +------v-------+
                                      | RabbitMQ     |
                                      | (Email Queue)|
                                      +------+-------+
                                             |
                                     +-------v--------+
                                     | EMAIL CONSUMER |
                                     | Sends real mail|
                                     +----------------+

## Features

Microservices Architecture

• Each service runs independently

• Connected through REST + Feign clients

• Load-balanced through Eureka

Resilience4j Circuit Breaker

• Prevents cascading failures

• Returns a graceful fallback when Flight Service is down

Booking Service

• Book a flight

• Cancel a booking

• Check booking history

• Automatically reduces or restores flight seats

Flight Service

• Add/update flights

• Check seat availability

• Reduce / increase seat count

RabbitMQ Email Notification

• Booking confirmations sent asynchronously

• Decoupled email flow

Centralized Configuration

• Powered by Spring Cloud Config Server

SonarQube + GitHub Actions CI/CD

• Automated code quality checks

• Jacoco test coverage reports

• Quality Gate enforcement

## Folder Structure 

flight_booking_microservices/
│── api-gateway/
│── bookingservice/
│── flightservice/
│── server/              # Config Server
│── service-registry/    # Eureka Server
│── pom.xml (root)
│── .github/workflows/build.yml


## Setup Instructions
1. Clone the repository

git clone https://github.com/saksham-0425/CHUBB_flight_microservice.git
cd CHUBB_flight_microservice


2. Start Required Tools

Start RabbitMQ
Start Mongodb


3. Run the services in order

Config server
Eureka Service Registry
API Gateway
Flight Service
Booking Service

# Screenshots of the working project 
## All the services being registered on the Eureka Service:-
<img width="1628" height="394" alt="image" src="https://github.com/user-attachments/assets/453a8559-a61d-4aed-bdc3-a1cd2757942f" />

## Queue creation at the starting of the bookingservice:-
<img width="1903" height="849" alt="image" src="https://github.com/user-attachments/assets/b2ca6a56-6206-47aa-83d4-890de7c5ad92" />

## Email consumption by the queue upon the successful booking of the ticket:-
<img width="1066" height="210" alt="image" src="https://github.com/user-attachments/assets/f21359d8-dc81-4d22-b246-357d8aaca370" />

## Circuit Breaker working when the flight service is down:-
<img width="954" height="670" alt="image" src="https://github.com/user-attachments/assets/bd49750e-e7b1-4cf8-aeb9-8619060ee292" />

## Configuration being received from the config server:-
<img width="1480" height="384" alt="image" src="https://github.com/user-attachments/assets/4bad1387-0566-4f5b-b6e0-bef490ecbdb0" />

## Jacoco coverage of flightservice:-
<img width="1915" height="376" alt="image" src="https://github.com/user-attachments/assets/45e782f1-ebf7-4b9d-9824-51f15c17b91f" />

## Jacoco coverage of bookingservice:-
<img width="1919" height="489" alt="image" src="https://github.com/user-attachments/assets/b7ddab5c-1e53-4f84-b8cc-7b3d8eb38edf" />

## Jmeter load testing with 20 samples:-

![WhatsApp Image 2025-12-02 at 03 23 49_1f5e890d](https://github.com/user-attachments/assets/fcf9a86f-15db-4b0b-a34e-ae97af0b1365)

![WhatsApp Image 2025-12-02 at 03 24 08_c062cf8d](https://github.com/user-attachments/assets/22981a40-50f7-44b3-9317-cfb56e83334f)
