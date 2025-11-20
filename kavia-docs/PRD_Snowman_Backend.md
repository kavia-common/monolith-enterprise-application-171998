# Product Requirements Document (PRD): Snowman Backend

## Product Overview

Snowman is an enterprise-scale employee management system (EMS) backend designed to demonstrate principles and features commonly seen in robust enterprise Java applications. It is monolithic, built in Spring, and adopts a hexagonal architecture (Ports & Adapters). Snowman exposes its core management logic via REST APIs, supports asynchronous messaging, implements caching for performance, and uses migration tooling for database consistency.

## Goals

- Provide a modular, maintainable reference for enterprise backend architectures.
- Enable CRUD management for employees, projects, users, and clients.
- Demonstrate integration with major backend systems: relational DB (MySQL), message queue (ActiveMQ), and cache (Ehcache).
- Expose well-documented REST APIs for core resources.
- Ensure high-level standards for reliability, maintainability, and scalability.

## Primary Users

- HR/Admin staff for employee and project management.
- IT staff for user and client management.
- Integrator/Developer roles accessing the backend via APIs or messaging.
- Operations/SRE staff monitoring and maintaining the backend.

## Use Cases

- Onboard or update employee records (CRUD).
- Manage project allocations, lifecycle, and relations to employees.
- Register, update, and deactivate application users.
- Maintain client entities and associated project assignments.
- Integrate with external/internal systems via messaging (ActiveMQ JMS).
- Cache frequently used data for API performance.
- Apply database schema changes safely during upgrades.

## Core Features

- **Employee Management**: Full CRUD for employee records.
- **Project Management**: Full CRUD for projects; manage employees assigned to projects.
- **User Management**: CRUD for system users, user role assignment.
- **Client Management**: CRUD for client entities, client-project relationships.
- **RESTful APIs**: Well-structured, resource-oriented endpoints for all four domains.
- **Integration Points**:
  - **Database**: Persistent storage in MySQL. ORM via Hibernate/JPA.
  - **Messaging**: Interact with JMS/ActiveMQ for asynchronous workflows.
  - **Caching**: Use Ehcache for hot/read-heavy data.
  - **Migrations**: Liquibase for DB schema/version management.

## Functional Requirements

- Each domain resource supports Create, Read (with pagination/sorting), Update, and Delete operations via REST.
- Input payloads are validated; error handling yields structured JSON responses.
- Endpoints should support filtering and sorting where practical.
- System uses pessimistic/optimistic locking as appropriate for update endpoints.
- All changes affecting employee or project data may trigger JMS notifications.
- Caching is applied to queries for frequently accessed, infrequently modified data (configurable per entity).
- Error messages must be meaningful and logged for debugging.
- Application configuration (DB connection, ports, etc) is environment-driven.

## Non-Functional Requirements

- **Performance**: REST endpoints must respond within 500ms under standard load, with cache utilized.
- **Scalability**: Support both vertical scaling (JVM params) and horizontal scaling (multiple app instances via port config).
- **Security**: Placeholder for user auth (future-facing), with input validation and no critical vulnerabilities.
- **Observability**: Application logs requests, errors, and key ops. Expose basic health-check endpoint(s).
- **Reliability**: Fail gracefully; support start/stop of app with minimal manual processes.
- **Maintainability**: Hexagonal architecture to ensure modularity and ease of adapting outer layers.
- **Portability**: Distributed as a self-contained Uber JAR, requires only JVM.

## Integrations

- **MySQL Database**: Main persistence layer.
- **ActiveMQ JMS**: Messaging backbone for decoupled events.
- **Ehcache**: L2 cache for domain models.
- **Liquibase**: Managed, version-controlled DB migrations.

## Out of Scope / Assumptions

- Full UI/front-end not included.
- Security/authentication mechanisms only stubbed (not live).
- Multi-tenancy, clustering, or managed cloud deployment not addressed.
- No built-in disaster recovery—app designed for demo/manual recovery only.
- Third-party system integration via messaging; no direct external system management.

## Risks

- Custom environments or DB versions may require manual adaptation of Liquibase changelogs.
- Deployment assumes operable MySQL, ActiveMQ, and prepopulated configuration.

## Acceptance Criteria

- REST endpoints for all primary resources return correct status and payloads.
- All entities managed via CRUD can be persisted, retrieved, updated, and deleted.
- Messaging is triggered for designated business events to ActiveMQ.
- Caching layer is effective for at least Employee or Project reads.
- Liquibase can apply DB upgrades without destructive operations by default.
- Logs and health checks can be accessed for monitoring and debugging.

## KPIs

- <500ms response time for cached REST GETs (standard dataset).
- 100% test coverage for resource-layer input validation.
- 90%+ reliability in downtime/restart scenarios.
- Successful rolling database and application updates.
