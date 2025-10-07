{
  description = "Zettelkasten dev shell with JDK 8 + Maven toolchains (vendor-agnostic)";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-24.05";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs { inherit system; };

        # Prefer a vendor-agnostic Java 8; fall back to any JDK if 8 is unavailable
        jdk =
          if builtins.hasAttr "jdk8" pkgs then pkgs.jdk8
          else if builtins.hasAttr "zulu8" pkgs then pkgs.zulu8
          else if builtins.hasAttr "temurin-bin-8" pkgs then pkgs."temurin-bin-8"
          else pkgs.jdk;

        maven = pkgs.maven;
        jdkPath = "${jdk}";
      in {
        devShells.default = pkgs.mkShell {
          packages = [ jdk maven pkgs.git pkgs.gnugrep pkgs.gnused ];

          # Export JAVA_HOME for tools
          JAVA_HOME = jdkPath;
          MAVEN_OPTS = "-Djava.awt.headless=true";

          shellHook = ''
            echo "== Zettelkasten dev shell =="
            echo "JAVA_HOME=$JAVA_HOME"
            "$JAVA_HOME/bin/java" -version || true
            mvn -v

            # Vendor-agnostic toolchain for Java 8
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
      <jdkHome>${jdkPath}</jdkHome>
    </configuration>
  </toolchain>
</toolchains>
EOF
            echo "Wrote $HOME/.m2/toolchains.xml (version=1.8, vendor-agnostic, jdkHome=${jdkPath})"
          '';
        };
      });
}
