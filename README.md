# shopping-service
This is a demo application

### Constitution

```aiignore
/skill:speckit-constitution
- Java 21 LTS is mandatory.
- Use the latest stable Spring Boot version.
- Maven is the standard build tool.
- Follow REST API best practices and OpenAPI 3 documentation.
- Use PostgreSQL as the primary database.
- Use JUnit 5, Mockito, and Testcontainers for testing.
- Use feature-based package structure.
- Use Java Records for DTOs where appropriate.
- Implement input validation using Jakarta Validation.
- Use Spring Boot Actuator and Micrometer for observability.
- Provide Docker support.
- Use secure coding practices, externalized configuration, and no hard-coded secrets.
- Prefer clean code, SOLID principles, and maintainable architecture.
- Require API versioning and standardized error responses.
- Define governance, compliance, and amendment rules for the constitution.
```
```aiignore
/skill:speckit-constitution
Jackson2 is not allowed; use Jackson3 for JSON serialization/deserialization.
```
### Specify
```aiignore
/skill:speckit-specify 
Expose the follwoing APIs:
GET /api/v1/products
POST /api/v1/orders
GET /api/v1/orders/{id}
DELETE /api/v1/orders/{id}
```