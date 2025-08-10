# Task Management System

A cloud-based web application providing basic task management with essential CRUD operations.

### 🖧 System Architecture

<details> <summary>System Architecture </summary> <pre>

```mermaid
graph TB
%% External
    subgraph External["`**External**`"]
        Client["`**Client**
        REST API calls`"]
    end

%% Core Layers
    subgraph API["`**REST API Layer**`"]
        Controller["`**TaskController** 
        @RestController`"]
    end

    subgraph Business["`**Business Layer**`"]
        Service["`**TaskService** 
        @Service`"]
    end

    subgraph Data["`**Data Layer**`"]
        Repository["`**TaskRepository**
        @Repository`"]
        Database["`**H2 Database**
        jdbc:h2:mem:testdb`"]
    end

%% Models
    subgraph Models["`**Models**`"]
        TaskEntity["`**Task @Entity**
        @Table(name='tasks')`"]
        TaskDTO["`**TaskDto**
        Data Transfer Object`"]
        Status["`**TaskStatus**
        (TO_DO,IN_PROGRESS,DONE)`"]
    end

%% Flow
    Client --> Controller
    Controller --> Service
    Service --> Repository
    Repository --> Database
%% Model Usage
    Service -.-> TaskEntity
    Service -.-> TaskDTO
    TaskEntity -.-> Status
    TaskDTO -.-> Status
```

</pre> </details>
<details> <summary>API Workflow</summary> <pre>

```mermaid
sequenceDiagram
    participant C as Client
    participant TC as TaskController
    participant TS as TaskService
    participant TR as TaskRepository
    C ->> TC: POST /tasks
    TC ->> TS: createTask(taskDto)
    TS ->> TR: save(task)
    TR -->> TS: savedTask
    TS -->> TC: taskDto
    TC -->> C: 201 Created
    C ->> TC: GET /tasks
    TC ->> TS: getAllTasks()
    TS ->> TR: findAll()
    TR -->> TS: taskList
    TS -->> TC: taskDtoList
    TC -->> C: 200 OK
```

</pre> </details>
<details> <summary>Project Structure</summary> <pre>

```
tasksys/
├── pom.xml
├── .gitignore
├── ms.resume.md
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── dev/
│   │   │       └── tasksys/
│   │   │           ├── TasksysApplication.java
│   │   │           ├── config/
│   │   │           │   └── OpenApiConfig.java
│   │   │           ├── controller/
│   │   │           │   └── TaskController.java
│   │   │           ├── exception/
│   │   │           │   ├── GlobalExceptionHandler.java
│   │   │           │   └── TaskNotFoundException.java
│   │   │           ├── model/
│   │   │           │   ├── Task.java
│   │   │           │   ├── TaskDto.java
│   │   │           │   └── TaskStatus.java
│   │   │           ├── repository/
│   │   │           │   └── TaskRepository.java
│   │   │           └── service/
│   │   │               └── TaskService.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── data.sql
│   └── test/
│       ├── java/
│       │   └── dev/
│       │       └── tasksys/
│       │           ├── TasksysApplicationTest.java
│       │           ├── controller/
│       │           │   └── TaskControllerTest.java
│       │           ├── integration/
│       │           │   └── TaskIntegrationTest.java
│       │           ├── repository/
│       │           │   └── TaskRepositoryTest.java
│       │           └── service/
│       │               └── TaskServiceTest.java
│       └── resources/
│           ├── application-test.properties
│           └── test-data.sql
└── target/
```

</pre> </details>

### Prerequisites

- Java 21 or later
- Maven 3.6+

### Getting Started

```bash
# Build
mvn clean install
# Run
mvn spring-boot:run
```

### Application Access

- API: `http://localhost:8080/api/tasks`
- H2 Console: `http://localhost:8080/h2-console`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

### API Endpoints

- `POST /api/tasks` — Create a task
- `GET /api/tasks` — List all tasks
- `GET /api/tasks/{id}` — Retrieve a task by ID
- `PUT /api/tasks/{id}` — Update a task by ID
- `DELETE /api/tasks/{id}` — Delete a task by ID
- `GET /api/tasks/status/{status}` — Filter tasks by status

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
