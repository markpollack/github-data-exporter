# GitHub Data Exporter

A Spring Boot 3.4.5 application that uses Spring for GraphQL to download **all Issues and Pull Requests** from a GitHub repository and save them as **pretty-printed JSON** files for offline analytics.

---

## Table of Contents
1. [Key Features](#key-features)  
2. [Technology Stack](#technology-stack)  
3. [Project Structure](#project-structure)  
4. [Getting Started](#getting-started)  
5. [Configuration](#configuration)  
6. [Running the Export](#running-the-export)  
7. [REST API](#rest-api)  
8. [Sample Output](#sample-output)  
9. [Extending / Roadmap](#extending--roadmap)  
10. [Contributing](#contributing)  
11. [License](#license)

---

## Key Features
* **Spring Boot 3.4.5** application – runs anywhere Java 17+ is available  
* **Spring for GraphQL** client – typed, reactive access to the GitHub GraphQL v4 API  
* Handles **pagination, rate limits** and exports up to a configurable maximum number of items  
* Exports **all major fields** for Issues and Pull Requests (authors, comments, labels, timeline, etc.)  
* Saves data as **JSON** (`issues.json`, `pull-requests.json`) into a configurable output directory  
* Can be triggered from the **command line** or via a **REST API**  
* Clear **status tracking** when triggered through REST  
* Configuration via **`application.yml`** _&_ environment variables – no secrets in code  
* Designed for future extensions (scheduled sync, other formats, Spring AI enrichment)

---

## Technology Stack
| Layer            | Technology |
|------------------|------------|
| Language         | Java 17 |
| Framework        | Spring Boot 3.4.5 |
| GraphQL Client   | Spring for GraphQL 1.4.5 (`HttpGraphQlClient`) |
| HTTP             | Spring WebFlux (reactive WebClient) |
| JSON Binding     | Jackson |
| Build Tool       | Maven |
| Logging          | SLF4J + Logback |

---

## Project Structure
```
src
 └── main
     ├── java/com/github/dataexporter
     │    ├── GitHubDataExporterApplication.java    (bootstraps Spring)
     │    ├── runner/ExportRunner.java              (CLI orchestrator)
     │    ├── controller/ExportController.java      (REST endpoints)
     │    ├── service/                              (fetching Issues & PRs)
     │    ├── export/JsonExporter.java              (file writer)
     │    ├── config/                               (client & props configs)
     │    └── model/                                (Issue, PullRequest, …)
     ├── resources
     │    ├── application.yml                       (defaults / placeholders)
     │    └── graphql/                              (GraphQL queries)
     └── test                                       (unit tests)
pom.xml
```

---

## Getting Started

### Prerequisites
* **Java 17** or newer  
* **Maven 3.9** or newer  
* A **GitHub Personal Access Token** with `repo` scope to read the repository

### Clone & Build
```bash
git clone https://github.com/<your-org>/github-data-exporter.git
cd github-data-exporter
mvn clean package
```
The shaded JAR will be located in `target/github-data-exporter-<version>.jar`.

---

## Configuration
All configuration lives in `src/main/resources/application.yml`.  
Override values with environment variables or command-line `--key=value` parameters.

| Property | Description | Default / Env |
|----------|-------------|---------------|
| `github.api.token` | **Required.** GitHub PAT used for GraphQL authentication | `GITHUB_TOKEN` |
| `github.repository.owner` | Repository owner (user/org) | `REPO_OWNER` |
| `github.repository.name`  | Repository name             | `REPO_NAME`  |
| `export.directory`        | Output folder for JSON files | `./exports` or `EXPORT_DIR` |
| `export.batch-size`       | Items per GraphQL request (max 100) | `100` |
| `export.pagination.max-items` | Global cap of items to retrieve | `1000` |

> **Security Tip** – Never commit tokens. Supply them as environment variables:
> ```bash
> export GITHUB_TOKEN=ghp_xxx...
> export REPO_OWNER=markpollack
> export REPO_NAME=github-data-exporter
> ```

---

## Running the Export

### 1. Command-Line Mode (default)
```bash
java -jar target/github-data-exporter-0.0.1-SNAPSHOT.jar \
     --export-and-exit \
     --export.pagination.max-items=5000
```
The process:
1. Fetches *issues* then *pull requests*  
2. Writes `exports/issues.json` and `exports/pull-requests.json`  
3. Exits with code `0` on success

### 2. Spring Boot Service Mode
Simply run the JAR without `--export-and-exit` to keep the web server alive:
```bash
java -jar target/github-data-exporter-0.0.1-SNAPSHOT.jar
```
This exposes REST endpoints (see below) while still executing the export once on startup via `ExportRunner`.

---

## REST API
Base URL: `http://localhost:8080/api/export`

| Method | Path | Purpose |
|--------|------|---------|
| `POST` | `/` | Start **both** issues & PR export |
| `POST` | `/issues` | Export issues only |
| `POST` | `/pull-requests` | Export pull requests only |
| `GET` | `/status/{exportId}` | Status of a single export |
| `GET` | `/status` | List status of all exports |

Example:
```bash
curl -X POST http://localhost:8080/api/export
# → returns JSON with issuesExportId & pullRequestsExportId
curl http://localhost:8080/api/export/status/{exportId}
```

Responses contain `status` (`IN_PROGRESS`, `COMPLETED`, `FAILED`), counts, duration and file paths.

---

## Sample Output
```text
exports/
 ├── issues.json         # pretty printed, full fidelity
 └── pull-requests.json
```
Each file is a JSON array of Issue / PullRequest objects mirroring most GraphQL fields.

---

## Extending / Roadmap
* Schedule periodic exports with Spring Scheduling or Quartz  
* Incremental sync (use `updatedAt` cursors)  
* Additional formats: CSV, Parquet, database sinks  
* **Spring AI** post-processing: summarisation, embeddings, etc.  
* Kubernetes & Docker packaging  
* Metrics & Prometheus integration

---

## Contributing
1. Fork the repo and create your branch (`git checkout -b feature/foo`)  
2. Ensure `mvn test` passes and lint (Spotless/Checkstyle) is clean  
3. Commit your changes (`git commit -am 'Add new feature'`)  
4. Push and open a Pull Request

---

## License
Distributed under the [Apache 2.0 License](LICENSE).  
© 2025 Mark Pollack & contributors
