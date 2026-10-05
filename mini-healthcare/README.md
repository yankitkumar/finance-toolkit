# Mini Healthcare: a tiny Spring Boot microservices project

Two Spring Boot 3 / Java 21 services. One calls the other over HTTP.

```
 client ──► appointment-service :9102 ──HTTP──► patient-service :9101
            POST /appointments                  GET /patients/{id}
```

| Service | Port | Job |
|---|---|---|
| `patient-service` | 9101 | Stores patients in memory (two sample patients at startup). |
| `appointment-service` | 9102 | Books appointments, checking with `patient-service` that the patient exists. |

All data is in memory and resets on restart. The sample patients are made up.

## Run

```bash
mvn package                                                  # build + test both
java -jar patient-service/target/patient-service.jar         # terminal 1
java -jar appointment-service/target/appointment-service.jar # terminal 2
```

Or with Docker: `mvn package && docker compose up --build`.

## Try it

```bash
curl localhost:9101/patients
curl -X POST localhost:9101/patients -H 'Content-Type: application/json' \
     -d '{"name":"Meena Iyer","age":41}'

curl -X POST localhost:9102/appointments -H 'Content-Type: application/json' \
     -d '{"patientId":1,"doctor":"Dr. Sen","time":"2026-11-01T10:30:00"}'
# {"id":1,"patientId":1,"patientName":"Asha Rao","doctor":"Dr. Sen","time":"2026-11-01T10:30:00"}

curl localhost:9102/appointments
```

Booking for a patient that doesn't exist returns **404**. Stop `patient-service`
and try to book again: you get **503** instead of a crash.
