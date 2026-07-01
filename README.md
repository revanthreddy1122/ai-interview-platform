# AI-Powered Interview Preparation Platform

A full-stack platform for resume analysis, ATS scoring, AI-generated interview
questions, and AI-evaluated mock interviews.

**Backend:** Java 17, Spring Boot 3, Spring Security + JWT, Spring Data JPA, MySQL, PDFBox
**Frontend:** HTML5, CSS3, vanilla JavaScript (ES6), Bootstrap 5
**AI:** Google Gemini API

---

## 1. Prerequisites

- JDK 17 or newer
- Maven 3.8+
- MySQL 8.0+ running locally (or reachable over network)
- A Gemini API key from https://aistudio.google.com/apikey

## 2. Configure the database

Either let Hibernate create the schema automatically (default), or run the
provided schema manually first:

```bash
mysql -u root -p < src/main/resources/schema.sql
```

The application is configured with `spring.jpa.hibernate.ddl-auto=update`,
so tables will also be created/updated automatically on first run if you skip
this step. `spring.sql.init.mode=never` is set so `schema.sql` is **not**
auto-executed by Spring Boot on startup — it's there for manual/reference use.

## 3. Configure application properties

Open `src/main/resources/application.properties` and set:

```properties
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD

jwt.secret=REPLACE_WITH_YOUR_OWN_LONG_RANDOM_SECRET

gemini.api.key=YOUR_GEMINI_API_KEY
```

The JWT secret must be at least 256 bits (32+ characters) for the HS256
algorithm used by `JwtUtil`. Do not reuse the placeholder secret in production.

## 4. Build and run

```bash
mvn clean install
mvn spring-boot:run
```

The backend starts on **http://localhost:8080**.

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI spec: http://localhost:8080/api-docs

## 5. Run the frontend

The frontend is static HTML/CSS/JS with no build step. Serve the `frontend/`
folder with any static file server, for example:

```bash
cd frontend
python3 -m http.server 5500
```

Then open **http://localhost:5500/pages/login.html** (or just
`http://localhost:5500/` to be redirected there automatically).

The frontend expects the API at `http://localhost:8080/api` — this is
configured at the top of `frontend/js/api.js` via `API_BASE_URL`. Change it
there if your backend runs on a different host/port.

> Note: opening the HTML files directly via `file://` will work for most of
> the UI, but browsers may restrict `fetch()` calls from `file://` origins in
> some configurations — serving over `http://` (as above) is recommended.

## 6. First use

1. Register a new account at `/pages/register.html`.
2. Upload a PDF resume — text is extracted with PDFBox and analyzed by Gemini.
3. View your ATS score, strengths, weaknesses, and missing skills.
4. Match your resume against a job description.
5. Generate interview questions by category/difficulty.
6. Practice them in the mock interview chat and get AI-scored feedback.
7. Check your dashboard for aggregated metrics.

## Project structure

```
src/main/java/com/interviewplatform/
  controller/    REST controllers (Auth, Resume, Interview, Dashboard, Profile)
  service/       Service interfaces + impl/ for business logic
  repository/    Spring Data JPA repositories
  entity/        JPA entities (User, Resume, ResumeAnalysis, Question, InterviewHistory, DashboardMetrics)
  dto/           Request/response DTOs
  security/      JWT util, filter, entry point, UserDetailsService
  config/        Security, CORS, WebClient, OpenAPI, Gemini property binding
  exception/     Custom exceptions + GlobalExceptionHandler
  util/          PDF extraction, JSON parsing, security context helper

src/main/resources/
  application.properties
  schema.sql

frontend/
  pages/   9 HTML pages
  css/     5 stylesheets
  js/      7 modules (api.js loads on every page first)
```

## Known limitations / things to verify yourself

This project was generated and manually reviewed for correctness, but could
not be compiled in the generation environment (no Maven Central access
there). Before relying on it:

- Run `mvn clean install` yourself and fix any residual compile errors.
- Verify the Gemini model name in `application.properties`
  (`gemini.api.model=gemini-2.0-flash`) is still valid/available for your API
  key — Google updates model names periodically.
- Review `jwt.secret` and `gemini.api.key` — placeholders must be replaced.
- The `User` entity doubles as the Spring Security principal
  (`implements UserDetails`); if you extend it, keep lazy-loaded
  relationships excluded from `equals`/`hashCode`/`toString` to avoid
  `LazyInitializationException`.
