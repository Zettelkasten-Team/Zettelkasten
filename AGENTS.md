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

## Commit & Pull Request Guidelines
- Follow the existing conventional commits style seen in history (`feat(scope): ...`, `fix(build): ...`, `docs(readme): ...`), using a short imperative subject (<72 chars) and optional body for context.
- PRs should describe the change, risks, and how to verify; link issues, and include screenshots or GIFs for UI-facing work. Note which Maven commands you ran (at least `mvn test`) and call out any platform-specific steps (e.g., macOS bundling).
