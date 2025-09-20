# Task Management System

A cloud-based web application providing task management with user authentication and essential CRUD operations.

### Features
- **Task Management**: Each user manages their own tasks
- **CRUD Operations**: Create, Read, Update, Delete tasks
- **Task Status**: TO_DO, IN_PROGRESS, DONE
- **REST API**: Full REST API with Swagger documentation
- **Security**: Protected endpoints with proper authorization

### 🖧 System Architecture
<details> <summary>System Design </summary> <pre>

```mermaid
graph TB
    subgraph Client["Client Layer"]
        FE[Frontend/Postman/Swagger UI]
    end
    
    subgraph API["API Layer"]
        AC[AuthController<br>/api/auth/*]
        TC[TaskController<br>/api/tasks/*]
    end
    
    subgraph Security["Security Layer"]
        JWT[JWT Filter]
        AUTH[Authentication]
    end
    
    subgraph Service["Service Layer"]
        US[UserService]
        TS[TaskService]
    end
    
    subgraph Data["Data Layer"]
        UR[UserRepository]
        TR[TaskRepository]
        DB[(H2/PostgreSQL<br>Database)]
    end
    
    FE --> AC
    FE --> TC
    TC --> JWT
    JWT --> AUTH
    AUTH --> TS
    AC --> US
    TS --> TR
    US --> UR
    TR --> DB
    UR --> DB
```

</pre> </details>
<details> <summary>ER Diagram</summary> <pre>

```mermaid
erDiagram
    USERS {
        bigint id PK
        varchar username UK
        varchar email UK  
        varchar password
        timestamp created_at
        boolean enabled
    }
    
    TASKS {
        bigint id PK
        varchar title
        varchar description
        date due_date
        varchar status
        bigint user_id FK
    }
    
    USERS ||--o{ TASKS : owns
```

</pre> </details>
<details> <summary>Security Model</summary> <pre>

```mermaid
graph LR
    subgraph Public["🌐 Public Endpoints"]
        LOGIN["/api/auth/login"]
        REGISTER["/api/auth/register"]
        SWAGGER["/swagger-ui/**"]
    end
    
    subgraph Protected["🔒 Protected Endpoints"]
        TASKS["/api/tasks/**"]
    end
    
    subgraph Auth["🛡️ Authentication"]
        JWT_TOKEN["JWT Token<br>(expires 24h)"]
        USER_CONTEXT["User Context<br>(SecurityContext)"]
    end
    
    LOGIN --> JWT_TOKEN
    REGISTER --> JWT_TOKEN
    JWT_TOKEN --> USER_CONTEXT
    USER_CONTEXT --> TASKS
    
    TASKS --> |"Only user's own tasks"| USER_DATA[("👤 User's Tasks")]
```

</pre> </details>
<details> <summary>Authentication Flow</summary> <pre>

```mermaid
sequenceDiagram
    participant C as Client
    participant AC as AuthController
    participant US as UserService
    participant JWT as JwtUtil
    participant DB as Database
    
    Note over C,DB: User Registration
    C->>AC: POST /api/auth/register
    AC->>US: createUser(username, email, password)
    US->>DB: save(hashedPassword)
    DB-->>US: User created
    US-->>AC: User object
    AC->>JWT: generateToken(user)
    JWT-->>AC: JWT token
    AC-->>C: {token, username, email}
    
    Note over C,DB: User Login
    C->>AC: POST /api/auth/login
    AC->>US: authenticate(username, password)
    US->>DB: findByUsername(username)
    DB-->>US: User + hashedPassword
    US-->>AC: Authenticated user
    AC->>JWT: generateToken(user)
    JWT-->>AC: JWT token
    AC-->>C: {token, username, email}
```

</pre> </details>
<details> <summary>Task Management Flow</summary> <pre>

```mermaid
sequenceDiagram
    participant C as Client
    participant F as JWT Filter
    participant TC as TaskController
    participant TS as TaskService
    participant TR as TaskRepository
    participant DB as Database
    
    Note over C,DB: Get Tasks (Protected)
    C->>F: GET /api/tasks<br>Authorization: Bearer <token>
    F->>F: Validate JWT & extract user
    F->>TC: Forward request + user context
    TC->>TS: getAllTasks()
    TS->>TS: getCurrentUser() from SecurityContext
    TS->>TR: findByUserId(currentUser.id)
    TR->>DB: SELECT * FROM tasks WHERE user_id = ?
    DB-->>TR: User's tasks
    TR-->>TS: List<Task>
    TS-->>TC: List<TaskDto>
    TC-->>C: User's tasks only
    
    Note over C,DB: Create Task
    C->>F: POST /api/tasks + TaskDto<br>Authorization: Bearer <token>
    F->>TC: Forward with user context
    TC->>TS: createTask(taskDto)
    TS->>TS: getCurrentUser()
    TS->>TS: task.setUser(currentUser)
    TS->>TR: save(task)
    TR->>DB: INSERT task with user_id
    DB-->>TR: Saved task
    TR-->>TS: Task entity
    TS-->>TC: TaskDto
    TC-->>C: 201 Created
```

</pre> </details>
<details> <summary>Configuration Profiles</summary> <pre>****

```mermaid
graph TD
    subgraph DEV["🔧 Development Mode"]
        H2[(H2 Database<br>In-Memory)]
        SAMPLE[Sample Users & Tasks]
        CONSOLE[H2 Console /h2-console]
    end
    
    subgraph PROD["🚀 Production Mode"]
        PG[(PostgreSQL<br>Persistent)]
        DOCKER[Docker Compose]
        ENV[Environment Variables]
    end
    
    APP[Spring Boot App]
    
    APP -.->|default profile| DEV
    APP -.->|prod profile| PROD
```

</pre> </details>

### Prerequisites
- Java 21 or later
- Maven 3.6+
- Docker & Docker Compose

### Getting Started

**Clone the repository**
   ```bash
   git clone <github.com/mitchell7970/tasksys/>
   cd tasksys
   ```

**Start with H2 (Development Mode)**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

**Start with PostgreSQL & pgAdmin4 (Production Mode)**
   ```bash
   docker-compose up -d postgres pgadmin
   mvn spring-boot:run -Pprod
   ```

**Full Docker Setup**
   ```bash
   mvn clean package
   docker-compose up --build -d
   ```

### Application Access

- **API**: `http://localhost:8080/api`
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **H2 Console**: `http://localhost:8080/h2-console` (Development Mode)
- **pgAdmin4**: `http://localhost:5050/` (Production Mode)

### API Endpoints
- #### Authentication
    - `POST /api/auth/login` — Login user
    - `POST /api/auth/register` — Register new user
- #### Tasks (Protected - Requires JWT Token)
    - `POST /api/tasks` — Create a task
    - `GET /api/tasks` — List user's tasks
    - `GET /api/tasks/{id}` — Get specific task
    - `PUT /api/tasks/{id}` — Update task
    - `DELETE /api/tasks/{id}` — Delete task
    - `GET /api/tasks/status/{status}` — Filter tasks by status

### Security
- JWT tokens expire in 24 hours
- Passwords hashed with BCrypt
- CORS enabled for development
- Protected endpoints require valid JWT
- Users can only access their own tasks

### Testing
```bash
# Run all tests
mvn test
# Run tests with coverage
mvn clean verify
# Run specific test class
mvn test -Dtest=TaskControllerTest
# Run integration tests
mvn test -Dtest="*IntegrationTest"
```

### Test Coverage
- Unit Tests: Controllers, Services, Repositories
- Integration Tests: Full API workflow
- Test Profiles: Separate H2 configuration for testing