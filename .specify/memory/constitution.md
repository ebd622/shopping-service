<!--
Sync Impact Report
- Version change: 1.1.0 -> 1.2.0
- Modified principles:
  - I. Platform and Architecture Baseline (framework version pinned)
- Added sections:
  - None
- Removed sections:
  - None
- Follow-up TODOs:
  - TODO(RATIFICATION_DATE): confirm original adoption date
-->

# Shopping Service Constitution

## Core Principles

### I. Platform and Architecture Baseline
All services MUST use Java 21 LTS, Maven, and Spring Boot 4.1.2. Code organization MUST follow
a feature-based package structure,
and DTOs SHOULD be implemented as Java Records when immutability and concise transport models
fit the use case. JSON serialization/deserialization MUST use Jackson 3; Jackson 2 dependencies
MUST NOT be introduced in new or updated code. Architecture and code design MUST follow SOLID and
clean code principles to maximize maintainability.

Rationale: A strict baseline minimizes drift, improves onboarding, and keeps implementation
choices consistent across features and teams.

### II. API Contract and Validation First
HTTP interfaces MUST follow REST best practices, include explicit API versioning, and publish
OpenAPI 3 documentation for all externally consumed endpoints. Request payloads MUST be validated
using Jakarta Validation before business logic execution. Error responses MUST use a standardized
format with stable error codes, traceable metadata, and client-actionable messages.

Rationale: Stable, versioned contracts and predictable validation/error behavior reduce integration
risk and prevent accidental breaking changes.

### III. Secure Configuration and Data Integrity
PostgreSQL is the primary data store and MUST be used for persistent transactional data unless a
formally approved exception exists. Configuration MUST be externalized per environment, and
secrets MUST NOT be hard-coded in source, tests, container assets, or build scripts. Secure coding
practices, including least privilege, input sanitization, and dependency hygiene, are mandatory.

Rationale: Security and data integrity are foundational non-functional requirements for reliable
production systems.

### IV. Testing Rigor and Release Confidence
Unit and component testing MUST use JUnit 5 and Mockito. Integration testing involving external
dependencies MUST use Testcontainers where practical, especially for PostgreSQL-backed flows.
Pull requests MUST include or update tests for behavior changes and MUST pass CI quality gates
before merge.

Rationale: Deterministic, environment-representative tests catch regressions early and increase
deployment confidence.

### V. Observability and Operability by Default
Services MUST expose health and operational telemetry through Spring Boot Actuator and Micrometer.
Metrics, logs, and traces SHOULD be structured to support production diagnostics and SLO tracking.
Every service MUST provide Docker support suitable for local development, CI verification, and
consistent deployment packaging.

Rationale: Built-in observability and reproducible runtime packaging reduce mean time to detect and
resolve incidents.

## Technology Standards and Constraints

- Runtime: Java 21 LTS is mandatory.
- Framework: Spring Boot 4.1.2 is mandatory.
- Build: Maven is the standard and required build tool.
- Serialization: Jackson 3 is required; Jackson 2 is prohibited.
- API documentation: OpenAPI 3 artifacts MUST remain current with implementation.
- Database: PostgreSQL is the default persistence platform.
- Validation: Jakarta Validation annotations and centralized validation handling are required.
- Packaging: Dockerfiles and related container assets MUST be maintained with the service.

## Delivery Workflow and Quality Gates

- Design changes that affect public APIs MUST update versioned API docs and compatibility notes.
- New or changed endpoints MUST include standardized error response examples in OpenAPI.
- Code review MUST verify secure coding practices, absence of hard-coded secrets, and
  maintainability impacts.
- Automated test suites (unit + integration) MUST pass in CI before merge.
- Observability instrumentation and health endpoints MUST be validated as part of release readiness.

## Governance

This constitution is the authoritative engineering policy for this repository. All specifications,
plans, tasks, and implementation decisions MUST comply with it.

Amendment process:
- Any amendment MUST be proposed via pull request with rationale, impact analysis, and migration
  notes when behavior or standards change.
- Amendments MUST be approved by at least two maintainers before merge.
- Constitution semantic versioning policy:
  - MAJOR: Backward-incompatible governance changes or principle removals/redefinitions.
  - MINOR: New principle/section or materially expanded mandatory guidance.
  - PATCH: Clarifications, wording improvements, or non-semantic refinements.
- Approved amendments MUST update the version and `Last Amended` date in this document.

Compliance and review:
- Every pull request MUST include an explicit constitution compliance check.
- Reviewers MUST block merges that violate mandatory provisions until remediated.
- Compliance posture SHOULD be reviewed at least quarterly and after major incidents.

**Version**: 1.2.0 | **Ratified**: TODO(RATIFICATION_DATE): confirm original adoption date | **Last Amended**: 2026-10-28
