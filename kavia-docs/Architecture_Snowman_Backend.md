# Architecture Document: Snowman Backend

## System Context

Snowman is an enterprise backend service for managing employees, projects, users, and clients in a fictional organization. The backend is implemented as a single Spring-based monolith, packaged as an Uber JAR, running an embedded Jetty HTTP server. It persists data in MySQL, exchanges events/messages with external systems via ActiveMQ JMS, applies a caching layer with Ehcache, and manages DB schema with Liquibase.

### Container Diagram Narrative

- **Spring Backend Container**: The core application, including REST APIs, hexagonal domain/application logic, adapters for persistence, messaging, and caching.
- **MySQL Database**: Stores all persistent business data.
- **ActiveMQ**: Message broker for asynchronous integration/events.
- **Ehcache**: In-process caching for high-frequency data access.
- **External Systems**: Communicate by consuming/producing JMS messages.

_See `etc/SystemComponents.png` and `etc/HexagonalArchitecture.png` for system overview._

## Architectural Style: Hexagonal (Ports & Adapters)

Snowman uses hexagonal architecture:
- **Core Domain**: Business entities and logic, isolated from technology and infra.
- **Ports**: Interfaces that drive and are driven by the core (e.g., repository, messaging, REST APIs).
- **Adapters**: Implementations of ports, wiring in frameworks and external systems.

**In this project:**
- Domain: `domain/` — Entity models, core interfaces, and services.
- Application (Ports): `application/` — Use-case/service interfaces, cache/messaging ports.
- Adapters: `infrastructure/` — REST endpoints, JPA repositories, JMS consumers/producers, caches.

## Technology Stack

- **Language/Framework**: Java 7+, Spring Framework
- **HTTP Server**: Embedded Jetty
- **REST**: Spring MVC annotations/controllers (`infrastructure/rest/endpoint/`)
- **Persistence**: JPA + Hibernate, JDBC, DAOs (`domain/repository/`, `infrastructure/db/dao/`)
- **Database**: MySQL
- **Messaging**: JMS (ActiveMQ), adapters and DTO mapping
- **Caching**: Ehcache (`application/cache/`, `infrastructure/cache/`)
- **Migrations**: Liquibase changelogs (`resources/db/changelog/`)
- **Packaging**: Uber JAR via Maven Shade Plugin
- **Runtime**: Standalone JAR; default on server.port=3001 (overridable)

## Module Boundaries

- **Domain Layer**: Entities (`domain/model/`), exceptions, repository interfaces, business services.
- **Application Layer**: Use case services, cache port/service interfaces.
- **Infrastructure Layer**: 
  - REST adapters (controllers, mappers, resources)
  - Data adapters (JPA/Hibernate/JDBC DAO impls)
  - Messaging adapters (JMS producers/consumers)
  - Cache adapters (Ehcache integration)

## Data Model Overview

**Entities** (see `resources/etc/entity-relationship.png`):
- `Employee`: Employee record, roles, project associations.
- `Project`: Projects, with assignment to employees, clients.
- `User`: Authentication and application access (simplified, auth stubbed).
- `Client`: Client organizations, each can have multiple projects.
- `EmployeeProject`: Join entity for many-to-many between employees and projects.

All relationships modeled with Hibernate/JPA annotations.

## API Surface Overview

### Core REST Endpoints
- `/employee` (GET, POST, PUT, DELETE): Manage employees.
- `/project` (GET, POST, PUT, DELETE): Manage projects.
- `/user` (GET, POST, PUT, DELETE): Manage application users.
- `/client` (GET, POST, PUT, DELETE): Manage clients.

Supports pagination, filtering, error reporting via structured JSON.

### Non-functional endpoints
- `/healthcheck`, `/status`: App and database health.
- `/cache/*`: For cache inspection (admin/internal).

## Data Access and Transaction Management

- DAOs encapsulate JPA EntityManager for persistence (`infrastructure/db/dao/impl/`).
- Transaction boundaries managed by Spring (typically at service layer).
- Both JDBC and JPA/Hibernate data access patterns present for demo.
- Optimistic locking recommended for concurrent update scenarios.

## Messaging Flows with ActiveMQ

- Named JMS ports and adapters decouple event flow.
- Adapters marshal domain events to DTOs to send/receive via ActiveMQ.
- Messaging used for:
   - Notification of lifecycle events (e.g., employee creation)
   - Outbound integrations (payroll, invoicing—see placeholder adapters)
- Idempotent message handling recommended for consumers; see DTO converters.

## Caching Strategy

- Ehcache is used for read-heavy queries (e.g., frequently requested Employee/Project data).
- Caching configured at service layer with time-to-live and max elements (see `ehcache.xml`).
- Invalidation patterns:
   - On write/update/delete to a cached entity, relevant cache region is evicted.
   - Expose endpoints/admin ops to clear all/part of the cache as needed.

## Database Migration with Liquibase

- DB schema changes defined as versioned changelog XMLs (`db/changelog/`).
- Application expects DB is migrated prior to or during startup, using:
  - `mvn liquibase:update` — to apply
  - `liquibase:rollback` — to revert changesets
- Changelog files include schema, seed/data, and application state for baseline.

## Configuration and Environment Management

- All runtime config via `application.properties` and Java system properties.
- Externalized DB, messaging, cache, and port details.
- Spring bean configs in XML under `resources/META-INF/`.

## Build, Deployment, and Runtime

- Package as Uber JAR with dependencies: `java -jar target/Snowman.jar`
- Jetty server listens by default on port 3001 (`server.port` can be overridden).
- No explicit clustering: horizontal scaling by running multiple instances.

## Observability

- Logging via standard Java/Spring logging mechanisms.
- Recommend using SLF4J + Logback for structured logs.
- Provide basic `/healthcheck` and `/status` resources.
- Metrics collection recommended for production (placeholder only).

## Security Considerations

- No live authentication/authorization for demo; placeholders for future role-based access.
- Validate all inputs on API boundaries.
- Avoid SQL injection and common web vulnerabilities via Spring/JPA.

## Scalability and Performance

- Application can scale vertically and horizontally (manual instance management, JVM tuning).
- Caching reduces DB read load under heavy traffic.
- Messaging decouples slow or batch external integrations.

## Testing Strategy

- Unit tests for service, repository, and resource mappers.
- Integration tests for REST endpoints and data flows.
- Recommend mocks for DB/messaging in CI.

## Future Enhancements Roadmap

- Implement real authentication (e.g., Spring Security, OAuth2).
- Add user roles and granular authorization for endpoints.
- Expand REST resources and support batch/bulk operations.
- Add richer metrics and monitoring endpoints.
- Refactor toward deployment as microservices or containerized cloud solution.

---
**For diagrams, refer to:**
- `etc/HexagonalArchitecture.png` (hexagonal pattern overview)
- `etc/SystemComponents.png` (component context)
- `etc/entity-relationship.png` (ER/data model)
- `etc/relation-table-schema.png` (DB schema)

