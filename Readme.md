# Flight Booking Microservices

### ( Logs folder (within repo) and screenshots (within readme) have been attached with the project ).

The system demonstrates:

- Centralized configuration via *Spring Cloud Config*
- Service discovery using *Eureka*
- A secured *API Gateway* with *JWT authentication*
- Independent microservices for *Auth, **Flights, and **Bookings*
- Separate *MongoDB databases per service*
- Containerized deployment with *Docker Compose*
- File-based logging per service

## Architecture Diagram

```mermaid

flowchart TD

subgraph Infra
    CS[Config Server]
    ER[Eureka Server]
end

subgraph GatewayLayer
    AG[API Gateway]
end

subgraph Services
    AS[Auth Service]
    FS[Flight Service]
    BS[Booking Service]
end

subgraph Databases
    DB1[(Auth DB)]
    DB2[(Flights DB)]
    DB3[(Bookings DB)]
end

Client -->|Requests| AG

%% Config + Registration
AS -->|fetch config| CS
FS -->|fetch config| CS
BS -->|fetch config| CS
AG -->|fetch config| CS

AS -->|register| ER
FS -->|register| ER
BS -->|register| ER
AG -->|register| ER

%% Authentication & Routing Flow
AG -->|authenticate| AS
AS --> DB1


AG -->|valid token| FS
AG -->|valid token| BS

FS --> DB2
BS --> DB3
```

## Technologies Used :- 

  <p align="center"> <!-- Languages --> <img src="https://img.shields.io/badge/Java-007396?style=flat-square&logo=java&logoColor=white" /> <!-- Backend Framework --> <img src="https://img.shields.io/badge/Spring%20Boot-6DB33F?style=flat-square&logo=springboot&logoColor=white" /> <img src="https://img.shields.io/badge/Spring%20Cloud-73C59F?style=flat-square&logo=spring&logoColor=white" /> <!-- Microservices & Distributed Systems --> <img src="https://img.shields.io/badge/Microservices-FF6F00?style=flat-square&logo=cloudfoundry&logoColor=white" /> <img src="https://img.shields.io/badge/API%20Gateway-000000?style=flat-square&logo=cloudflare&logoColor=white" /> <img src="https://img.shields.io/badge/Eureka-FFCA28?style=flat-square&logo=googlecloud&logoColor=black" /> <img src="https://img.shields.io/badge/Config%20Server-00A1E0?style=flat-square&logo=apache&logoColor=white" /> <!-- Messaging --> <img src="https://img.shields.io/badge/RabbitMQ-FF6600?style=flat-square&logo=rabbitmq&logoColor=white" /> <!-- Database --> <img src="https://img.shields.io/badge/MongoDB-4EA94B?style=flat-square&logo=mongodb&logoColor=white" /> <!-- Security --> <img src="https://img.shields.io/badge/JWT-000000?style=flat-square&logo=jsonwebtokens&logoColor=white" /> <!-- Containerization --> <img src="https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=docker&logoColor=white" /> <img src="https://img.shields.io/badge/Docker%20Compose-2496ED?style=flat-square&logo=docker&logoColor=white" /> <!-- DevOps --> <img src="https://img.shields.io/badge/CI/CD-FFD700?style=flat-square&logo=githubactions&logoColor=black" /> <!-- Tools --> <img src="https://img.shields.io/badge/Postman-FF6C37?style=flat-square&logo=postman&logoColor=white" /> <!-- Version Control --> <img src="https://img.shields.io/badge/Git-F05032?style=flat-square&logo=git&logoColor=white" /> <img src="https://img.shields.io/badge/GitHub-181717?style=flat-square&logo=github&logoColor=white" /> </p>

## Services :-
```

| Service         | Path (through Gateway)                | Responsibility                           |
| --------------- | ------------------------------------- | ---------------------------------------- |
| API Gateway     | `http://localhost:8765`               | Routing & authentication                 |
| Auth Service    | `/auth-service/**`                    | Register/login, JWT generation           |
| Flight Service  | `/flightservice/**`                   | Flights CRUD & availability              |
| Booking Service | `/bookingservice/**`                  | Ticket booking & history                 |
| Config Server   | `http://localhost:8888`               | Central configuration                    |
| Eureka Registry | `http://localhost:8761`               | Service discovery dashboard              |
| MongoDB         | `mongodb://root:root@localhost:27017` | Databases: authdb, flightsdb, bookingsdb |
```

## Databases :-
```
| Service         | Database     | Collection(s) |
| --------------- | ------------ | ------------- |
| Auth Service    | `authdb`     | `users`       |
| Flight Service  | `flightsdb`  | `flights`     |
| Booking Service | `bookingsdb` | `bookings`    |
```
## Logging :-
Each service is configured to write logs into an app.log file inside the container, which is then mapped to the host via Docker volumes.
In each service config (centralized via Config Server):
```
logging.file.name=logs/app.log
logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss} %-5level %logger{36} - %msg%n
```
Folder Structure on host :-
```
logs/
├── auth-service/app.log
├── bookingservice/app.log
├── flightservice/app.log
├── api-gateway/app.log
└── server/app.log
```
## To run microservices :-

Clone the repository 
```
git clone https://github.com/saksham-0425/CHUBB_flight_microservice
```
```
cd CHUBB_flight_microservice
```
From the parent folder, run 
```
docker-compose up -d --build
```
All the services will start running.

## Screenshots :-

The container is up with all the services

<img width="1763" height="462" alt="image" src="https://github.com/user-attachments/assets/fde3a3a6-ce15-41bf-a4e8-d6e6b5754275" />

Check the status of all the services with
```
docker ps
```

<img width="1905" height="469" alt="image" src="https://github.com/user-attachments/assets/3aba8bbc-765f-420f-9a36-cdb60b28026f" />

Docker desktop showing all the services as running

<img width="1919" height="1079" alt="image" src="https://github.com/user-attachments/assets/c36ee0c2-e21a-443a-81bc-9ddfc1d0bdd2" />


All the services will register themselves on the eureka server

<img width="1899" height="633" alt="image" src="https://github.com/user-attachments/assets/7f8e5840-5dcd-4e81-a4df-b8fa2945c4f5" />

Working of the auth-service 

<img width="1252" height="588" alt="image" src="https://github.com/user-attachments/assets/f4a097ef-9021-4426-8ebd-f2c6616973de" />

Auth-service issues the jwt token for authentication based on role.

<img width="1259" height="715" alt="image" src="https://github.com/user-attachments/assets/a0b65845-df08-4c36-ae55-1068e553ed4e" />

Try accessing the protected route without the jwt token.

<img width="1263" height="722" alt="image" src="https://github.com/user-attachments/assets/5ecb79ed-7c31-40c0-b588-59955b95b459" />
