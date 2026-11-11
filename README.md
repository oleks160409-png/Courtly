# Courtly

Courtly is a Java learning project for booking sports courts. It grew from a console application into a Spring Boot REST API backed by PostgreSQL.

The API supports two seeded courts, bookings on specific dates, half-hour time slots, availability queries and cancellation by booking ID. Booking intervals include the start and exclude the end, so `10:00–10:30` and `10:30–11:00` can coexist.

## Stack

Java 21, Spring Boot 3.5, Maven Wrapper, Spring Data JPA, PostgreSQL 17, Flyway, JUnit 5 and Docker Compose.

## Run with Docker

Install Docker with Compose, then run these commands from the repository root.

```bash
docker compose up --build -d --wait
curl http://localhost:8080/courts
```

The API listens on `localhost:8080`. PostgreSQL is exposed on `localhost:5432`. Use `PORT=8081 DB_PORT=5434 docker compose up --build -d --wait` if those ports are occupied.

```bash
docker compose down
```

The named `courtly-data` volume retains bookings when containers stop. To check persistence, create a booking, restart the application with `docker compose restart app`, wait for it to become healthy and retrieve the booking by ID.

The example database username and password are both `courtly`. These are local development defaults. The API is anonymous and should be run locally for this version. Authentication and booking ownership are planned future work. The repository currently contains no user registration or authorization checks.

## Run with Java

Use JDK 21 or a compatible JDK. Set `JAVA_HOME` to its installation directory and ensure `java -version` shows the intended version.

Start the database from the repository root.

```bash
docker compose up -d --wait postgres
cd Courtly
./mvnw -B -ntp -DskipTests package
java -jar target/courtly-1.0-SNAPSHOT.jar
```

Windows users can use `mvnw.cmd` in place of `./mvnw`. The wrapper downloads its pinned Maven distribution on first use. Flyway creates the schema and two example courts on an empty database. Hibernate validates the schema on startup.

For an existing PostgreSQL installation, create a database and configure these environment variables.

| Variable | Default |
| --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/courtly` |
| `DB_USERNAME` | `courtly` |
| `DB_PASSWORD` | `courtly` |
| `PORT` | `8080` |
| `SERVER_ADDRESS` | `127.0.0.1` |

The database role running migrations needs permission to create the `btree_gist` extension. Containers set `SERVER_ADDRESS=0.0.0.0` internally and expose the host port on loopback.

## Test

Tests use a separate PostgreSQL database and modify its booking records. Always point `TEST_DB_URL` at a disposable test database.

```bash
# Repository root
docker compose --profile test up -d --wait postgres-test
cd Courtly
TEST_DB_URL=jdbc:postgresql://localhost:5433/courtly_test ./mvnw -B -ntp verify
```

The default test credentials are `courtly` / `courtly`, configurable through `TEST_DB_USERNAME` and `TEST_DB_PASSWORD`. Without `TEST_DB_URL`, tests expect `courtly_test` on port 5432.

The suite checks service rules, HTTP responses, validation, cancellation, adjacent intervals, different courts and dates, and concurrent conflicting requests. Database constraints are also tested through a direct SQL insertion that bypasses service validation.

For a running local API, an additional HTTP check is available using Python 3.9 or newer.

```bash
python3 scripts/smoke_api.py
```

GitHub Actions runs the Java tests against PostgreSQL and builds and checks the Docker Compose application on Linux.

## API example

```bash
curl -i -X POST http://localhost:8080/bookings \
  -H 'Content-Type: application/json' \
  -d '{"courtId":1,"date":"2026-12-01","startTime":"10:00","endTime":"11:30"}'
```

Successful requests return `201 Created` and a `Location` header.

```json
{
  "id": 1,
  "court": {"id": 1, "name": "Tennis Court"},
  "date": "2026-12-01",
  "startTime": "10:00:00",
  "endTime": "11:30:00"
}
```

| Method | Path | Purpose |
| --- | --- | --- |
| GET | `/courts` | List courts |
| GET | `/courts/{id}/availability?date=2026-12-01` | List 28 half-hour slots |
| POST | `/bookings` | Create a booking |
| GET | `/bookings` | List bookings ordered by date, start time and ID |
| GET | `/bookings/{id}` | Retrieve one booking |
| DELETE | `/bookings/{id}` | Cancel a booking, returning 204 |

Invalid input returns 400, missing courts or bookings return 404, and overlapping bookings return 409. Error bodies contain a `message` field. See [API examples](docs/API.md) and [architecture decisions](docs/ARCHITECTURE.md).

## Scope and learning

This is an educational backend developed with AI assistance. Its behavior is covered by automated checks, while independent understanding and interview preparation remain ongoing learning work. There are no production scale or performance claims.

Current boundaries include one shared opening schedule (`08:00–22:00`), half-hour precision and dates without timezone conversion. Past dates are accepted. Courts are seeded through a migration. Cancellation deletes a booking. User accounts, authorization, request idempotency and pagination are future work.
