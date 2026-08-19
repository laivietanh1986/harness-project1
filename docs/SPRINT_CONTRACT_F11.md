# Sprint Contract — F11: Task Filtering & Sorting API

Status: DRAFT — for review before implementation starts. No implementation code
has been written under this contract.

Source of truth for scope: `src/promts.md` + `docs/PRODUCT.md` (uncommitted
section, currently mislabeled "F07" — see §1) + `evaluator-rubric.md`.

## 1. Identity & numbering (flag before coding)

- `feature_list.json` already has **F07 = structured logging** and **F08 =
  business-logic logging**, both `state: done`. The uncommitted addition to
  `docs/PRODUCT.md` titles this feature "F07. Task Filtering & Sorting API",
  which collides with the existing F07 entry.
- `evaluator-rubric.md` and the user instruction both call this feature
  **F11**. This contract treats **F11** as canonical and will add the
  `feature_list.json` entry as F11.
- Exclusion: this contract does not rewrite the PRODUCT.md section header
  from F07 to F11 — flagging the mismatch here is enough; fixing the doc is a
  one-line follow-up, not part of F11 implementation.

## 2. In scope

- `GET /api/tasks` gains optional query params: `category`, `status`,
  `sortBy`, `sortDir`, `page`, `size`.
- `category` / `status` filters combine with AND; both optional.
- `sortBy` ∈ `{title, createdAt, category}`, default `createdAt`.
- `sortDir` ∈ `{asc, desc}`, default `desc`.
- `page` default `0`, `size` default `20`.
- Response body: `{content, totalElements, totalPages, page, size}`.
- Invalid `sortBy` → `400` with a clear message, no crash.
- Filter combination yielding 0 rows → `200` with empty `content`, not an
  error.

## 3. Prerequisite schema change (not spelled out in PRODUCT.md, but required)

`Task` currently has only `id` and `title` — there is no `category`,
`status`, or `createdAt` column. F11 cannot be built without adding them:

- Add `category` (String, nullable), `status` (String, nullable), `createdAt`
  (LocalDateTime, set once at creation, not updatable) to `Task`.
- `ddl-auto=update` against the existing H2 file DB will add these as
  nullable columns on next boot — existing persisted rows (from F01–F08
  testing) get `NULL` in all three. No backfill migration is in scope (see
  §7 Exclusions); sorting/filtering must be null-safe rather than assume
  every row has these values populated.
- `CreateTaskRequest` and `TaskResponse` gain optional `category`/`status`
  fields so a caller can actually set values to filter on. This is a minimal,
  backward-compatible extension of F03/F05's contract (omitting the fields
  keeps working exactly as before) — not a rework of those features.

## 4. Architecture approach

- Controller stays thin: parses/validates query params only, delegates to
  `TaskService`, no query logic — required by `check_architecture.sh` and
  the evaluator rubric's top band ("Service dùng Specification/Query rõ
  ràng, Controller mỏng").
- Filtering/sorting/paging logic lives in `TaskService`, built on Spring Data
  JPA `Specification`/`Pageable` (`JpaSpecificationExecutor`) rather than
  hand-rolled JPQL string concatenation — evaluator rubric penalizes "logic
  filter nằm trong Controller" and rewards clear Specification/Query usage.
- `sortBy`/`sortDir` validation happens in the service (or a small mapper it
  owns), producing the `400` on an unrecognized column — not left to
  Spring's default (which would 500 on an unknown property).

## 5. Logging (per docs/ARCHITECTURE.md#logging)

- New/changed `TaskService` methods each log exactly one line per outcome:
  INFO on success with identifying context (result count, applied filters
  as param names — not values that could be arbitrary user content beyond
  what's already logged elsewhere), WARN on invalid `sortBy`/`sortDir` with
  the offending value and reason.
- No new access-logging work — `RequestLoggingFilter` already covers
  method/path/status/duration for the new query params.

## 6. Verification standards

Maps directly to `evaluator-rubric.md`'s 5 dimensions plus PRODUCT.md's
acceptance criteria:

- **Functional correctness**: every filter alone, every combination of
  `category`+`status`, every `sortBy`×`sortDir` pairing, and pagination
  across multiple pages return correct results.
- **Edge cases**: invalid `sortBy` → `400` + clear message (not a stack
  trace); 0-result combination → `200` + empty `content` array, correct
  `totalElements=0`/`totalPages=0`.
- **Architecture compliance**: `./check_architecture.sh` passes (controller
  doesn't touch repository, doesn't return entities, every service method
  logs, no `System.out`/`printStackTrace`).
- **Test coverage**: no test files exist in the repo yet
  (`spring-boot-starter-test` is already a dependency, unused so far). F11
  is the first feature required to ship with automated tests — at minimum
  `@SpringBootTest`/`MockMvc` or service-level tests covering: happy path
  per filter, combined filters, invalid `sortBy`, invalid `sortDir`,
  0-result filter, pagination metadata correctness, default values when no
  params given.
- **Code quality**: no duplication between filter branches, easy to add a
  new filterable field later (rubric explicitly rewards extensibility).
- Full `./init.sh` must still pass end-to-end (build + health check + F01–F08
  smoke checks unaffected).

## 7. Exclusions (explicitly out of scope for F11)

- No data backfill/migration script for pre-existing rows' `NULL`
  `category`/`status`/`createdAt` — null-safe handling only.
- No enum/fixed vocabulary enforcement on `category` or `status` values —
  PRODUCT.md doesn't define one; both are treated as free-form strings,
  matched by exact value.
- No changes to F01–F06/F08 behavior beyond the additive DTO fields in §3.
- No new endpoints beyond the existing `GET /api/tasks` gaining query
  params (no separate `/api/tasks/search`, no `PATCH` for category/status).
- No auth/authorization, no rate limiting.
- No renumbering of the PRODUCT.md section header (§1).
- No refactor of unrelated existing code ("Không refactor, không tối ưu"
  per PRODUCT.md's project-wide constraint).

## 8. Assumptions requiring confirmation before/while implementing

These fill gaps PRODUCT.md leaves unspecified; flagging instead of
silently deciding in code:

1. Invalid `sortDir` (not `asc`/`desc`) is treated the same as invalid
   `sortBy` → `400` with clear message (PRODUCT.md only states this
   explicitly for `sortBy`).
2. Invalid `page`/`size` (negative, or `size` ≤ 0) → `400` with clear
   message, same principle extended from the `sortBy` acceptance criterion.
3. `category`/`status` are exact-match, case-sensitive string filters (no
   partial match, no case-insensitivity) — simplest reading of "lọc kết hợp
   được (AND)".

## 9. Definition of done

- `feature_list.json` gets a new `F11` entry (behavior/verification/state)
  once implemented and manually verified, per CLAUDE.md's per-feature
  tracking convention.
- `claude-progress.md` updated with what was built and how it was verified.
- `./init.sh` passes.
- `evaluator-rubric.md` scored honestly against the shipped implementation.
