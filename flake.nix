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

        # --- JDK 25 (not in nixos-24.05, so take it from unstable) ---
        jdk =
          if builtins.hasAttr "temurin-bin-25" pkgsUnstable then pkgsUnstable.temurin-bin-25
          else if builtins.hasAttr "jdk25" pkgsUnstable then pkgsUnstable.jdk25
          else throw "No JDK 25 available in nixpkgs-unstable.";

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
