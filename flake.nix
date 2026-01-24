{
  description = "Zettelkasten (Swing) dev shell with JDK 8 + Maven + IntelliJ IDEA CE + repomix-md";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-24.05";
    nixpkgs-unstable.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, nixpkgs-unstable, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs { inherit system; };
        pkgsUnstable = import nixpkgs-unstable { inherit system; };

        # --- JDK 8 selection (unchanged logic) ---
        jdk =
          if builtins.hasAttr "temurin-bin-8" pkgs then pkgs.temurin-bin-8
          else if builtins.hasAttr "zulu8" pkgs then pkgs.zulu8
          else if builtins.hasAttr "jdk8" pkgs then pkgs.jdk8
          else throw "No JDK 8 available in this nixpkgs.";

        maven = pkgs.maven;

        # --- repomix: disable failing test suite (macOS / sysctl issue) ---
        repomix =
          (pkgsUnstable.repomix or (throw "repomix not found in nixpkgs-unstable"))
            .overrideAttrs (_: {
              doCheck = false;
            });

        # --- repomix-md wrapper ---
        repomixMd = pkgs.writeShellScriptBin "repomix-md" ''
          set -euo pipefail
          OUT="''${REPOMIX_MD_OUT:-zettelkasten-repomix-output.md}"
          exec ${repomix}/bin/repomix --style markdown -o "$OUT" "$@"
        '';

        # --- macOS IntelliJ launcher ---
        ideaMacWrapper = pkgs.writeShellScriptBin "idea-community" ''
          set -euo pipefail
          for CAND in \
            "/Applications/IntelliJ IDEA CE.app/Contents/MacOS/idea" \
            "$HOME/Applications/IntelliJ IDEA CE.app/Contents/MacOS/idea"
          do
            if [ -x "$CAND" ]; then exec "$CAND" "$@"; fi
          done
          if command -v mdfind >/dev/null 2>&1; then
            FOUND=$(mdfind 'kMDItemCFBundleIdentifier == "com.jetbrains.intellij.ce"' | head -n1)
            if [ -n "$FOUND" ]; then exec "$FOUND/Contents/MacOS/idea" "$@"; fi
          fi
          echo "IntelliJ IDEA CE app not found."
          exit 1
        '';
      in {
        devShells.default = pkgs.mkShell {
          packages =
            [ jdk maven repomixMd ]
            ++ (if pkgs.stdenv.isLinux
                then [ pkgs.jetbrains.idea-community ]
                else [ ideaMacWrapper ]);

          shellHook = ''
            export JAVA_HOME=${jdk}
            export MAVEN_OPTS="-Djava.awt.headless=true"

            mkdir -p "$HOME/.m2"
            cat > "$HOME/.m2/toolchains.xml" <<'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<toolchains>
  <toolchain>
    <type>jdk</type>
    <provides>
      <version>1.8</version>
    </provides>
    <configuration>
      <jdkHome>${jdk}</jdkHome>
    </configuration>
  </toolchain>
</toolchains>
EOF

            echo "▶ Zettelkasten dev shell"
            echo "   Java:    $(java -version 2>&1 | head -n1)"
            echo "   Maven:  $(mvn -v | head -n1)"
            echo "   Repomix: repomix-md → zettelkasten-repomix-output.md"
            echo
            echo "IDEA:  idea-community ."
          '';
        };

        apps.idea-ce = {
          type = "app";
          program =
            if pkgs.stdenv.isLinux
            then "${pkgs.jetbrains.idea-community}/bin/idea-community"
            else "${ideaMacWrapper}/bin/idea-community";
        };

        apps.repomix-md = {
          type = "app";
          program = "${repomixMd}/bin/repomix-md";
        };
      }
    );
}
