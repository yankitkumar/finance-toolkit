# Mini Finance: a tiny Spring Boot microservices project

Two Spring Boot 3 / Java 21 services. One calls the other over HTTP.

```
 client ──► converter-service :9002 ──HTTP──► rate-service :9001
            GET /convert                      GET /rates/{from}/{to}
```

| Service | Port | Job |
|---|---|---|
| `rate-service` | 9001 | Returns exchange rates (hard-coded, no database). |
| `converter-service` | 9002 | Converts an amount, fetching the rate from `rate-service`. |

## Run

```bash
mvn package                                             # build + test both
java -jar rate-service/target/rate-service.jar          # terminal 1
java -jar converter-service/target/converter-service.jar # terminal 2
```

Or with Docker: `mvn package && docker compose up --build`.

## Try it

```bash
curl localhost:9001/rates/USD/INR
# {"from":"USD","to":"INR","rate":83.50}

curl "localhost:9002/convert?from=USD&to=INR&amount=100"
# {"from":"USD","to":"INR","amount":100,"rate":83.50,"result":8350.00}
```

Stop `rate-service` and call `/convert` again: you get **503** instead of a crash.
An unknown pair such as `JPY/INR` returns **404**.

Supported pairs: USD-INR, EUR-INR, USD-EUR, GBP-INR.
