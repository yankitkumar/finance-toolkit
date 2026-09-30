# Finance Toolkit — Spring Boot Microservices

A small, heavily commented **Java 21 / Spring Boot 3** microservices project for
learning two things at once:

1. **Core personal finance:** EMIs, compound interest, SIPs, inflation, CAGR, NPV and IRR.
2. **How microservices fit together:** an API gateway, services calling each other
   over HTTP, a database per service, and what to do when a service is down.

Each formula is written out in the code comments, with the intuition behind it,
and each concept has a test with an answer you can check by hand.

## Architecture

```
                         ┌──────────────────────┐
   curl / browser ──────►│  api-gateway  :8080  │   one front door; routes by URL path
                         └──────────┬───────────┘
            /api/loans/**           │  /api/investments/**        /api/plans/**
        ┌───────────────────────────┼──────────────────────────────┐
        ▼                           ▼                              ▼
┌────────────────┐        ┌────────────────────┐        ┌─────────────────────┐
│ loan-service   │        │ investment-service │        │ planner-service     │
│ :8081          │        │ :8082              │        │ :8083               │
│ EMI, schedule, │        │ lump sum, SIP,     │        │ "Can I afford it?"  │
│ prepayment     │        │ goal, inflation,   │        │                     │
│                │        │ CAGR, NPV/IRR      │        │  ┌───────────────┐  │
│ (stateless)    │        │ (stateless)        │        │  │ H2 database   │  │
└────────────────┘        └────────────────────┘        │  │ (its own)     │  │
        ▲                           ▲                   │  └───────────────┘  │
        │   POST /api/loans/emi     │ POST /api/investments/goal             │
        └───────────────────────────┴───────────────────┴─────────────────────┘
                     planner-service calls the other two over HTTP
```

| Service | Port | What it owns |
|---|---|---|
| `api-gateway` | 8080 | Routing only (Spring Cloud Gateway). Returns a clear 503 when a service is down. |
| `loan-service` | 8081 | Loan math: EMI, amortization schedule, prepayment savings. |
| `investment-service` | 8082 | Growth math: lump sum, SIP, goal planning, real return, CAGR, NPV/IRR. |
| `planner-service` | 8083 | Combines both into an affordability verdict and saves plans in its own H2 database. |

## Run it

You need **Java 21** and **Maven 3.9+**. Docker is optional.

### Option A: Docker Compose (one command after the build)

```bash
mvn package                  # builds the 4 jars and runs the tests
docker compose up --build    # starts the 4 containers
```

### Option B: run each service yourself (4 terminals)

```bash
mvn package
java -jar loan-service/target/loan-service.jar
java -jar investment-service/target/investment-service.jar
java -jar planner-service/target/planner-service.jar
java -jar api-gateway/target/api-gateway.jar
```

Check that everything is up: `curl localhost:8080/actuator/health`

## Try it

Every request goes to the gateway on port **8080**. Rates are in **percent**: `9` means 9%.

### Loans: what does a ₹20 lakh, 9%, 20-year loan really cost?

```bash
curl -s localhost:8080/api/loans/emi -H 'Content-Type: application/json' \
  -d '{"principal": 2000000, "annualRatePercent": 9, "years": 20}'
```
```json
{"monthlyPayment":17994.52,"months":240,"totalPayment":4318684.18,"totalInterest":2318684.18}
```
You pay back more than twice what you borrowed.

`POST /api/loans/schedule` (same body) returns all 240 months. Month 1 is
₹15,000 interest + ₹2,994.52 principal; the last month is ₹133.95 interest +
₹17,859.95 principal.

```bash
curl -s localhost:8080/api/loans/prepayment -H 'Content-Type: application/json' \
  -d '{"loan": {"principal": 2000000, "annualRatePercent": 9, "years": 20}, "extraMonthlyPayment": 5000}'
```
```json
{"originalMonths":240,"newMonths":142,"monthsSaved":98,"originalInterest":2318684.18,"newInterest":1251312.47,"interestSaved":1067371.71}
```
₹5,000 extra a month finishes the loan 8 years 2 months early and saves over ₹10 lakh.

### Investments

| Question | Endpoint | Example body | Answer |
|---|---|---|---|
| What does ₹1 lakh become in 10 years at 8%? | `/api/investments/lump-sum` | `{"amount":100000,"annualRatePercent":8,"years":10,"compoundsPerYear":1}` | `futureValue: 215892.50` |
| ₹10k/month SIP at 12% for 10 years? | `/api/investments/sip` | `{"monthlyInvestment":10000,"annualRatePercent":12,"years":10}` | invested 12,00,000 → `futureValue: 2323390.76` |
| Monthly SIP to reach ₹20 lakh in 10 years? | `/api/investments/goal` | `{"targetAmount":2000000,"annualRatePercent":12,"years":10}` | `monthlyInvestment: 8608.11` |
| 7% FD with 5% inflation? | `/api/investments/real-return` | `{"nominalRatePercent":7,"inflationRatePercent":5}` | `realRatePercent: 1.90` (not 2) |
| ₹1 lakh → ₹2 lakh in 5 years = ? per year | `/api/investments/cagr` | `{"startValue":100000,"endValue":200000,"years":5}` | `cagrPercent: 14.87` |
| Pay 1 lakh now, get 30k/yr for 5 years? | `/api/investments/evaluate-project` | `{"discountRatePercent":10,"cashFlows":[-100000,30000,30000,30000,30000,30000]}` | `npv: 13723.60, irrPercent: 15.24, decision: INVEST` |

### Planner: can I afford a loan *and* my savings goal?

```bash
curl -s localhost:8080/api/plans -H 'Content-Type: application/json' -d '{
  "name": "Home + child education",
  "monthlyIncome": 150000,
  "monthlyExpenses": 60000,
  "loan": {"principal": 3000000, "annualRatePercent": 8.5, "years": 20},
  "goal": {"targetAmount": 2000000, "annualRatePercent": 12, "years": 10}
}'
```
```json
{"id":1,"name":"Home + child education","monthlyIncome":150000,"monthlyExpenses":60000,
 "monthlyEmi":26034.70,"monthlyInvestment":8608.11,"monthlyLeftover":55357.19,
 "emiToIncomePercent":17.36,"verdict":"AFFORDABLE",
 "advice":"Comfortable: 55357.19 (36.90% of income) is left every month after the EMI and your investment.", ...}
```

Behind that one call, planner-service called **loan-service** for the EMI and
**investment-service** for the SIP, applied its budgeting rules, and saved the
result. Read plans back with `GET /api/plans` and `GET /api/plans/1`.

| Verdict | Rule (first match wins) |
|---|---|
| `NOT_AFFORDABLE` | income − expenses − EMI − SIP is below zero |
| `RISKY` | EMI is more than 40% of income (lenders' usual comfort limit) |
| `TIGHT` | less than 10% of income left as a buffer |
| `AFFORDABLE` | everything else |

### See what happens when a service is down

Stop loan-service (Ctrl+C, or `docker compose stop loan-service`) and call it again:

```json
{"title":"Service unavailable","status":503,"detail":"loan-service is not reachable. Is it running?","service":"loan-service"}
```

The investment endpoints keep working. One failing service doesn't take down the others.

### Bad input gets a clear 400

```json
{"title":"Invalid request","status":400,"errors":{"principal":"must be greater than 0","years":"must be greater than or equal to 1"}}
```

## Finance concepts, and where to find them in the code

| Concept | Formula | Code |
|---|---|---|
| **EMI** | `P × i × (1+i)^n / ((1+i)^n − 1)` | `LoanCalculator.monthlyPayment` |
| **Amortization** | each month: interest = balance × i; principal = EMI − interest | `LoanCalculator.simulate` |
| **Compound interest** | `PV × (1 + r/m)^(m×t)` | `InvestmentCalculator.lumpSum` |
| **SIP** (start-of-month) | `P × ((1+i)^n − 1)/i × (1+i)` | `InvestmentCalculator.sip` |
| **Goal planning** | SIP formula solved for P | `InvestmentCalculator.goal` |
| **Real return** | `(1+nominal)/(1+inflation) − 1` | `InvestmentCalculator.realReturn` |
| **CAGR** | `(end/start)^(1/years) − 1` | `InvestmentCalculator.cagr` |
| **NPV** | `Σ CF_t / (1+r)^t` | `InvestmentCalculator.npv` |
| **IRR** | the r where NPV = 0 (found by bisection) | `InvestmentCalculator.irr` |

Money is handled with `BigDecimal`, never `double` (`0.1 + 0.2` isn't exactly
`0.3` in binary floating point). Loan amounts are rounded to the cent every
month, and the last payment clears any leftover cents, just like a bank statement.

## Microservices concepts, and where to find them

| Concept | Where |
|---|---|
| API gateway / single entry point | `api-gateway/src/main/resources/application.yml` |
| Service-to-service calls with `RestClient` | `planner-service/.../client/LoanServiceClient.java` |
| Tolerant readers (`@JsonIgnoreProperties(ignoreUnknown = true)`) | `planner-service/.../client/LoanQuote.java` |
| Database per service (JPA + H2) | `planner-service/.../model/FinancialPlan.java` |
| Config per environment (`${LOAN_SERVICE_URL:http://localhost:8081}`) | `application.yml` files + `docker-compose.yml` |
| Graceful failure: 503 instead of 500 | `ServiceUnavailableHandler` (gateway), `ApiExceptionHandler` (planner) |
| Validation + RFC 7807 problem details | `@Valid` on controllers, `ApiExceptionHandler` in each service |
| Stateless vs stateful services | loan/investment keep nothing; planner stores plans |

Browse the planner's database at http://localhost:8083/h2-console
(JDBC URL `jdbc:h2:mem:planner_db`, user `sa`, empty password). It's in memory,
so it resets on every restart.

## Tests

```bash
mvn test     # 39 tests across the four services
```

The tests are written at a few different levels, as a learning example:

- **Plain unit tests** (`LoanCalculatorTest`, `InvestmentCalculatorTest`, `PlannerServiceTest`):
  no Spring, run in milliseconds.
- **Web layer tests** (`@WebMvcTest`): JSON in/out and validation, without a real server.
- **HTTP client tests** (`@RestClientTest`): a fake loan-service checks the exact request sent.
- **Full service tests** (`@SpringBootTest`): the real planner with H2, other services mocked;
  the real gateway checking the 503 behaviour.

## Project structure

```
finance-toolkit/
├── pom.xml                       parent build: versions for all modules
├── docker-compose.yml
├── api-gateway/                  routes + 503 handling
├── loan-service/
│   └── src/main/java/com/financetoolkit/loan/
│       ├── controller/           HTTP endpoints + error handling
│       ├── dto/                  request/response records
│       └── service/              LoanCalculator (the math)
├── investment-service/           same layout as loan-service
└── planner-service/
    └── src/main/java/com/financetoolkit/planner/
        ├── client/               calls to loan-service and investment-service
        ├── controller/
        ├── dto/
        ├── model/                FinancialPlan entity, PlanVerdict
        ├── repository/           Spring Data JPA
        └── service/              PlannerService (budget rules)
```

## Ideas for next steps

- Service discovery with Eureka instead of fixed URLs
- Circuit breaker and retries with Resilience4j on the planner's clients
- Swap H2 for PostgreSQL (only `application.yml` and one dependency change)
- OpenAPI/Swagger UI with springdoc-openapi

---

*For learning only. This is not financial advice.*
