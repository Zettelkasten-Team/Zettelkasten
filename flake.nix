{
  description = "Zettelkasten (Swing) dev shell with JDK 8 + Maven + IntelliJ IDEA CE";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-24.05";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs { inherit system; };

        # Pick a JDK 8 available in this nixpkgs; prefer Temurin, then Zulu, then jdk8 if present.
        jdk =
          if builtins.hasAttr "temurin-bin-8" pkgs then pkgs.temurin-bin-8
          else if builtins.hasAttr "zulu8" pkgs then pkgs.zulu8
          else if builtins.hasAttr "jdk8" pkgs then pkgs.jdk8
          else throw "This nixpkgs does not provide a JDK 8; please switch to a channel that includes temurin-bin-8 or zulu8.";

        maven = pkgs.maven;

        # macOS launcher so IntelliJ inherits the devShell env
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
          echo "IntelliJ IDEA CE app not found. Install it, then run: idea-community ."
          exit 1
        '';
      in {
        devShells.default = pkgs.mkShell {
          packages =
            [ jdk maven ]
            ++ (if pkgs.stdenv.isLinux then [ pkgs.jetbrains.idea-community ] else [ ideaMacWrapper ]);

          shellHook = ''
            export JAVA_HOME=${jdk}
            export MAVEN_OPTS="-Djava.awt.headless=true"
            # If you want IntelliJ itself to run on JDK 8 (usually not needed), uncomment:
            # export IDEA_JDK="$JAVA_HOME"

            # Ensure Maven uses JDK 8 toolchain (required by enforcer [1.8,1.9))
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

            echo "▶ Zettelkasten (Swing) dev shell"
            echo "   Java:  $(java -version 2>&1 | head -n1)"
            echo "   Maven: $(mvn -v | head -n1)"
            echo
            echo "IDEA:  idea-community .   (or: nix run .#idea-ce .)"
            echo "Build: mvn -q -DskipTests package"
          '';
        };

        apps.idea-ce = {
          type = "app";
          program =
            if pkgs.stdenv.isLinux
            then "${pkgs.jetbrains.idea-community}/bin/idea-community"
            else "${ideaMacWrapper}/bin/idea-community";
        };
      }
    );
}
