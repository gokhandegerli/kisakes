# Kisakes: A Modern URL Shortener - A System Design Playground

![Java](https://img.shields.io/badge/Java-21-blue.svg?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3-green.svg?style=for-the-badge&logo=spring)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg?style=for-the-badge&logo=postgresql)
![Redis](https://img.shields.io/badge/Redis-7-red.svg?style=for-the-badge&logo=redis)
![Docker](https://img.shields.io/badge/Docker-Compose-blue.svg?style=for-the-badge&logo=docker)
![Liquibase](https://img.shields.io/badge/Liquibase-4.2-orange.svg?style=for-the-badge&logo=liquibase)
![Grafana](https://img.shields.io/badge/Grafana_Loki-Logging-orange.svg?style=for-the-badge&logo=grafana)

Kisakes (Turkish for "Shorten It") is a robust, scalable, and observable URL shortening service. But more importantly, it serves as a practical, hands-on laboratory for implementing and understanding advanced backend engineering and system design concepts.

## The "Why": A System Design Playground

The primary goal of this project is not just to build another URL shortener. It is to create a tangible learning environment to explore the challenges of building distributed, high-performance systems. Each feature and architectural decision is a deliberate step towards applying and mastering key system design principles.

This repository documents the journey of evolving a simple application into a scalable and resilient service, tackling problems like database scaling, caching strategies, and system observability one concept at a time.

## Implemented Concepts & Core Features

This project currently implements the following concepts:

#### 1. Scalability & Architecture
*   **Stateless Architecture:** The `kisakes-app` is designed to be stateless, allowing for seamless horizontal scaling without requiring session affinity or complex state management.
*   **Horizontal Scaling:** The `docker-compose.yml` is configured to run two instances of the `kisakes-app` to simulate a horizontally scaled environment, ready to be placed behind a load balancer.
*   **Containerization (Docker):** The entire application and its dependencies (PostgreSQL, Redis, Grafana, etc.) are containerized, ensuring consistency across development and production environments.
*   **Multi-Stage Dockerfile:** Employs a multi-stage build to create a lean, production-ready final image, separating the build environment from the runtime environment.

#### 2. Database & Performance
*   **PostgreSQL:** The primary relational database for storing URL mappings.
*   **Connection Pooling (HikariCP):** Leverages the high-performance HikariCP connection pool, configured automatically by Spring Boot, to efficiently manage database connections under load.
*   **Time-Based Partitioning:** The `urls` table is partitioned by `created_at` to improve query performance on large datasets and simplify data management (e.g., archiving old data).
*   **Indexing Strategies:** Utilizes B-tree indexes on `short_code` and `created_at` to ensure fast lookups and efficient partitioning.
*   **Lookup Table Pattern:** A dedicated, non-partitioned `short_code_lookup` table is used to enforce global uniqueness for `short_code`, overcoming a limitation of unique constraints on partitioned tables in PostgreSQL.
*   **Database Schema Management (Liquibase):** Manages all database schema evolutions in a version-controlled, environment-agnostic way, ensuring reliability and repeatability.

#### 3. Caching
*   **Redis:** Used as a distributed cache to reduce database load and decrease latency for frequently accessed URLs.
*   **Cache-Aside Pattern:** The application logic first checks Redis for a given short code. If it's a cache miss, it fetches the data from PostgreSQL and populates the cache for subsequent requests.
*   **Spring Cache Abstraction (`@Cacheable`):** Refactored the manual cache-aside logic to use Spring's declarative caching, resulting in cleaner, more maintainable code.

#### 4. Observability & Monitoring (The "LPG" Stack)
*   **Centralized & Distributed Logging:** The entire system is configured for centralized logging, allowing logs from multiple application instances to be aggregated and searched in one place.
*   **Grafana Loki:** The core log aggregation system, chosen for its efficiency and deep integration with Grafana.
*   **Promtail:** The agent responsible for discovering containers, collecting their logs, adding metadata labels (like `app_name`, `container`), and shipping them to Loki.
*   **Structured JSON Logging:** The Spring Boot application is configured (via `logstash-logback-encoder`) to output logs in a structured JSON format, enabling powerful filtering and analysis.
*   **Distributed Tracing Foundation:** Using `Micrometer Tracing`, every incoming request is automatically assigned a `Trace-ID`, which is included in all logs. This allows for filtering all logs related to a single request across all services.

#### 5. System Design Patterns & API
*   **Unique ID Generation:** Employs a random, collision-resistant Base62-like string generation for short codes, validated against the lookup table for global uniqueness.
*   **Graceful Error Handling:** Implements custom exceptions (e.g., `UrlNotFoundException`) and leverages Spring's `@ControllerAdvice` for clean and consistent API error responses.

## Tech Stack & Tools

*   **Language/Framework:** Java 21, Spring Boot 3.3
*   **Database:** PostgreSQL 16
*   **Caching:** Redis 7
*   **DB Migration:** Liquibase
*   **Containerization:** Docker, Docker Compose
*   **Observability:** Grafana, Loki, Promtail, Micrometer
*   **Build Tool:** Maven

## Getting Started

### Prerequisites
*   Git
*   Docker & Docker Compose
*   Java 21 (for local development outside Docker)
*   Maven (for local development outside Docker)

### Running the System
1.  Clone the repository:
    ```bash
    git clone <your-repo-url>
    cd kisakes
    ```
2.  Build and run the entire stack using Docker Compose:
    ```bash
    docker compose up --build
    ```
    This will build the Spring Boot application, create Docker images, and start all services.

### Accessing Services
*   **Application Instance 1:** `http://localhost:8081`
*   **Application Instance 2:** `http://localhost:8082`
*   **Grafana (for Logs):** `http://localhost:3000` (user: `admin`, pass: `admin`)
*   **PostgreSQL:** `localhost:5432`
*   **Redis:** `localhost:6379`

## Project Roadmap (TODO)

The following concepts are planned for future implementation:

-   [ ] **Load Balancing:**
    -   [ ] Implement a basic, custom Round-Robin Load Balancer in Java/Spring Boot to understand the core principles of reverse proxies and service registries.
    -   [ ] Integrate a production-grade load balancer like Nginx.
-   [ ] **Scalability:**
    -   [ ] **Sharding (Hash-Based):** Design and implement a sharding strategy for the `urls` table to distribute data across multiple database instances.
-   [ ] **Performance:**
    -   [ ] **Asynchronous Operations:** Introduce asynchronous event processing (e.g., with `@Async` or a message queue like RabbitMQ/Kafka) for tasks like analytics tracking.
    -   [ ] **Batch Processing:** Implement batch inserts for high-throughput scenarios.
-   [ ] **Reliability:**
    -   [ ] **Rate Limiting:** Configure and apply fine-grained rate limiting using Resilience4j.
    -   [ ] **Circuit Breaker Pattern:** Implement circuit breakers to prevent cascading failures when communicating with external or internal services.
    -   [ ] **Graceful Degradation:** Implement strategies for non-critical features to fail gracefully without impacting core functionality.
-   [ ] **Caching:**
    -   [ ] Fine-tune caching with explicit **Time-To-Live (TTL)** configurations.
    -   [ ] Implement a **cache invalidation** strategy for scenarios where URLs are updated or deleted.
-   [ ] **API & Security:**
    -   [ ] Introduce API versioning.
    -   [ ] Add user authentication (e.g., with JWT) to manage user-specific URLs.