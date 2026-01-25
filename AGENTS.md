# Repository Guidelines

## Project Structure & Module Organization
- Core Java 8 sources live in `src/main/java`, UI assets and properties in `src/main/resources`, and XML/FXML assets alongside their packages. Tests sit in `src/test/java` with fixtures in `src/test/resources`.
- Build outputs land in `target/` (fat JAR, Windows EXE, macOS bundle when enabled). `local-repository/` hosts vendored artifacts referenced by the Maven profile. Keep assets small; binaries or sample zettels (e.g., `validFile.zkn`) belong in `target/` or Git LFS, not under version control.
- The main entry point is `de.danielluedecke.zettelkasten.ZettelkastenApp`; keep new packages under `de.danielluedecke.zettelkasten` or `ch.dreyeck.zettelkasten` to stay consistent.

## Build, Test, and Development Commands
- `mvn clean package` — full build; produces the shaded runnable JAR in `target/Zettelkasten.jar` plus platform launchers when configured.
- `mvn test` — run JUnit 4 (Vintage) and TestNG suites.
- `mvn -DskipTests package` — iterate faster when tests are unchanged.
- `mvn -Plocal-repo clean package` — use the bundled `local-repository/` artifacts when external mirrors are unavailable.
- Run the app locally with `java -jar target/Zettelkasten.jar` after packaging.

## Coding Style & Naming Conventions
- Java 8, 4-space indentation, K&R braces, and one public class per file. Keep package and class names lowerCamel/UpperCamel following existing patterns (e.g., `TasksData`, `ListSelectionFixTest`).
- Prefer `final` for fields that should not change, and favor explicit types over raw collections. Keep UI strings and icons in `resources`; avoid hard-coded absolute paths.
- Logging: stay with the existing `java.util.logging` logger wiring via `Constants.zknlogger`; avoid mixing logging frameworks unless coordinated in `pom.xml`.

## Testing Guidelines
- Unit and integration tests belong in `src/test/java` with names ending in `*Test` or `*TestNG`. Co-locate test data in `src/test/resources` and keep them deterministic (no network or filesystem writes outside `target/`).
- Use JUnit 4/Vintage for legacy tests and Mockito/PowerMock for isolation; TestNG is available for data-driven cases. Verify new behavior with `mvn test` before opening a PR.
- Known JVM font availability warning on some systems; does not affect tests.

## Commit & Pull Request Guidelines
- Follow the existing conventional commits style seen in history (`feat(scope): ...`, `fix(build): ...`, `docs(readme): ...`), using a short imperative subject (<72 chars) and optional body for context.
- PRs should describe the change, risks, and how to verify; link issues, and include screenshots or GIFs for UI-facing work. Note which Maven commands you ran (at least `mvn test`) and call out any platform-specific steps (e.g., macOS bundling).

## Change Scopes & Agent Protocol (Codex)

This repository uses *explicit change scopes* for PR-sized work.
Each PR MUST implement exactly one scope defined below.
Codex agents MUST follow this protocol strictly.

### Agent Protocol (One Prompt = One PR)

For each PR, the agent MUST:

1. Declare the active PR-SCOPE identifier.
2. Change only files permitted by that scope.
3. Avoid feature expansion beyond scope.
4. Add or update tests where invariants are affected.
5. Ensure `mvn test` passes.

The agent MUST NOT:
- Combine multiple scopes in one PR.
- Introduce UI changes unless explicitly allowed.
- Change persistence formats unless explicitly allowed.

### PR-SCOPE: AC-01 — Treat BibTeX as bridge, not literature manager (Ahrens-aligned)

Intent:
- Align with Ahrens’ “four tools” minimal setup: literature management remains external (e.g., Zotero),
  while Zettelkasten focuses on permanent notes and stable linking.
- Keep BibTeX support strictly as an interoperability layer (import/export, citation keys), not as a
  parallel literature database or workflow hub.

Allowed changes:
- Refactor BibTeX-related code to reduce “manager-like” behavior and UI entanglement where it is not required
  for interoperability.
- Introduce small interfaces/callbacks to remove Swing dependencies from non-UI logic (e.g., replace direct
  `JOptionPane` calls in BibTeX logic with injected notifier/decision hooks).
- Clarify and tighten naming, documentation strings, and tooltips to frame BibTeX as import/export bridge.
- Add/adjust tests to validate unchanged import/export behavior and that headless operation is possible
  for BibTeX parsing/formatting paths.

Forbidden changes:
- No UI redesign or workflow redesign (no new dialogs, no new screens, no rearranging menus).
- No feature additions (no new BibTeX fields, no new import sources, no new citation styles).
- No persistence format changes (no schema changes to `.zkn3` or internal XML structure).
- No behavioral changes to existing BibTeX import/export outputs except where needed to fix clear bugs
  uncovered by tests; if such a change is necessary, it must be minimal, explicitly justified, and
  covered by a regression test.

Acceptance criteria:
- BibTeX functionality remains available and produces the same outputs for existing test fixtures.
- BibTeX parsing/formatting core can run without Swing classes on the classpath (UI interactions are
  isolated behind callbacks/adapters).
- No new “library management” concepts are introduced (no new entities or state that replicate external
  reference manager responsibilities).

Notes:
- Prefer extraction of a small “BibTeXCore” (pure logic) or equivalent internal separation over broad
  refactors. Keep changes narrowly scoped to interoperability and decoupling.
- When in doubt, preserve current behavior and limit changes to moving UI interactions to the edge.

### PR-SCOPE: AC-02 — Constrain formatting (“Constrained Markdown”) and reduce markup surface

Intent:
- Align with Ahrens’ emphasis on simplicity and low distraction: reduce degrees of freedom in note
  formatting so the default workflow remains stable, uniform, and cognitively lightweight.
- Treat formatting/rendering as a projection layer; discourage “authoring-by-styling” behaviors.

Allowed changes:
- Introduce a configuration flag (e.g., “Constrained Markdown”) that constrains supported formatting to a
  small, explicitly defined subset.
- Implement the constraint primarily in parsing/normalization/projection layers (UBB/HTML/Markdown
  conversion utilities), not via new UI controls.
- Extend existing markdown/UBB normalization and lint/test fixtures to enforce the constrained subset.
- Add/adjust tests (golden files or normalization tests) that prove restricted formatting is stable and
  deterministic under the new mode.

Forbidden changes:
- No UI redesign or workflow redesign (no new editors, no new formatting toolbars, no rearranging menus).
- No feature additions to formatting (no new markup syntax, no new styling capabilities).
- No persistence format changes (stored note content format must remain backward compatible).
- No bulk “reformat all notes” migrations. Existing notes must render acceptably; the constraint applies to
  new edits and/or normalization outputs, not destructive rewrite of stored content.

Acceptance criteria:
- When Constrained Markdown is enabled, only the allowed formatting subset is produced by
  normalization/projection (unsupported constructs are either stripped, downgraded to plain text, or
  rendered in a neutral way).
- When Constrained Markdown is disabled, formatting behavior remains unchanged.

### PR-SCOPE: AC-08 — Automatic Markdown workspace export on Zettel save (Pandoc)

Intent:
- When a Zettel is saved/committed, automatically export that Zettel as a `.md` file into a workspace
  directory for downstream Pandoc processing.

Allowed changes:
- Add a small headless service/utility that writes `z<ID>.md`.
- Call the service from the existing “save/commit entry” path.
- Use existing Pandoc invocation patterns already present in export code (`-f html -t <format>`).
- Add tests (unit-level) that validate: (a) no crash when disabled/missing workspace, (b) correct filename
  and that a write attempt occurs when enabled.

Forbidden changes:
- No UI layout/behaviour changes.
- No persistence format changes.
- No new settings dialog controls.
- No redesign of editor/content model.

Acceptance criteria:
- Workspace dir resolution:
  1. `ZETTELKASTEN_WORKSPACE_DIR` env var if present and non-empty
  2. else `${user.home}/workspace` if it exists
  3. else: do nothing; log `INFO` once per run.
- Output filename: `z<entryNumber>.md`
- Pandoc call: `pandoc -f html -t markdown -o <outfile> <tmpHtmlFile>`
- If Pandoc missing or fails: do not interrupt save; log `WARNING`.
- `mvn test` passes.


### PR-SCOPE: AC-06 — Decouple data/model from Swing UI

Intent:
- Align with Ahrens’ longevity principle: the Zettelkasten core must outlive UI technology.

Allowed changes:
- Remove Swing/UI references from data and model classes.
- Introduce interfaces, events, or observer abstractions if needed.
- Update tasks/services to use abstractions instead of UI types.

Forbidden changes:
- UI behavior or layout changes.
- Persistence format changes.
- Feature additions unrelated to decoupling.

Acceptance criteria:
- Core classes (e.g. `Daten`) can be instantiated without Swing.
- Core operations can run headlessly in tests.
- Existing UI continues to function unchanged.

Notes:
- Prefer minimal interfaces over event buses unless necessary.

### PR-SCOPE: AC-07 — Test-runner hygiene (isolate JExample from TestNG JUnit-mode)

Intent:
- Stop TestNG from running JExample/JUnit tests in JUnit-mode.

Allowed changes:
- Maven Surefire/TestNG suite configuration changes.
- Test-only changes required to keep JExample tests running under JUnit.

Forbidden changes:
- Production code changes.
- UI behavior or layout changes.
- Persistence format changes.

Acceptance criteria:
- JExample-dependent tests run via JUnit provider.
- TestNG tests still run via TestNG provider.
- `mvn test` completes without the JExample/TestNG JUnit-mode warning.
