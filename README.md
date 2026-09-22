# NOC-Pulse --- Network Outage Management System

> A Java Spring Boot microservices + Angular application for managing
> the network outage lifecycle from alarm detection to incident
> resolution and reporting.

## 🔄 Core Workflow

``` text
Network Device
      ↓
Alarm Generated
      ↓
Alarm Service
      ↓
Incident Created
      ↓
Engineer Assigned
      ↓
Incident Worked On
      ↓
Resolved & Verified
      ↓
Incident Closed
      ↓
Dashboard & Reports Updated
```

## 🎯 Problem Statement

NOC-Pulse centralizes network outage management for enterprise/telecom
environments. It connects device management, alarm handling, incident
management, engineer assignment, SLA tracking, resolution verification,
and reporting in one platform.

## 🏗️ Architecture

``` text
                Angular 17
                    │
                    ▼
             API Gateway :8080
                    │
       ┌────────────┼────────────┐
       ▼            ▼            ▼
    Auth         Device        Alarm
   :8081         :8082         :8083
                                  │
                                  ▼
                             Incident
                              :8084
                                  │
                                  ▼
                         Dashboard/Reporting
                              :8085

              Eureka Server :8761
             Service Discovery
```

### Services

  Service                 Port Responsibility
  --------------------- ------ ---------------------------------
  Eureka Server           8761 Service discovery
  API Gateway             8080 Request routing
  Auth Service            8081 Authentication, users & roles
  Device Service          8082 Network device management
  Alarm Service           8083 Alarm & severity management
  Incident Service        8084 Incidents, assignment & SLA
  Dashboard/Reporting     8085 Aggregated dashboards & reports

## 🛠️ Tech Stack

**Backend:** Java 17, Spring Boot, Spring Web, Spring Data JPA,
Hibernate, Spring Security, JWT, Maven

**Microservices:** Spring Cloud Gateway, Eureka, OpenFeign, LoadBalancer

**Database:** H2

**Frontend:** Angular 17, TypeScript, HTML, SCSS, RxJS

## ⭐ Key Features

-   JWT authentication and role-based access control
-   Network device inventory and device history
-   Alarm creation, severity classification and deduplication
-   Automatic incident creation from alarms
-   Engineer assignment based on specialization, skills and workload
-   Incident lifecycle:
    `NEW → ASSIGNED → IN_PROGRESS → RESOLVED → CLOSED`
-   SLA monitoring for incidents
-   Resolution verification and progress tracking
-   Dashboard aggregation across multiple services
-   Reporting and audit visibility

## 🔗 Microservice Communication

``` text
Angular
   ↓
API Gateway
   ↓
Microservices

Alarm Service ──Feign──→ Device Service
Alarm Service ──Feign──→ Incident Service

Dashboard Service ──Feign──→ Device Service
Dashboard Service ──Feign──→ Alarm Service
Dashboard Service ──Feign──→ Incident Service
```

Eureka provides service discovery between microservices.

## 🔐 Security

``` text
Login
  ↓
Auth Service
  ↓
JWT Token
  ↓
Angular HTTP Interceptor
  ↓
API Gateway / Services
  ↓
JWT + Role Validation
```

Roles include `ADMIN`, `NOC_OPERATOR`, `ENGINEER`, and `VIEWER`.

## 💾 Database Architecture

``` text
Auth Service       → authdb
Device Service     → devicedb
Alarm Service      → alarmdb
Incident Service   → incidentdb
Dashboard Service  → No business database
```

## 🚀 Running the Project

### Prerequisites

-   Java 17
-   Maven
-   Node.js / npm
-   Angular CLI 17

### Backend

Start the services in this order:

``` text
1. Eureka Server
2. API Gateway
3. Auth Service
4. Device Service
5. Alarm Service
6. Incident Service
7. Dashboard/Reporting Service
```

Run a Spring Boot service:

``` bash
mvn spring-boot:run
```

### Frontend

``` bash
npm install
ng serve
```

Frontend:

``` text
http://localhost:4200
```

Gateway:

``` text
http://localhost:8080
```

Eureka:

``` text
http://localhost:8761
```

## 📌 Project Highlights

This project demonstrates practical exposure to:

-   Java & Spring Boot
-   REST API development
-   Microservices architecture
-   API Gateway & Service Discovery
-   Inter-service communication with OpenFeign
-   JWT & Spring Security
-   JPA/Hibernate
-   Angular frontend development
-   SLA and incident lifecycle management
-   Enterprise workflow design
