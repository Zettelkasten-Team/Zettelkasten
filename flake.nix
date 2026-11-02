{
  description = "Zettelkasten (Swing) dev shell with JDK 25 + Maven + IntelliJ IDEA CE + repomix-md";

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

        # Pin the official Temurin release independently of other nixpkgs updates.
        # https://github.com/adoptium/temurin25-binaries/releases/tag/jdk-25.0.5%2B8
        temurinRelease = "25.0.5+8";
        temurinArchives = {
          x86_64-darwin = {
            platform = "x64_mac";
            sha256 = "32cd4d2151c91aa00d0fc911edfd05245ed40fa6b1aa894d5f35c928cd67a59d";
          };
          aarch64-darwin = {
            platform = "aarch64_mac";
            sha256 = "36dc14d797aaff6d89aab302cd053fdba6207dfdcf772052fa7b023a94b0e857";
          };
          x86_64-linux = {
            platform = "x64_linux";
            sha256 = "68982529869257d0ee6b824ec5aa270ff8b118bb438670da0b97ebdab178d7fb";
          };
          aarch64-linux = {
            platform = "aarch64_linux";
            sha256 = "bb5b2ded252f21c6090cf780b0a408ad0d83bbb557e702955643557f13231880";
          };
        };
        archive = temurinArchives.${system};
        jdk = pkgsUnstable.temurin-bin-25.overrideAttrs (_: {
          version = "25.0.5";
          src = pkgsUnstable.fetchurl {
            url = "https://github.com/adoptium/temurin25-binaries/releases/download/jdk-${temurinRelease}/OpenJDK25U-jdk_${archive.platform}_hotspot_25.0.5_8.tar.gz";
            inherit (archive) sha256;
          };
        });

        maven = pkgs.maven.override { inherit jdk; };

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
        packages.temurin = jdk;
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
