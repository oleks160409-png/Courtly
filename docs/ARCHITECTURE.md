# Architecture and decisions

## Request flow

The HTTP controller reads a request and calls `BookingService`. The service validates business rules and uses Spring Data JPA repositories. Hibernate maps the objects to PostgreSQL tables. Controllers serialize the returned data as JSON, and `ApiExceptionHandler` maps expected failures to HTTP responses.

`Court` describes a court. `Booking` contains its ID, court, calendar date, start time and end time. JPA requires a no-argument constructor and non-final persistent fields, so these classes use protected empty constructors and expose getters without setters. PostgreSQL generates booking IDs.

`CreateBookingRequest` is a record containing the four input fields. Bean Validation checks required values before the controller calls the service. The service repeats business validation so other callers, including the console entry point, cannot bypass the rules.

The original console entry point remains in `Main`. It obtains the same database-backed service from a Spring context. The packaged application's default entry point is `CourtlyApplication`, which starts the HTTP server.

## Time and conflicts

A period is represented as `[start, end)`. Two periods overlap when the new start precedes the existing end and the new end follows the existing start. Conflict checks also match court ID and date.

The service's overlap query gives a readable early error. That query alone cannot prevent races because two transactions could both see a free period. Migration `V1__create_courts_and_bookings.sql` therefore defines a PostgreSQL GiST exclusion constraint on court ID and the timestamp range formed from the booking date and times.

The constraint rejects conflicting inserts even across different application instances. PostgreSQL reports exclusion violations with SQLSTATE `23P01`. The service translates that failure into `BookingConflictException`, which the HTTP layer returns as 409. The failed transaction is rolled back.

Database checks also enforce opening hours, positive duration and half-hour boundaries. The foreign key requires an existing court. Flyway versions schema changes, and Hibernate validates mappings rather than modifying the schema automatically.

## Transactions and storage

Service reads use read-only transactions. Creation and cancellation use write transactions. `saveAndFlush` executes the insert before returning so database conflicts can be translated in the service. Booking records survive application restarts.

The list query orders by date, start time and ID. Availability queries currently perform one lookup per slot. This is a simple implementation for two courts and a fixed 28-slot schedule; no throughput or query-efficiency claims are made. A future improvement can load a day's bookings in one query.

## Test boundaries

Tests use PostgreSQL, including Flyway migrations and the actual exclusion constraint. Service tests run in rollback transactions. HTTP and concurrency tests commit operations to a disposable test database and clear booking data between scenarios. Test databases must be separate from application data.

The concurrency test releases two threads together and verifies exactly one successful booking. A separate SQL test bypasses the service and verifies that the database itself rejects overlap. Docker and CI checks exercise the packaged application through HTTP.

## Current limits

Authentication and user ownership are outside this first version. Any caller with access to the local API can view or cancel bookings. The application binds to loopback by default. Docker Compose publishes its ports only on loopback.

Dates have no timezone, and past dates are permitted. Cancellation physically deletes a row. IDs are unique but not necessarily consecutive. The API does not currently provide pagination, idempotency keys, auditing or court administration endpoints.
