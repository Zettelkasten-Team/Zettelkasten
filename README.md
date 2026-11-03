# Zettelkasten
<img src="src/main/resources/de/danielluedecke/zettelkasten/resources/icons/zkn3-256x256.png" height="128" align="right"/>
<p>
    <a href="https://github.com/Zettelkasten-Team/Zettelkasten/releases" alt="Release">
        <img src="https://img.shields.io/github/release/Zettelkasten-Team/Zettelkasten.svg" />
    </a>
     <a href="https://github.com/Zettelkasten-Team/Zettelkasten/releases" alt="Downloads">
        <img src="https://img.shields.io/github/downloads/Zettelkasten-Team/Zettelkasten/total.svg" />
     </a>
    <a href="https://github.com/Zettelkasten-Team/Zettelkasten/graphs/contributors" alt="Contributors">
        <img src="https://img.shields.io/github/contributors/Zettelkasten-Team/Zettelkasten" />
    </a>
    <img src="https://github.com/Zettelkasten-Team/Zettelkasten/workflows/CI/badge.svg" alt="build status"/>
</p>
Zettelkasten is a knowledge management tool. It is inspired by Niklas Luhmann's note-taking system (Zettelkasten is German for slip box).

![screenshot](http://zettelkasten.danielluedecke.de/img/gallery/zkn1.png)
*Zettelkasten main window on macOS*

## Download Zettelkasten
Here you can download the latest version of Zettelkasten: [Releases](https://github.com/Zettelkasten-Team/Zettelkasten/releases).

**Note** to users:
> This information is relevant to users of Zettelkasten.
> To run Zettelkasten you need a Java Runtime Environment for Java 25 or newer, e.g. [Eclipse Temurin](https://adoptium.net/) (on macOS: `brew install openjdk@25`).

## Wiki
Here you can find and add content about Zettelkasten in a [Wiki](https://github.com/Zettelkasten-Team/Zettelkasten/wiki).

## Zettelkasten Build
**Note** to software developers:
> This information is only relevant to software developers.
> To build the Zettelkasten, you need a Java Development Kit (JDK) for Java 25 or newer and Maven 3.9+.

The Nix/direnv development environment pins **Eclipse Temurin 25.0.5+8**
using versioned upstream archives and SHA-256 hashes for macOS and Linux
(x86_64 and aarch64). `flake.lock` continues to pin all other dependencies.
No fallback to another JDK distribution is used.

From the repository root, enter it with `nix develop` (or `direnv allow`
when using direnv), then verify:

```shell
java -version
javac -version
mvn -version
```

All three commands should report Java 25.0.5; Maven should use the pinned
Temurin JDK. The JDK can also be built independently with
`nix build .#temurin --no-link`. When updating it, update the release,
archive names and all four checksums in `flake.nix` together.

To get a local repository, use Git:

```shell
git clone git@github.com:Zettelkasten-Team/Zettelkasten.git
```

Then, in the root directory of the local repository, run the following command line:

```shell
mvn clean package
```
Note: `mvn` is [Apache Maven](https://maven.apache.org/install.html). Confirm with `mvn -v` in a new shell.

Find the build result in your `target` directory.

### macOS app bundle

On macOS, check that `mvn -v` reports Java 25, then run `mvn clean package`.
The automatically activated `mac` profile copies Maven's JDK into
`target/Zettelkasten.app/Contents/Java/jdk`. The launcher loads that copy.
The standalone artifact remains `target/Zettelkasten.jar`. On macOS,
`mvn -P '!mac' clean package` disables JDK embedding when building for
standalone JAR use.

The embedded JDK must match the target Mac's native architecture.
The launcher has both Intel and Apple Silicon slices, but each build
embeds a single JDK. Build separately for `x86_64` and `arm64` and use
the same JDK distribution, patch version and architecture when rebuilding
a release. The Intel bundle was tested with Temurin 25.0.5. An Apple
Silicon build and launch test are still pending.

The Intel smoke test used a local ZIP and launched isolated copies of the
extracted bundle directly and via macOS Launch Services (`open`). Both
loaded the bundled JDK and showed one main window. The tested app bundle
was unsigned. Developer ID signing and Apple notarization are unverified.
The test also leaves open the distribution flow for a downloaded,
quarantined ZIP opened through Finder and Gatekeeper. The optional DMG
path was outside the test.

### Windows executable packaging

Launch4j runs automatically during `mvn package` on Windows. It is inactive
in the normal macOS/Linux build; macOS still produces the JAR and app bundle.
For an explicit cross-build on macOS or Linux, run:

```shell
mvn -Pwindows -DskipTests package
```

The executable is `target/Zettelkasten.exe`. Its launcher looks for Java on the
**target Windows machine** through `%JAVA_HOME%` and `%PATH%`, then the Windows
registry. Launch4j checks for Java 25 or newer and a 64-bit runtime in both search
stages. A JRE is sufficient; no JDK or bundled runtime is required. If Java is
installed without environment variables or recognized registry entries, configure
`JAVA_HOME` or add its `bin` directory to `PATH`. No build-host/Nix path is embedded.

The generated Launch4j configuration is retained in `target/launch4j-config.xml`.
Version resources use the explicit project name, description and existing
copyright attribution from `pom.xml`. Automatic metadata defaults are disabled:
no synthetic company name or inception year is supplied. The executable is
unsigned; release signing requires a separately managed certificate and process.

Packaging and runtime lookup are separate checks: a successful cross-build does
not validate startup on Windows. Native Windows startup remains a release check.
An old EXE in `target` may survive a later macOS-only incremental build; use a
fresh output directory to verify which artifacts the active profiles generate.



## License

The GPLv3 applies. For detail information see [LICENSE](./LICENSE).
