# ATLAS — Adaptive Tagging, Loadouts & Asset Services

**AI Disclosure**: This project specification was developed with assistance from generative AI; all software architecture decisions, implementation, testing, and technical problem-solving are independently undertaken by the project author.

**Project Requirements & Engineering Design Brief**  
**Version:** 1.0 | **Date:** October 13, 2026 | **Status:** Proposed / Not Yet Implemented  
**Project type:** Independent portfolio project | **Primary language:** Java  
**Suggested stack:** Spring Boot, REST/JSON, JUnit 5, AWS SDK for Java 2.x, Amazon DynamoDB

> **Project statement:** Design and implement a game-agnostic backend service that manages a catalog of cosmetic items, player-owned entitlements, saved cosmetic loadouts, and searchable item metadata. Prioritize correctness, maintainability, secure data access, and persistence over graphical presentation.

> **Portfolio disclaimer:** ATLAS is an independent educational project, not affiliated with or endorsed by Epic Games or Fortnite. All sample content should be original or appropriately licensed. This document specifies intended functionality; it does not claim features have been implemented.

## 1. Purpose and success criteria

ATLAS models the backend of a customizable game cosmetic locker. A client should be able to discover available items, determine what a player owns, assemble a valid loadout, save it, and retrieve the same configuration later. The service must enforce domain rules on the server rather than trusting a client.

**Minimum successful demonstration:** An evaluator can start the service locally, load a sample catalog, create a player, grant cosmetics, save a loadout, restart the service, and retrieve the unchanged loadout. Invalid ownership, category, and capacity operations return consistent errors. Automated tests verify these behaviors. A later deployment demonstrates AWS persistence.

### Design principles

1. **Domain first:** Implement explicit business rules independently of controllers and persistence.
2. **Client agnostic:** Any HTTP client can use the API; a game or GUI is optional.
3. **Open for extension:** Adding a cosmetic type or new metadata tags should require minimal changes.
4. **No implicit trust:** Validate ownership, inputs, authorization, and state transitions on the server.
5. **Observable and testable:** Provide reproducible tests and actionable, non-sensitive logs.
6. **Honest scope:** A polished, working vertical slice is better than a large unfinished system.

## 2. Scope

### MVP (required)

- Read-only catalog of cosmetics loaded from JSON seed files.
- Tag-based filtering and item lookup.
- Player profiles identified by stable unique IDs.
- Player entitlements (ownership of cosmetics) maintained separately from the catalog.
- Create, retrieve, update, list, and delete saved loadouts.
- Server-side loadout validation (ownership, slot type, duplicate rules, capacity).
- Persistent storage behind a repository abstraction, initially local and then AWS-backed.
- Versioned REST API with predictable status codes and error payloads.
- Automated domain, service, and API tests.
- Developer setup instructions, architecture notes, and sample requests.

### Stretch goals (optional; not application blockers)

- Optimistic concurrency control and explicit conflict handling.
- Authenticated player access with per-player authorization.
- Cloud deployment, monitoring, and health checks.
- Pagination and more advanced search.
- Loadout history, rollback, import/export, or a small client.
- Load testing and a documented scalability review.

### Out of scope for MVP

- Purchases, payments, real money, and virtual currency.
- Matchmaking, live multiplayer synchronization, or real game account integration.
- Reproducing proprietary game APIs or scraping production player accounts.
- Artwork rendering, complex UI, and asset delivery/CDN implementation.
- Real-world account login until authentication is explicitly implemented.

## 3. Domain model and invariants

The following are conceptual entities; the developer chooses exact class names, relationships, DTOs, and storage layout.

| Entity | Responsibility | Minimum conceptual fields |
|---|---|---|
| `CosmeticDefinition` | Canonical catalog item | `id`, `type`, `displayName`, `tags`, optional `metadata` |
| `Player` | Owner of a locker | `playerId`, optional `displayName` |
| `Entitlement` | Player owns an item | `playerId`, `cosmeticId`, `grantedAt` |
| `Loadout` | Named saved configuration | `loadoutId`, `playerId`, `name`, `slots`, `version`, timestamps |
| `SlotDefinition` | Rules for what may be equipped | `slotKey`, accepted cosmetic types, `capacity`, ordering rule |

### Initial loadout schema

| Slot | Accepted cosmetic type | Capacity |
|---|---|---:|
| Skin | `SKIN` | 1 |
| Back bling | `BACK_BLING` | 1 |
| Pickaxe | `PICKAXE` | 1 |
| Glider | `GLIDER` | 1 |
| Emotes | `EMOTE` | 8 ordered positions |
| Wraps | `WRAP` | 7 ordered positions |

**Requirements:**

- All references must resolve to catalog items.
- An equipped item must belong to the requesting player.
- A slot may contain only compatible cosmetic types.
- Capacity must never be exceeded; ordered slots retain their order after save/reload.
- Empty slots are permitted; the representation is an implementation decision documented in the API contract.
- Within a single emote or wrap slot group, the same item may not occupy multiple positions for the MVP. Whether duplicate items across different groups are valid is governed by slot compatibility.
- Deleting a loadout must not delete the player, catalog items, or entitlements.
- Updating a loadout must not mutate its original catalog definitions.
- Stable IDs, not display names, are authoritative references.

**Extensibility challenge:** Choose whether slot rules belong in enums, configuration, polymorphic types, or a rule registry. Document how a new cosmetic category could be added. The initial six types are requirements; the internal design is intentionally unspecified.

## 4. JSON catalog and tagging

Use JSON as the human-editable interchange and seed format for cosmetic definitions. It is **not** necessary to use JSON files as the production database. Catalog data, player entitlements, and loadouts have different lifecycles and should not be conflated.

Illustrative, non-binding seed record:

```json
{
  "id": "skin_aurora_01",
  "type": "SKIN",
  "displayName": "Aurora Scout",
  "tags": ["original", "winter", "blue", "rare"],
  "metadata": {"set": "Polar Expedition", "introducedIn": "demo-1"}
}
```

**FR-CAT-01** The service shall load catalog records from one or more JSON files at startup or through an explicitly documented import action.

**FR-CAT-02** The importer shall reject or report malformed JSON, duplicate IDs, unknown mandatory types, and missing required fields. Define whether imports are atomic or partial; tests must reflect the choice.

**FR-CAT-03** Tags shall be normalized consistently (for example, case and whitespace) and searchable by one or more tags. Document whether multi-tag filtering uses AND or OR semantics.

**FR-CAT-04** Unknown optional metadata shall be preserved or rejected according to a documented schema policy, rather than silently corrupting data.

**FR-CAT-05** Catalog reads shall not leak player-specific ownership information.

**Design decision:** Should the catalog remain seed-file-based while player state moves to DynamoDB, or should the catalog also be stored in DynamoDB? Either is acceptable if justified.

## 5. Functional requirements

### Player and entitlement management

- **FR-PLY-01:** Create and retrieve a player by unique ID.
- **FR-PLY-02:** Retrieve the set of cosmetic IDs owned by a player.
- **FR-PLY-03:** Grant a valid cosmetic to a player through an administrative/test-only operation; granting the same item twice must be idempotent or produce a documented conflict.
- **FR-PLY-04:** Reject grants for nonexistent catalog items or players.
- **FR-PLY-05:** Never expose administrative entitlement mutation publicly without appropriate protection. For the local MVP, keep it restricted to development/test mode or a seed fixture.

### Loadout lifecycle

- **FR-LOD-01:** Create a named loadout for an existing player.
- **FR-LOD-02:** Retrieve one loadout and list a player's saved loadouts.
- **FR-LOD-03:** Replace or modify a loadout's equipped items, preserving ordering for emotes and wraps.
- **FR-LOD-04:** Rename and delete a loadout.
- **FR-LOD-05:** Validate all slots and entitlements before committing any update; rejected requests must leave the previous loadout intact.
- **FR-LOD-06:** A player may modify only their own loadouts once authorization is enabled.
- **FR-LOD-07:** Persist loadout state across service restarts.
- **FR-LOD-08 (stretch):** Reject stale writes using a version or conditional update to prevent lost updates.

### Reliability and diagnostics

- **FR-SYS-01:** Return structured errors with stable machine-readable codes and human-readable descriptions.
- **FR-SYS-02:** Log request failures without exposing secrets or private player data.
- **FR-SYS-03:** Provide a health/readiness endpoint or documented equivalent.
- **FR-SYS-04:** Invalid client input must not crash the service or partially apply changes.

## 6. REST API contract (proposed)

Prefix all endpoints with `/api/v1`. These paths are a recommended contract, not mandatory method implementations.

| Method | Endpoint | Purpose | Typical success |
|---|---|---|---|
| GET | `/cosmetics` | List/filter catalog (`type`, `tag`, pagination optional) | 200 |
| GET | `/cosmetics/{cosmeticId}` | Get catalog definition | 200 |
| POST | `/players` | Create player | 201 |
| GET | `/players/{playerId}` | Get player | 200 |
| GET | `/players/{playerId}/entitlements` | List owned cosmetics | 200 |
| POST | `/dev/players/{playerId}/entitlements` | Development-only grant | 200/201 |
| GET | `/players/{playerId}/loadouts` | List loadouts | 200 |
| POST | `/players/{playerId}/loadouts` | Create loadout | 201 |
| GET | `/players/{playerId}/loadouts/{loadoutId}` | Read loadout | 200 |
| PUT | `/players/{playerId}/loadouts/{loadoutId}` | Replace loadout | 200 |
| PATCH | `/players/{playerId}/loadouts/{loadoutId}` | Partial edit (optional) | 200 |
| DELETE | `/players/{playerId}/loadouts/{loadoutId}` | Delete loadout | 204 |

Suggested error semantics: `400` malformed input, `401` unauthenticated when auth is enabled, `403` unauthorized, `404` unknown resource, `409` stale version/duplicate conflict, `422` valid JSON that violates a loadout business rule, `500` unexpected server failure, `503` unavailable dependency. Consistency is more important than any single mapping.

Sample error shape:

```json
{
  "code": "COSMETIC_NOT_OWNED",
  "message": "The selected cosmetic is not owned by this player.",
  "details": {"cosmeticId": "skin_aurora_01", "slot": "skin"}
}
```

**Security note:** A path parameter containing `playerId` is not proof of identity. Until authentication is implemented, the API is a local demo, not a secure multi-user production service. Document this limitation prominently.

## 7. System flow

### A. Catalog ingestion

1. Start the application.
2. Parse and validate JSON catalog records.
3. Normalize identifiers and tags.
4. Build or update the catalog repository according to the chosen import policy.
5. Expose read-only catalog search endpoints.
6. Report import errors clearly and deterministically.

### B. Create and save a loadout

1. Client sends player ID and proposed loadout payload.
2. Controller validates JSON structure and required fields.
3. Service confirms the player exists and the loadout belongs to them.
4. Service resolves each cosmetic ID from the catalog.
5. Service verifies entitlement, slot compatibility, capacities, and duplicate rules.
6. Repository persists the **entire valid configuration**; no partial save on validation failure.
7. API returns saved ID, representation, and version if implemented.
8. Client can retrieve the same configuration in a later request or after restart.

### C. Concurrent update (stretch)

1. Two clients read the same loadout version.
2. Client A saves a valid update and increments the version.
3. Client B submits an update based on the stale version.
4. Persistence rejects the stale write and returns a conflict.
5. Client B refreshes the latest state and chooses whether to retry.

### D. Failure handling

1. A request reaches the service while the storage dependency is unavailable.
2. The service returns a controlled dependency error, without claiming success.
3. Logs capture a correlation identifier and non-sensitive diagnostic context.
4. After recovery, previously committed loadouts remain readable.

## 8. Architecture boundaries

A suggested layered structure is:

```text
HTTP Client
    |
REST Controllers + DTO Validation
    |
Application Services / Use Cases
    |
Domain Models + Business Rules
    |
Repository Interfaces
    |--------------------------|
Local Persistence Adapter   DynamoDB Adapter
```

Controllers should not contain core ownership or slot rules. Domain logic should not require an AWS account to run tests. Repositories should hide storage-specific concerns from services. Choose packages and patterns independently; explain tradeoffs in an architecture decision record (ADR).

### Persistence phases

- **Phase 1 — Local:** Use an in-memory repository for fast tests, plus a durable local implementation (JSON files or an embedded database) to satisfy restart persistence.
- **Phase 2 — AWS:** Implement the same repository contracts using DynamoDB and the AWS SDK for Java 2.x Enhanced Client, where appropriate.
- **Phase 3 — Deployment (stretch):** Deploy the API to a managed AWS runtime, configure runtime identity with least privilege, and collect logs/metrics.

Document the DynamoDB partition/sort key strategy, expected access patterns, ownership isolation, and conditional writes. Avoid storing an entire rapidly growing player catalog as one oversized record. Avoid scans for routine player-owned loadout lookups.

## 9. Security, cloud, and operational requirements

- Use AWS IAM roles/credential provider chains; never commit keys, tokens, `.env` secrets, or credentials.
- Apply least-privilege access to DynamoDB resources.
- Configure AWS billing alerts/budgets before provisioning resources and tear down unused deployments.
- Separate development/test data from any production-like environment.
- Use HTTPS for public endpoints, if deployed.
- Apply reasonable request-size limits and input validation.
- Record the selected AWS region and environment configuration without hard-coding credentials.
- Document known security limitations if authentication, authorization, or rate limiting are not yet implemented.
- Never label a deployment 'production secure' solely because it runs on AWS.

## 10. Testing and acceptance criteria

Each required scenario should have at least one automated test where practical. Use unit tests for domain rules, integration tests for repositories, and API tests for HTTP behavior.

| ID | Acceptance scenario | Expected result |
|---|---|---|
| AC-01 | Import a valid catalog | All valid definitions become queryable |
| AC-02 | Import duplicate cosmetic IDs | Deterministic rejection/report; no silent overwrite |
| AC-03 | Filter cosmetics by type and tag | Only matching items returned |
| AC-04 | Grant an existing cosmetic to a player | Entitlement recorded |
| AC-05 | Equip an owned skin in skin slot | Loadout saves successfully |
| AC-06 | Equip unowned cosmetic | Request rejected; state unchanged |
| AC-07 | Equip a glider in pickaxe slot | Request rejected; state unchanged |
| AC-08 | Provide nine emotes | Request rejected; capacity enforced |
| AC-09 | Provide eight wraps | Request rejected; capacity enforced |
| AC-10 | Save exactly eight emotes and seven wraps | Request succeeds; order preserved |
| AC-11 | Equip duplicate emote in two positions | Request rejected under MVP duplicate policy |
| AC-12 | Update a loadout with one invalid item | Entire update rejected atomically |
| AC-13 | Restart application after save | Persisted loadout still retrievable |
| AC-14 | Delete a loadout | Loadout absent; catalog and entitlements intact |
| AC-15 | Request missing player or loadout | Consistent 404 response |
| AC-16 | Malformed JSON or missing required field | Controlled 400 response |
| AC-17 | Simulated repository failure | Controlled error; no false success |
| AC-18 (stretch) | Two writers update stale version | Stale update rejected with 409 |
| AC-19 (stretch) | Access another player's loadout with auth enabled | Request denied |

**Definition of done:** MVP requirements pass automated tests, service starts from a clean clone with documented commands, API examples work, data survives restart, repository contains no secrets, and README clearly distinguishes implemented features from planned work.

## 11. Milestones and delivery plan

| Milestone | Deliverable | Exit condition |
|---|---|---|
| M1 — Foundation | Spring Boot project, JSON models, documented architecture | App starts and catalog can be queried |
| M2 — Domain | Player entitlements and loadout rules | AC-01 through AC-12 pass where applicable |
| M3 — Durable MVP | CRUD endpoints and persistent local storage | AC-13 through AC-17 pass |
| M4 — Cloud integration | DynamoDB repository adapter, cloud configuration | End-to-end CRUD demonstrated with DynamoDB |
| M5 — Portfolio polish | README, diagrams, tests, CI, demo script | Fresh clone is reproducible and reviewable |
| M6 — Advanced engineering (optional) | Versioned writes, auth, deployment, monitoring | Stretch acceptance tests pass |

**Schedule guidance:** Target a working local vertical slice first; do not make AWS deployment a prerequisite for publishing the repository or applying to internships.

## 12. Engineering decision log — questions for the implementer

Document decisions and tradeoffs rather than treating this specification as a coding tutorial.

1. How are cosmetic types and slot definitions extended without breaking existing saves?
2. Should catalog seed files be immutable, reloadable, or administratively editable?
3. How are tags normalized and indexed? What happens when a tag is renamed?
4. What representation best supports eight ordered emote slots and seven ordered wrap slots, including gaps?
5. How do you keep entitlement checks consistent with loadout writes?
6. Which DynamoDB access patterns determine partition and sort keys?
7. What are the risks of JSON-file persistence under concurrent requests?
8. How do you prevent lost updates and ensure atomic behavior?
9. What is the smallest meaningful authentication/authorization model?
10. What is the migration strategy if the loadout schema changes?
11. What should the service do if a previously equipped cosmetic is removed from the catalog or revoked?
12. How will you measure API latency, test coverage, and operational failures?

## 13. Suggested repository layout

```text
atlas/
  README.md
  docs/
    PROJECT_SPECIFICATION.md
    ARCHITECTURE.md
    API.md
    decisions/
  sample-data/
    cosmetics.json
  src/
    main/
    test/
  .gitignore
  LICENSE
```

The implementation layout may differ. Keep this specification under `docs/PROJECT_SPECIFICATION.md` and link it from the main README. Add badges or claims about passing tests, cloud deployment, and coverage **only after verifying them**.

## 14. Demonstration script for reviewers

1. Start the API using the documented local profile.
2. Query the catalog and filter by `tag=winter`.
3. Create a sample player and grant several test cosmetics through a protected dev-only mechanism.
4. Save a loadout containing a skin, back bling, pickaxe, glider, eight emotes, and seven wraps.
5. Retrieve the loadout and show that ordered slots were preserved.
6. Attempt to equip an unowned item; show a predictable error and unchanged saved data.
7. Restart the API; retrieve the previously saved loadout.
8. Optionally switch to the AWS profile and demonstrate the same contract against DynamoDB.
9. Show tests and explain one architecture decision and one failure scenario.

## 15. References and learning resources

- Epic Games, Backend Services Programmer Intern (R27435): https://www.epicgames.com/site/careers/jobs/6183293004
- Spring, Building a RESTful Web Service: https://spring.io/guides/gs/rest-service
- Spring, Testing the Web Layer: https://spring.io/guides/gs/testing-web
- AWS, DynamoDB Enhanced Client for Java 2.x: https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/dynamodb-enhanced-client.html
- AWS, DynamoDB with the Java SDK 2.x: https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/examples-dynamodb.html

---

**Implementation ownership:** Architecture choices, Java implementation, tests, cloud configuration, and project claims belong to the developer. This brief intentionally defines behavior and quality targets without providing an implementation solution.
