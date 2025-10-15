# Repository Guidelines

## Project Structure & Modules
- Source: `src/main/java`
    - `no.hasmac.jsonld.api` — high‑level APIs (expand/compact/flatten/frame/toRdf/fromRdf).
    - `no.hasmac.jsonld.{expansion,compaction,flattening,framing}` — core algorithms.
    - `no.hasmac.jsonld.loader` — pluggable document loaders (HTTP, file, scheme router).
    - `no.hasmac.jsonld.http{,.link,.media}` and `json` — HTTP/media types and JSON utilities.
    - `no.hasmac.rdf{,.io,.impl}` — minimal RDF model + N‑Quads I/O.
- Tests: `src/test/java`, fixtures in `src/test/resources`.
- Release utilities: `scripts/` (maintainers).

## Build, Test, and Dev Commands
- Build: `./mvnw -Dmaven.repo.local=.m2_repo clean package` — compile + jar.
- Full test + coverage: `./mvnw -Dmaven.repo.local=.m2_repo verify` — runs JUnit 5 and JaCoCo.
- Skip tests for quick iteration: `./mvnw -Dmaven.repo.local=.m2_repo -DskipTests package`.
- Run class/method: `./mvnw -Dmaven.repo.local=.m2_repo -Dtest=FlatteningApiTest test` or `./mvnw -Dmaven.repo.local=.m2_repo -Dtest=FlatteningApiTest#shouldFlatten test`.
- Release (maintainers): `./mvnw -Dmaven.repo.local=.m2_repo -Pmaven-central -DskipTests deploy` (GPG-signs, publishes).

## Coding Style & Naming
- Java 11; UTF‑8; 4‑space indentation.
- Classes `UpperCamelCase`, methods/fields `lowerCamelCase`, constants `UPPER_SNAKE_CASE`.
- Keep public API under `no.hasmac.jsonld.api.*`; avoid breaking changes without discussion.
- Favor immutability, pure functions in algorithms, and clear separation between JSON‑LD and RDF layers.

## Testing Guidelines
- JUnit Jupiter (JUnit 5); mock remote behavior via WireMock and local resources.
- Name tests `*Test.java`; store fixtures under matching package in `src/test/resources`.
- Aim to cover algorithmic edge cases and regressions; JaCoCo runs on `verify`.

## Commit & PR Guidelines
- Commit messages: short, imperative (e.g., "fix http timeout"). Conventional Commits are welcome but not required.
- PRs: describe the change, link issues, include tests, and call out performance or API impacts.
- Before opening a PR, run `./mvnw verify` and ensure formatting consistent with surrounding code.

## Architecture Notes
- The document loading layer is pluggable (`DocumentLoader`); prefer `FileLoader`/classpath resources in tests and stub HTTP via WireMock.
- RDF model is intentionally minimal; keep JSON‑LD ↔ RDF conversions isolated from HTTP and parsing concerns.

## PIOSEE Decision Model (Adopted)

Use PIOSEE on every task to structure thinking and execution. It complements the routines below and ties directly into the Traceability trio (Description, Evidence, Plan).

- Problem: restate the task in one sentence, note constraints/timebox, and identify likely routine (A/B/C).
- Information: inspect modules and AGENTS.md, gather environment constraints, locate existing tests/reports, and search code to localize the work.
- Options: list 2–3 viable approaches (routine choice, test scope, fix location) and weigh them with the Proportionality Model.
- Select: choose one option and routine; update the Living Plan with exactly one `in_progress` step.
- Execute: follow the Working Loop and house rules; for Routine A add the smallest failing test first; capture an Evidence block after each grouped action.
- Evaluate: check against the Definition of Done; if gaps remain, adjust the plan or change routine; record final Evidence and a brief retrospective.

PIOSEE → Traceability trio mapping
- P/I/O → Description
- S → Plan (one `in_progress`)
- E/E → Evidence and Verification
