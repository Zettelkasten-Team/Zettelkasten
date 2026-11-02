# Repository Guidelines

## Permanent Repository and Architecture Rules

### Project Structure & Module Organization
- Core Java 25 sources live in `src/main/java`, UI assets and properties in `src/main/resources`, and XML/FXML assets alongside their packages. Tests sit in `src/test/java` with fixtures in `src/test/resources`.
- Build outputs land in `target/` (fat JAR, Windows EXE, macOS bundle when enabled). `local-repository/` hosts vendored artifacts referenced by the Maven profile. Keep assets small; binaries or sample zettels (e.g., `validFile.zkn`) belong in `target/` or Git LFS, not under version control.
- The executable entry point is `de.danielluedecke.zettelkasten.ZettelkastenLauncher`, which checks the runtime before loading `ZettelkastenApp`; keep new packages under `de.danielluedecke.zettelkasten` or `ch.dreyeck.zettelkasten` to stay consistent.

### Build, Test, and Development Commands
- `mvn clean package` — full build; produces the shaded runnable JAR in `target/Zettelkasten.jar` plus platform launchers when configured.
- `mvn test` — run JUnit 4 (Vintage) and TestNG suites.
- `mvn -DskipTests package` — iterate faster when tests are unchanged.
- `mvn -Plocal-repo clean package` — use the bundled `local-repository/` artifacts when external mirrors are unavailable.
- Run the app locally with `java -jar target/Zettelkasten.jar` after packaging.

### Coding Style & Naming Conventions
- Java 25 (the compatibility launcher remains Java 8 bytecode), 4-space indentation, K&R braces, and one public class per file. Keep package and class names lowerCamel/UpperCamel following existing patterns (e.g., `TasksData`, `ListSelectionFixTest`).
- Prefer `final` for fields that should not change, and favor explicit types over raw collections. Keep UI strings and icons in `resources`; avoid hard-coded absolute paths.
- Logging: stay with the existing `java.util.logging` logger wiring via `Constants.zknlogger`; avoid mixing logging frameworks unless coordinated in `pom.xml`.

### Testing Guidelines
- Unit and integration tests belong in `src/test/java` with names ending in `*Test` or `*TestNG`. Co-locate test data in `src/test/resources` and keep them deterministic (no network or personal-file access; writes only in `target/` or framework-managed temporary directories).
- Use JUnit 4/Vintage for legacy tests and Mockito/PowerMock for isolation; TestNG is available for data-driven cases. Verify changed behavior with `mvn test` before opening an implementation PR, unless the agreed task explicitly narrows validation. For documentation-only work, check content, references and `git diff --check`; report that Java tests were not rerun.
- Known JVM font availability warning on some systems; does not affect tests.

### Commit & Pull Request Guidelines
- Follow the existing conventional commits style seen in history (`feat(scope): ...`, `fix(build): ...`, `docs(readme): ...`), using a short imperative subject (<72 chars) and optional body for context.
- PRs should describe the change, risks, and how to verify; link issues, and include screenshots or GIFs for UI-facing work. Report the applicable checks and their results, including tests not rerun, and call out any platform-specific steps (e.g., macOS bundling).

### Architecture and Data Boundaries
- Keep BibTeX an interoperability bridge for external literature management, not a parallel library manager.
- Move data/model dependencies on Swing toward small callbacks or interfaces. Existing coupling is a
  migration constraint, not evidence that complete separation has already been achieved.
- Treat rendering as a projection: do not rewrite stored notes to repair display issues. Changes to
  persistence formats, user data, settings, or UI behavior require an explicitly agreed scope.
- Keep runtime dependencies limited to observable application needs; prefer removing unused libraries.
- Isolate tests from personal settings and data. Use framework-managed temporary fixtures and restore
  modified JVM globals even on failure. Changes to test assertions and to test infrastructure are distinct.

### Baseline and Constrained Markdown
- Baseline Markdown is the regularly supported syntax and HTML projection. It may be corrected or
  extended in its own explicitly agreed scope, with compatibility tests and documented limitations.
- Constrained Markdown denotes a separate, optional mode with an explicitly defined allowed subset.
  The ordinary Markdown on/off setting is not that mode. No separate mode implementation was found
  in the scope review; do not assume one exists.
- AC-02's ban on adding formatting features applies to that restriction-mode task. It is not a blanket
  ban on baseline renderer development. The pipe-table extension in
  `a58f8f71100b5b58567ebece8afefa924cecd260` is baseline functionality, not an AC-02 violation;
  see [table behavior](docs/markdown-pipe-tables.md).
- Every new baseline syntax scope must state its effect on Constrained Markdown. If that optional mode
  is implemented, new syntax must not silently enlarge its subset: specify and test neutral fallback,
  exclusion, or an explicitly agreed subset change. Until then, record that applicability is pending.
- Neither baseline additions nor a restriction mode authorize bulk note rewrites or enabling Markdown
  for users without their instruction.

## Agent Protocol for Changes
- Declare the agreed task scope before implementation, including goal, permitted files/components,
  exclusions and validation. Declare analysis/documentation boundaries as such, without inventing an
  implementation AC assignment.
- A PR implements exactly one scope explicitly agreed in advance. New scopes may be defined when
  needed; do not force them into a historical AC identifier or silently expand an existing scope.
- A prompt is an instruction; a scope is the agreed change boundary; a commit is a local history unit;
  a PR is a review unit. They are not interchangeable. One scope may need several commits, and an
  analysis or documentation prompt does not automatically create an implementation PR.
- Keep changes small, reviewable and independent. Preserve unrelated working-tree changes. If work
  cannot be isolated within the agreed boundary, report the conflict before including it.
- Add or adjust tests when behavior/invariants change, and perform the applicable validation above.
  A historical test source or successful build is not proof that every test is currently discovered.
- Do not commit, push, open a PR, or alter earlier commits unless authorized by the task. Scope
  agreement alone does not authorize those separate actions.

## Scope Management and Lifecycle
- For a new scope, record its goal/intent, allowed and forbidden changes, acceptance criteria and
  validation. Use an unambiguous identifier or descriptive name; historical AC numbers are not required.
- Record evidence and unresolved criteria when assessing status. Use:
  - `active`: explicitly agreed unfinished work that remains assigned/relevant;
  - `completed`: the bounded task's acceptance criteria are supported by implementation and validation;
  - `partially implemented`: concrete implementation exists, but criteria remain unmet or not fully demonstrated;
  - `superseded`: an identified later decision replaces the task; link that decision;
  - `unclear`: available repository evidence cannot establish the task's implementation or disposition.
- Do not infer status from inactivity, a matching commit title, or the absence of recent changes alone.
  A partial/unclear historical scope is not an instruction to resume implementation.
- Retain completed/replaced scopes and their evidence as historical references. Their task-specific
  file limits and prohibitions do not automatically restrict new scopes; permanent project rules do.
- Overlapping components do not merge scopes. For example, AC-01/AC-06 share UI decoupling concerns,
  while AC-07 changes test execution and AC-11 changes fixtures. Agree the actual boundary explicitly.

## Historical AC Scope Catalogue
The [catalogue](docs/agent-scopes.md) preserves the eight existing scope definitions, their intent,
allowed/forbidden changes and acceptance criteria, with status evidence and unresolved questions.
Read the relevant catalogue section when explicitly undertaking that historical scope.

`AC` is a historically used identifier scheme. No binding expansion of the abbreviation was found
in tracked documentation or the available scope history. Do not invent one or reuse an identifier
for a different task. No definitions of AC-03 through AC-05 were found; the catalogue records that gap.
