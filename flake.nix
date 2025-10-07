{
  description = "Zettelkasten dev shell with JDK 8 + Maven toolchains";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-24.05";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs { inherit system; };

        # Prefer Temurin JDK 8; fall back to jdk8, then default jdk
        jdk =
          if builtins.hasAttr "temurin-bin-8" pkgs then pkgs.temurin-bin-8
          else if builtins.hasAttr "jdk8" pkgs then pkgs.jdk8
          else pkgs.jdk;

        maven = pkgs.maven;
      in {
        devShells.default = pkgs.mkShell {
          packages = [ jdk maven pkgs.git pkgs.gnugrep pkgs.gnused ];

          # Export JAVA_HOME for tools (and echo it for visibility)
          JAVA_HOME = "${jdk}";
          MAVEN_OPTS = "-Djava.awt.headless=true";

          shellHook = ''
            echo "== Zettelkasten dev shell =="
            echo "JAVA_HOME=$JAVA_HOME"
            "$JAVA_HOME/bin/java" -version
            mvn -v

            # Write a toolchains.xml with an ABSOLUTE jdkHome (no $JAVA_HOME!)
            mkdir -p "$HOME/.m2"
            cat > "$HOME/.m2/toolchains.xml" <<'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<toolchains>
  <toolchain>
    <type>jdk</type>
    <provides>
      <version>1.8</version>
      <vendor>Temurin</vendor>
    </provides>
    <configuration>
      <jdkHome>${jdk}</jdkHome>
    </configuration>
  </toolchain>
</toolchains>
EOF
            echo "Wrote $HOME/.m2/toolchains.xml (vendor=Temurin, version=1.8)"
          '';
        };
      });
}
