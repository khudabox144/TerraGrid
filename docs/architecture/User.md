# User Management Module Documentation

## Overview
The User Management Module handles all user-related operations including creation, retrieval, updating, and deletion of users. It follows clean architecture principles, SOLID design patterns, and Spring Boot best practices.

## Architecture

### Layered Architecture
```
┌─────────────────────────────────────────────────────────────┐
│                     Presentation Layer                       │
│                    (UsersController)                         │
├─────────────────────────────────────────────────────────────┤
│                     Business Layer                          │
│                     (UserService)                           │
├─────────────────────────────────────────────────────────────┤
│                     Data Access Layer                       │
│                   (UserRepository)                          │
├─────────────────────────────────────────────────────────────┤
│                     Database Layer                          │
│                   (User Entity)                             │
└─────────────────────────────────────────────────────────────┘
```

## Component Breakdown

### 1. Controller Layer (`UsersController`)

**Purpose**: Handles HTTP requests and responses, acts as the entry point for all user-related API calls.

**Responsibilities**:
- Receives HTTP requests from clients
- Validates input data (with `@Valid` annotation)
- Delegates business logic to the service layer
- Returns appropriate HTTP responses with proper status codes
- Logs request information for monitoring

**Key Features**:
```java
@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class UsersController {
    // All endpoints are secured with ADMIN role
    // Uses constructor injection for dependencies
    // Consistent response formatting
}
```

**Endpoints**:
| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| GET | `/api/users` | Get all users | None | `List<UserResponse>` |
| GET | `/api/users/{id}` | Get user by ID | None | `UserResponse` |
| POST | `/api/users` | Create new user | `UserCreateRequest` | `UserResponse` (201 Created) |
| PATCH | `/api/users/{id}/role` | Update user role/status | `UserUpdateRequest` | `UserResponse` |
| DELETE | `/api/users/{id}` | Delete user | None | 204 No Content |

### 2. Service Layer (`UserService`)

**Purpose**: Contains all business logic and serves as a bridge between controllers and repositories.

**Responsibilities**:
- Implements business rules and validations
- Orchestrates data operations
- Handles transactions
- Manages password encoding
- Maps between DTOs and entities
- Throws appropriate exceptions

**Key Features**:
```java
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {
    // All write operations are transactional
    // Comprehensive error handling
    // Business validation logic
    // Clear method naming
}
```

**Business Logic**:
- Prevents duplicate email registration
- Encodes passwords before storage
- Soft delete capability (activate/deactivate)
- Role-based access control
- Proper exception handling

### 3. DTO Layer (Data Transfer Objects)

**Purpose**: Decouple internal entity structure from API contracts.

#### UserCreateRequest
Used for user creation operations:
```java
public class UserCreateRequest {
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
    
    @NotNull(message = "Role is required")
    private Role role;
}
```

#### UserUpdateRequest
Used for partial updates:
```java
public class UserUpdateRequest {
    private Role role;
    
    @NotNull(message = "isActive status cannot be null")
    private Boolean isActive;
}
```

#### UserResponse
Used for API responses:
```java
public class UserResponse {
    private Integer id;
    private String name;
    private String email;
    private Role role;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### 4. Mapper Layer (`UserMapper`)

**Purpose**: Converts between entities and DTOs, centralizing transformation logic.

**Benefits**:
- Centralizes mapping logic
- Easy to test
- Changes to entity structure don't affect API
- Reduces boilerplate code

```java
@Component
public class UserMapper {
    public User toEntity(UserCreateRequest request) {
        // Convert DTO to Entity
    }
    
    public UserResponse toResponse(User user) {
        // Convert Entity to DTO
    }
}
```

## SOLID Principles Applied

### Single Responsibility Principle (SRP)
- **Controller**: Handles only HTTP concerns
- **Service**: Contains only business logic
- **Repository**: Only data access
- **Mapper**: Only object conversion
- **DTOs**: Only data transfer

### Open/Closed Principle (OCP)
- New functionality can be added by extending mappers or services
- DTOs can be extended without modifying existing code

### Liskov Substitution Principle (LSP)
- DTOs can be substituted with their implementations
- Service interfaces enable substitution

### Interface Segregation Principle (ISP)
- Each layer has a focused interface
- Controllers depend only on service interfaces

### Dependency Inversion Principle (DIP)
- Controllers depend on service abstractions
- Services depend on repository abstractions
- Use constructor injection for loose coupling

## Best Practices Implemented

### 1. Security
- Role-based access control (`@PreAuthorize`)
- Password encoding with BCrypt
- Input validation
- No exposure of sensitive data

### 2. Error Handling
- Custom exceptions (`ResourceNotFoundException`, `UserAlreadyExistsException`)
- Consistent error responses
- Graceful error propagation

### 3. Logging
- Comprehensive logging at all levels
- Different log levels for different situations
- Logging request/responses for debugging

### 4. Validation
- Bean validation with JSR-303
- Custom validation messages
- Validation at both controller and service levels

### 5. Transaction Management
- `@Transactional` for write operations
- Rollback on exceptions
- Consistent data integrity

### 6. RESTful Design
- Proper HTTP methods (GET, POST, PATCH, DELETE)
- Appropriate HTTP status codes
- Resource-oriented URLs
- Stateless operations

### 7. Code Quality
- Clean method naming
- Meaningful variable names
- Minimal method length
- Single responsibility per method

## Exception Handling

### Custom Exceptions
```java
// Resource not found
public class ResourceNotFoundException extends RuntimeException

// User already exists
public class UserAlreadyExistsException extends RuntimeException
```

### Global Exception Handler (Recommended)
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage()));
    }
    
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleConflict(UserAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ex.getMessage()));
    }
}
```

## Database Schema

```sql
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_role (role),
    INDEX idx_is_active (is_active)
);
```

## API Usage Examples

### Create User
```bash
POST /api/users
Authorization: Bearer <admin-token>
Content-Type: application/json

{
    "name": "John Doe",
    "email": "john@example.com",
    "password": "securepassword123",
    "role": "ADMIN"
}
```

### Update User Role
```bash
PATCH /api/users/1/role
Authorization: Bearer <admin-token>
Content-Type: application/json

{
    "role": "USER",
    "isActive": true
}
```

### Get All Users
```bash
GET /api/users
Authorization: Bearer <admin-token>
```

### Delete User
```bash
DELETE /api/users/1
Authorization: Bearer <admin-token>
```

## Testing Strategy

### Unit Tests
- Test controller layer with mock services
- Test service layer with mock repositories
- Test mapper functionality
- Test validation logic

### Integration Tests
- Test entire flow from controller to database
- Test transaction management
- Test exception handling
- Test security annotations

### Sample Test
```java
@SpringBootTest
@AutoConfigureMockMvc
class UsersControllerTest {
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_ShouldReturnCreatedUser() throws Exception {
        // Test implementation
    }
}
```

## Performance Considerations

1. **Connection Pooling**: Using HikariCP for optimal connection management
2. **Caching**: Consider adding Redis cache for frequently accessed data
3. **Pagination**: Implement pagination for large user lists
4. **Indexing**: Database indexes on frequently queried fields
5. **Lazy Loading**: Proper handling of relationships to avoid N+1 queries

## Monitoring and Observability

### Key Metrics to Track
- User creation rate
- Active vs inactive users
- Role distribution
- API response times
- Error rates

### Logging Strategy
- **INFO**: User creation, updates, deletions
- **DEBUG**: Detailed operation logs
- **ERROR**: Exceptions and failures
- **WARN**: Potential issues

## Future Enhancements

1. **Email Service**: Send welcome emails on user creation
2. **Audit Trail**: Track all user operations
3. **Pagination**: Support for paginated user lists
4. **Search/Filter**: Advanced user search capabilities
5. **Batch Operations**: Bulk user updates
6. **Two-Factor Authentication**: Enhanced security
7. **Profile Pictures**: User avatar management
8. **Soft Delete**: Implement soft delete pattern

## Dependencies

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </dependency>
</dependencies>
```

## Conclusion

This User Management Module follows industry best practices and SOLID principles to ensure:
- **Maintainable code**: Clear separation of concerns
- **Scalable**: Easy to add new features
- **Secure**: Role-based access and input validation
- **Testable**: Well-structured for unit and integration tests
- **Documented**: Clear API contracts and business logic
