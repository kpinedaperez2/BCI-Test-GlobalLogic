# 🧭 BCI — User Authentication Service

Microservice for **user registration and authentication**, implemented with **Java 11 + Spring Boot 2.5.14** using a **Hexagonal Architecture (Ports & Adapters)**.  
It provides endpoints to sign up new users and log in with **JWT**-based authentication.

---

## 📑 Table of Contents
1. [Features](#-features)  
2. [Tech Stack](#-tech-stack)  
3. [Project Structure](#-project-structure)  
4. [Getting Started](#-getting-started)  
5. [API Endpoints](#-api-endpoints)  
6. [Configuration](#-configuration)  
7. [Docker & Docker Compose](#-docker--docker-compose)  
8. [Testing & Coverage](#-testing--coverage)  
9. [Design Decisions](#-design-decisions)  
10. [Contributing](#-contributing)  
11. [License](#-license)

---

## ✨ Features
- REST API with **/sign-up** and **/login** endpoints.  
- **JWT** token generation and validation.  
- Password rules (length, uppercase, digits, alphanumeric only).  
- In-memory **H2 database** with web console enabled.  
- **Hexagonal Architecture**: Domain, Application, Infrastructure, Router.  
- **MapStruct** for object mapping.  
- **Lombok** to reduce boilerplate.  
- Unit and integration tests with **JUnit 5 + Mockito**.  

---

## 🛠 Tech Stack
- **Java 11**  
- **Spring Boot 2.5.14**  
- **Spring Data JPA + H2**  
- **JWT (jjwt)**  
- **MapStruct**  
- **Lombok**  
- **Gradle (wrapper included)**  
- **Jacoco** (coverage)  
- **Docker / Docker Compose**  

---

## 📂 Project Structure
```
src/
 └─ main/
    ├─ java/com/local/bci/
    │  ├─ BCITestApplication.java     # main class
    │  ├─ router/                     # controllers + exceptions
    │  ├─ application/                # DTOs, mappers, use cases
    │  ├─ domain/                     # models + ports
    │  └─ infrastructure/             # adapters, persistence, security
    └─ resources/
       └─ application.yml             # config (db, regex, jwt)
```

---

## 🚀 Getting Started

### Prerequisites
- Java 11+
- Docker (optional, if using Docker setup)

### Run locally (Gradle)
```bash
./gradlew clean build
./gradlew bootRun
```

App will start at:  
👉 `http://localhost:8080`

### Run with Jar
```bash
java -jar build/libs/*.jar
```

### Run tests
```bash
./gradlew test
```

### Coverage report
```bash
./gradlew jacocoTestReport
# open: build/reports/jacoco/test/html/index.html
```

---

## 📡 API Endpoints

### 🔹 Sign Up
**POST** `/sign-up`

**Request:**
```json
{
  "name": "Test globallogic",
  "email": "test@example.com",
  "password": "Abcdef12",
  "phones": [
    { "number": 1234567890, "cityCode": 11, "countryCode": "54" }
  ]
}
```

**Response happy path:**
```json
{
  "id": "UUID",
  "created": "2025-09-14T12:34:56",
  "lastLogin": null,
  "token": "token",
  "isActive": true,
  "name": "Test globallogic",
  "email": "test@example.com",
  "phones": [ { "number": 1234567890, "cityCode": 11, "countryCode": "54" } ]
}
```

**Response if email dont match with requested pattern:**
```json
{
    "error": [
        {
            "timestamp": "2025-09-15T18:53:25.697708500Z",
            "codigo": 400,
            "detail": "email: debe ser una dirección de correo electrónico con formato correcto"
        }
    ]
}
```

**Response if password dont match with requested pattern:**
```json
{
    "error": [
        {
            "timestamp": "2025-09-15T18:55:27.618728100Z",
            "codigo": 400,
            "detail": "Invalid password format"
        }
    ]
}
```

**Response if user already exist:**
```json
{
    "error": [
        {
            "timestamp": "2025-09-15T18:58:09.329604800Z",
            "codigo": 409,
            "detail": "User already exists"
        }
    ]
}
```
---

### 🔹 Login
**POST** `/login`

**Headers:**
```
Authorization: Bearer <token>
```

**Response happy path:**
```json
{
    "id": "cd0a2fee-49be-4d48-a7a2-60dbe2ee5a75",
    "created": "2025-09-15T15:10:00.296369",
    "lastLogin": "2025-09-15T15:10:04.7792775",
    "token": "token",
    "isActive": true,
    "name": "Test globallogic",
    "email": "test@example.com",
    "password": "encriptedPassword",
    "phones": [
        {
            "number": 123456789,
            "cityCode": 11,
            "countryCode": "+54"
        },
        {
            "number": 987654321,
            "cityCode": 34,
            "countryCode": "+1"
        }
    ]
}
```

**Response if request have an incorrect token :**
```json
{
    "error": [
        {
            "timestamp": "2025-09-15T18:56:50.973128500Z",
            "codigo": 400,
            "detail": "Invalid token"
        }
    ]
}
```

---

## ⚙️ Configuration

File: `src/main/resources/application.yml`

👉 Access H2 console: `http://localhost:8080/h2-console`  
JDBC URL: `jdbc:h2:mem:usersdb`, User: `sa`

### JWT / Regex
```
    Email pattern must be like "aaaaaaa@undominio.alg" or will return error

    The password must match the following rules:
    - Length between **8 and 12** characters  
    - Exactly **one uppercase letter**  
    - Exactly **two digits** (not necessarily consecutive)  
    - All other characters must be lowercase letters  
    - Only alphanumeric characters allowed  

```

---

### Postman
```
  You can take a postman collection and environment from the app, "BCI.postman_environment.json" and "BCI GlobalLogic Test.postman_collection.json", import them in postman to execute the endpoints
```
---

## 🐳 Docker & Docker Compose

### Build image
```bash
docker build -t bci-test .
```

### Run container
```bash
docker run -p 8080:8080 bci-test
```

### Run with docker-compose
```bash
docker-compose up --build
```

By default exposes:  
- API → `http://localhost:8080`  
- H2 Console → `http://localhost:8080/h2-console`  

---

## 🧪 Testing & Coverage

Run all tests:
```bash
./gradlew test
```

Coverage:
```bash
./gradlew jacocoTestReport
```
---

## 🧩 Design Decisions
- **Hexagonal Architecture** with clear separation:  
  - *Domain* → core models and ports.  
  - *Application* → DTOs, mappers, use cases.  
  - *Infrastructure* → persistence, security, adapters.  
  - *Router* → REST controllers and exception handling.  
- **Functional interfaces** for use cases (extending `java.util.function.Function`).  
- **JWT** stored in DB and renewed at login.  
- **Password rules enforced** via regex in `application.yml`.  
- **MapStruct + Lombok** for cleaner, maintainable code.
- **The component and sequence diagrams have been included in the app as images (Components, Login, SignUp).**

---

## 🤝 Contributing
1. Fork the repo  
2. Create your branch (`git checkout -b feature/xyz`)  
3. Commit changes (`git commit -m "Add xyz"`)  
4. Push branch (`git push origin feature/xyz`)  
5. Open a Pull Request  

---

## 📜 License
This project is unlicensed. Add a [LICENSE](LICENSE) file (MIT recommended) if you plan to share publicly.

---

