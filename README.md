# LakeSide Hotel Backend

A Spring Boot–based backend application for managing hotel operations such as guest bookings, user authentication, and room services.

## 🧰 Tech Stack

- Java 17+
- Spring Boot 3.x
- Spring Security
- Spring Data JPA
- MySQL
- Maven

## 🚀 Getting Started

### Prerequisites

- Java 17 or 21
- Maven 3.8+
- MySQL Server

### Setup Instructions

1. **Clone the repository**
   ```bash
   git clone https://github.com/GosuCode/LakeSide-Hotel-Backend.git
   cd LakeSide-Hotel-Backend

2. **Configure the database**

   Create a MySQL database named `lakeside_hotel` and update the credentials in `application.properties`:

   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/lakeSide_hotel_db
   spring.datasource.username=your_db_user
   spring.datasource.password=your_db_password

3. **Build and run the project**

   ```bash
   mvn clean install
   mvn spring-boot:run

## 📦 API Overview

This application exposes REST APIs for:

* User registration and login (with JWT authentication)
* Room management
* Booking operations
* Guest profile handling

API documentation (e.g., Swagger) may be added in future updates.

## 🧪 Testing

Run tests with:

```bash
mvn test
```

## 📄 License

This project is open source and available under the [MIT License](LICENSE).
