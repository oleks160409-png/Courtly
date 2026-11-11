# Courtly learning roadmap

## Current phase

First backend release, following the reduced internship preparation scope. Core Java, object-oriented refactoring, multiple courts and dates, REST operations and PostgreSQL persistence are implemented. Container and CI verification are the final release checks.

## Completed implementation tasks

- Console menu and input validation.
- Booking creation and cancellation, including interval conflict checks.
- Separate Court, Booking and BookingService classes.
- Multiple courts, LocalDate and LocalTime with half-hour boundaries.
- Maven Wrapper and JUnit tests.
- Spring Boot controllers and JSON request/response handling.
- PostgreSQL repositories, Flyway migration and database constraints.
- Conflict protection for concurrent booking attempts.
- Docker Compose and setup/API/architecture documentation.

## Current assignment

Verify the first release locally and in GitHub Actions, then prepare an accurate CV project description. After that, review the implementation and explain each important class and method independently.

## Concepts encountered

Arrays, loops, conditions, methods, return values, encapsulation, constructors, getters, object references, ArrayList, dates and times, exceptions, JUnit, Maven, dependency injection, HTTP, JSON, JPA, SQL constraints, transactions, containers and CI.

Implementation and passing tests do not establish independent mastery of these concepts.

## Concepts requiring revision

- Explain object creation, private fields, constructors and getters.
- Trace controller to service to repository to database.
- Explain interval overlap and exclusive end boundaries.
- Explain why a query alone cannot prevent a concurrent booking race.
- Read the migration and explain foreign keys, checks and the exclusion constraint.
- Write and explain a test without copying the implementation.
- Practise Java data structures, algorithms and complexity separately.

## Known limitations

- No authentication, authorization or booking ownership.
- No pagination, request idempotency, audit history or court administration API.
- Past dates are accepted, and dates/times have no timezone conversion.
- Shared fixed opening schedule and half-hour boundaries.
- Availability currently queries one slot at a time.

## Next milestone

An independently explainable project, an evidence-based CV description, and parallel preparation for coding assessments and interviews. Security and advanced backend features can be added after that milestone.
