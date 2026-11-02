# Historical PR Scope Catalogue

Read with the permanent rules and [agent protocol](../AGENTS.md). The definitions below preserve
historical task boundaries; they are not a queue of automatically authorized work or a closed list
of permitted future tasks. Status assessment: 2026-11-02, against
`90c7748e26dfe31fd0cd32ad64058e85efbe73c8` (`main`). Implementation evidence is static inspection
and available Git history; no Java tests were rerun for this documentation-only review.

| ID | Historical title | Status | Main evidence / open criterion |
| --- | --- | --- | --- |
| [AC-01](#ac-01) | BibTeX bridge | partially implemented | `a0940d8`; callbacks present, Swing-free classpath unproven |
| [AC-02](#ac-02) | Constrained Markdown | unclear | Optional mode/subset not found; baseline tables are separate |
| [AC-06](#ac-06) | Model/Swing decoupling | partially implemented | `ad73291`, `c3e5fcf`, `807b286`; indirect coupling remains |
| [AC-07](#ac-07) | Test-runner hygiene | completed | `6610909`; JExample/TestNG provider separation |
| [AC-08](#ac-08) | Markdown workspace export | partially implemented | Export/hooks/tests present; missing-workspace log level differs |
| [AC-09](#ac-09) | Literal Markdown code spans | partially implemented | `bd39c72`; main acceptance test not selected by current Vintage configuration |
| [AC-10](#ac-10) | Dependency hygiene | completed | `239d398`; jsoup/copy_down removed, build evidence recorded |
| [AC-11](#ac-11) | Test fixture isolation | partially implemented | `da3a6d1`, `eec4b59`, `e50e176`; residual settings access |

## Identifier Provenance and Gaps

`a3d8ac6` introduces the protocol with AC-06; later commits add the other definitions. No binding
meaning for the letters `AC` was found. Searches of current tracked documentation/source and the
available history (`git log --all -G 'AC-0[345]'`, including AGENTS.md/docs/README) found no definitions
for AC-03, AC-04 or AC-05. Their titles, intent, permitted changes, acceptance and disposition are
**unclear**; absence does not establish completion, cancellation or reserved numbers. External or
unavailable history could answer this; do not fill the gaps by inference.

AC-02 once also labelled workspace export; the explicit correction to AC-08 is recorded below.
Always identify a historical task by title/context as well as number, not a number reused in an
unrelated discussion. No current scope is marked `active` merely because it has open criteria;
this review assigns no implementation work. No current task has evidence of being `superseded`;
only the former duplicate export label has an explicit replacement.

## Overlaps and Continuing Rules

- AC-01 is a bounded BibTeX bridge separation; AC-06 is wider model/UI decoupling. They can touch the
  same classes, but one does not authorize the whole other task. Small interface boundaries persist.
- AC-02 restricts an optional mode; AC-09 fixes baseline correctness. They overlap conversion code,
  not authorization. Pipe tables in `a58f8f7` are a separate baseline extension.
- AC-08 uses rendered HTML for export without authorizing UI or persistence changes. Rendering and
  export scopes may share utilities; agree exact components and compatibility checks before editing.
- AC-07 changes the runner; AC-11 explicitly excludes it. Discovering an unexecuted Jupiter fixture
  is evidence to report, not permission for an incidental POM change.
- AC-10 permits removal of dead dependency-only code; AC-11 forbids altering assertions/identities.
  A dependency change that needs substantive test changes must declare those separately and must
  not claim fixture-isolation authorization.
- Historical task-specific UI/feature prohibitions apply within their agreed task. Permanent rules
  (data integrity, deliberate UI/persistence scope, deterministic tests, dependency economy and
  projection-only rendering) continue after an individual task closes. Historical file permissions
  are component descriptions, not an automatic stage/commit allowlist.

## Scope Definitions and Evidence

The original intent, allowed/forbidden changes and acceptance criteria are retained below. They
remain historical criteria, including unmet ones; the evidence notes do not silently weaken them.
AC-02 is clarified by the baseline/mode distinction above and in AGENTS.md.

<a id="ac-01"></a>

### PR-SCOPE: AC-01 — Treat BibTeX as bridge, not literature manager (Ahrens-aligned)

**Status: partially implemented.** `a0940d8` extracted
[BibTeXUiCallbacks](../src/main/java/de/danielluedecke/zettelkasten/database/BibTeXUiCallbacks.java)
and removed direct view notifications from
[BibTeX](../src/main/java/de/danielluedecke/zettelkasten/database/BibTeX.java).
[BibTeXTest](../src/test/java/de/danielluedecke/zettelkasten/database/BibTeXTest.java)
checks import-summary callbacks and imported content with `NO_OP` callbacks. The commit records
`mvn test`. This demonstrates a concrete bridge/UI separation, not the full classpath criterion:
BibTeX still depends on Settings (which imports Swing), and its logging uses Constants, whose
logger references `ZettelkastenView.class`. No Swing-free classpath test or comprehensive
import/export output-equivalence proof was found. Those acceptance points remain open.
The bridge principle remains permanent; this finding does not authorize a new refactoring.

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

<a id="ac-02"></a>

### PR-SCOPE: AC-02 — Constrain formatting (“Constrained Markdown”) and reduce markup surface

**Status: unclear.** Baseline conversion in
[HtmlUbbUtil](../src/main/java/de/danielluedecke/zettelkasten/util/HtmlUbbUtil.java) is controlled by
`Settings.getMarkdownActivated()`. No separate Constrained-Markdown flag or defined runtime subset
was found in production code. The
[markdownlint tests](../src/test/java/de/danielluedecke/zettelkasten/markdownlint/MarkdownSupportMatrixTest.java)
record compatibility limitations; they do not implement an optional restriction mode. Whether the
mode is still scheduled, deferred or abandoned is not established by the repository.

**Policy clarification:** Baseline Markdown may evolve under separately agreed scopes. Commit
`a58f8f71100b5b58567ebece8afefa924cecd260` adds limited pipe-table projection and quote-boundary
preservation, with
[HtmlUbbUtilMarkdownProjectionTest](../src/test/java/de/danielluedecke/zettelkasten/util/HtmlUbbUtilMarkdownProjectionTest.java)
and [documented limits](markdown-pipe-tables.md). It is a legitimate baseline feature, not a
retroactive AC-02 violation. New baseline syntax must explicitly state its constrained-mode
behavior; a future optional subset must not automatically grow with the baseline. The original
prohibition on new formatting features below concerns implementing the restriction mode itself.

**Identifier history:** `2612dfc` used AC-02 for both this task (then called “Ahrens mode”) and
workspace export. `df45543` explicitly renamed workspace export to AC-08 and this mode to
“Constrained Markdown”. That duplicate export label is superseded by AC-08; this does not establish
that the restriction-mode task itself was superseded.

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

<a id="ac-06"></a>

### PR-SCOPE: AC-06 — Decouple data/model from Swing UI

**Status: partially implemented.**
[DatenUiCallbacks](../src/main/java/de/danielluedecke/zettelkasten/database/DatenUiCallbacks.java),
`Daten(Document)` and
[SearchRequestsUiCallbacks](../src/main/java/de/danielluedecke/zettelkasten/database/SearchRequestsUiCallbacks.java)
provide concrete headless seams. `ad73291` moves form-image creation out of Daten;
`c3e5fcf` decouples SearchRequests from the view; `807b286` tests backup propagation.
[DatenDeleteZettelTest](../src/test/java/de/danielluedecke/zettelkasten/database/DatenDeleteZettelTest.java)
(`b210fd9`) characterizes deletion without constructing a view;
[ZettelkastenViewSearchRequestsCallbacksTest](../src/test/java/de/danielluedecke/zettelkasten/ZettelkastenViewSearchRequestsCallbacksTest.java)
and [view backup-state test](../src/test/java/de/danielluedecke/zettelkasten/ZettelkastenViewSearchRequestsBackupStateTest.java)
cover the callback boundary. Complete Swing-free core operation is not demonstrated: shared
Settings still imports Swing, and Constants initializes its logger from `ZettelkastenView.class`.
The direction of dependency separation remains permanent; completed slices do not close the whole
model/task migration or prove every UI path unchanged.

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

<a id="ac-07"></a>

### PR-SCOPE: AC-07 — Test-runner hygiene (isolate JExample from TestNG JUnit-mode)

**Status: completed (bounded provider-separation task).** `6610909` introduces separate
Surefire `junit-tests` and `testng-tests` executions and records successful `mvn -q test` / `mvn test`.
The current [POM](../pom.xml) retains the split and `junit=false`; the TestNG execution uses
[the explicit suite](../src/test/resources/testng.xml). JExample runner tests include
[HistoryTest](../src/test/java/de/danielluedecke/zettelkasten/data/HistoryTest.java).
The previously recorded 11483 suite log reports 101 JUnit and 55 TestNG cases without failures,
11 skipped in total, and no JExample/TestNG JUnit-mode warning. This review did not rerun it.

**Separate discovery limitation:** the current JUnit execution explicitly includes only the
Vintage engine. Jupiter test sources used by AC-01/08/09/11 therefore cannot be counted as executed
by that default configuration. Correcting or verifying Jupiter discovery needs a separately agreed
runner scope; it is not a fixture change or a reason to undo the JExample/TestNG separation.

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

<a id="ac-08"></a>

### PR-SCOPE: AC-08 — Automatic Markdown workspace export on Zettel save (Pandoc)

**Status: partially implemented.** `2612dfc` adds export, `df45543` hardens it and assigns
AC-08, `64b1fc3` simplifies the heading, and `77313b1` sanitizes UI-only HTML. The latter commits
record successful `mvn test` runs.
[MarkdownWorkspaceExporter](../src/main/java/de/danielluedecke/zettelkasten/util/MarkdownWorkspaceExporter.java)
resolves the environment/fallback directory, invokes Pandoc with the specified arguments,
cleans up temporary HTML, and catches process failures. Hooks exist in `EditorFrame.exportMarkdownOnSave`
and `SaveFileTask.triggerMarkdownExport`.
[exporter tests](../src/test/java/de/danielluedecke/zettelkasten/util/MarkdownWorkspaceExporterTest.java),
[editor tests](../src/test/java/de/danielluedecke/zettelkasten/EditorFrameMarkdownExportTest.java) and
[save-task tests](../src/test/java/de/danielluedecke/zettelkasten/tasks/SaveFileTaskMarkdownExportTest.java)
cover absent workspace, writing, save hooks and the minimal heading with a mock Pandoc.

A concrete criterion is unmet: missing workspace is logged once via `AtomicBoolean`, but at
`WARNING`, not the required `INFO`; an unconfigured Pandoc path returns before workspace resolution.
The script-based tests skip Windows, and these tests use Jupiter (see AC-07 discovery limitation).
Neither complete cross-platform acceptance nor a currently executed default-suite proof is established.

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
- Exported Markdown starts with a minimal heading line: `# Zettel <entryNumber> – <title>` (no word count, timestamps, or ratings).
- Output filename: `z<entryNumber>.md`
- Pandoc call: `pandoc -f html -t markdown -o <outfile> <tmpHtmlFile>`
- If Pandoc missing or fails: do not interrupt save; log `WARNING`.
- `mvn test` passes.

<a id="ac-09"></a>

### PR-SCOPE: AC-09 — Markdown HTML correctness (prevent emphasis inside code spans)

**Status: partially implemented (correction present; acceptance not fully demonstrated).**
`bd39c72` adds code-span protection around Markdown emphasis processing in
[HtmlUbbUtil](../src/main/java/de/danielluedecke/zettelkasten/util/HtmlUbbUtil.java).
[HtmlUbbUtilMarkdownCodeSpanValidationTest](../src/test/java/de/danielluedecke/zettelkasten/util/HtmlUbbUtilMarkdownCodeSpanValidationTest.java)
asserts literal underscores/HTML characters, absence of emphasis inside code, and validator success
with bold list content. Later escaped-backtick correction `d58c86c` has separate
[JUnit 4 regression coverage](../src/test/java/de/danielluedecke/zettelkasten/util/HtmlUbbUtilBacktickEscapeTest.java).
The original protection is guarded by Markdown activation, consistent with the disabled-mode boundary.
However, the principal acceptance test is Jupiter, excluded by the current Vintage-only selection;
its presence is not a current successful execution. A dedicated disabled-mode code-span equivalence
assertion and full execution evidence for these original acceptance criteria remain unestablished.
This historical identifier does not redefine every subsequent Markdown fix as the same task.

Intent:
- Ensure Markdown inline code spans render as literal text and do not receive emphasis parsing.
- Prevent invalid HTML produced by Markdown/UBB conversion when code spans contain underscores.

Allowed changes:
- Add or adjust Markdown/UBB-to-HTML conversion logic to treat code spans as literal content.
- Add/adjust HTML validation tests covering the conversion and validator output.

Forbidden changes:
- No UI layout/behaviour changes.
- No persistence format changes.
- No new formatting features beyond correcting parsing inside code spans.

Acceptance criteria:
- Inline code containing underscores (e.g., `ZETTELKASTEN_WORKSPACE_DIR`) renders without emphasis tags.
- HTML validation passes for entries with inline code, bold markers, and list content.
- When Markdown is disabled, behavior is unchanged.
- `mvn test` passes.

<a id="ac-10"></a>

### PR-SCOPE: AC-10 — Dependency hygiene and removal of unused dependencies

**Status: completed (jsoup/copy_down removal).** `239d398` removes the two POM dependencies
and unused `HtmlValidator.isWellFormed`; `19a86c6` documents the scope, merged by `c443217`.
The removal commit records passing tests and `mvn clean package`, and absence from the dependency
graph and shaded JAR. Current [POM](../pom.xml) and source searches show no remaining dependency/use;
a read-only ZIP inspection of the existing `target/Zettelkasten.jar` in this review found zero
`org/jsoup/` or `io/github/furstenheim/` entries. The active validator remains `isValidHTML`.
No dependency resolution or rebuild was performed for this documentation task. This closes the
identified removal, not an assertion that all dependencies will remain necessary forever.
Keep dependency economy as a permanent principle; use new scopes for later removals.

Intent:
- Keep the runtime and packaged dependency surface limited to
  dependencies required by observable application behaviour.
- Prefer removal of unused dependencies over version maintenance
  or compensating machinery.

Allowed changes:
- Remove dependencies with no remaining production or test use.
- Remove dead code whose sole purpose was to use such dependencies.
- Adjust pom.xml accordingly.
- Add or adjust tests where observable behaviour is affected.

Forbidden changes:
- No feature changes.
- No UI behaviour or layout changes.
- No persistence format changes.
- No replacement dependency unless required by an existing behaviour.
- No unrelated dependency upgrades.

Acceptance criteria:
- Existing behaviour and tests remain unchanged.
- mvn test passes.
- Removed dependencies are absent from Maven dependency tree.
- Removed runtime libraries are absent from the shaded artifact.

<a id="ac-11"></a>

### PR-SCOPE: AC-11 — Test fixture isolation

**Status: partially implemented.** `da3a6d1` fixes ignored JUnit rules and unmanaged files;
`eec4b59` isolates Jupiter settings; `e50e176` isolates five JUnit 4 renderer tests. Their diffs retain
assertions and test identities while adding managed directories and teardown for `user.dir`/`user.home`.
Examples are
[SettingsTest](../src/test/java/de/danielluedecke/zettelkasten/database/SettingsTest.java),
[BibTeXTest](../src/test/java/de/danielluedecke/zettelkasten/database/BibTeXTest.java),
[ZettelkastenAppRefactorTest](../src/test/java/de/danielluedecke/zettelkasten/ZettelkastenAppRefactorTest.java)
and [HtmlUbbUtilMarkdownImagesTest](../src/test/java/de/danielluedecke/zettelkasten/util/HtmlUbbUtilMarkdownImagesTest.java).
These are implemented fixture fixes, not proof of repository-wide isolation:
[ZettelkastenViewUpdateDisplayTest](../src/test/java/de/danielluedecke/zettelkasten/ZettelkastenViewUpdateDisplayTest.java)
still constructs real Settings without redirecting the settings locations when a display is available
(and skips headless). The Jupiter execution caveat from AC-07 also applies. Thus machine/order
independence and cleanup for every affected path are not fully demonstrated. Do not change assertions
or the runner under the guise of completing these fixtures.

Intent:
- Make test outcomes independent of the developer's machine, of other tests and of
  execution order, and keep test runs from leaving state behind.
- Treat fixture setup and teardown as test infrastructure, not as a place to change
  what a test asserts.

Allowed changes:
- Replace ineffective or framework-mismatched fixture mechanisms in existing tests
  (e.g., a JUnit 4 `@Rule` inside a JUnit Jupiter class) with ones the running framework
  applies, such as JUnit Jupiter `@TempDir`.
- Save and restore global JVM state that a test modifies (system properties, default
  locale, static configuration).
- Move files a test creates out of the working directory into managed temporary
  directories, with cleanup that runs even when a test fails.

Forbidden changes:
- Production code changes.
- Changing assertions, expected values or domain expectations; renaming, removing,
  disabling or skipping tests.
- Test-runner or Maven configuration changes (AC-07).
- UI behavior or layout changes.
- Persistence format changes.

Acceptance criteria:
- Test setup is deterministic and does not depend on execution order.
- Global state modified by a test is restored afterwards, also when the test fails.
- Files created by tests are cleaned up, also when assertions fail; nothing is written
  to the repository root or left in the temporary directory.
- Test identities and existing assertions are unchanged.
- `mvn test` results are unchanged apart from the removed side effects.
