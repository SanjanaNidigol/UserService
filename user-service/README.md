# user-service

Java 21 • Spring Boot 3.3.3 • Maven 3.5.5+ • MySQL • Kafka • Eureka Client

## Quick start

1. Update `spring.datasource.password` in `src/main/resources/application.yml`.
2. Start infrastructure locally (MySQL, Kafka, Zookeeper, Eureka Server).
3. Run the app:
   ```bash
   ./mvnw spring-boot:run
   ```
   or from IntelliJ.
4. Sample endpoints:
   - user-service:
     - `POST /api/users/register` -> `{"email":"a@b.com","mpin":"1234"}`
     - `POST /api/users/{id}/verify-mpin` -> `{"mpin":"1234"}`
   - account-service:
     - `POST /api/accounts` -> `{"userId":1,"accountNumber":"123456789012"}`
     - `POST /api/accounts/deposit` -> `{"accountNumber":"123456789012","amount":1000}`
   - transaction-service:
     - `POST /api/transactions/transfer` -> `{"fromAccount":"123","toAccount":"456","amount":500}`

All services publish Kafka events; auditlog-service consumes and persists them.
