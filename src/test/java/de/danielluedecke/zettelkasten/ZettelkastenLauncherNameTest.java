package de.danielluedecke.zettelkasten;

import static org.junit.Assert.*;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import javax.tools.ToolProvider;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Tests the real Java-8-compatible launcher in a separate JVM, without AWT. */
public class ZettelkastenLauncherNameTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder(new File("target"));
    private Path jar;

    @Before public void createLauncherJar() throws Exception {
        Path root = temporary.getRoot().toPath();
        Path source = root.resolve("ZettelkastenApp.java");
        Files.write(source, ("package de.danielluedecke.zettelkasten;"
                + "public class ZettelkastenApp { public static void main(String[] args) {"
                + "if (!args[0].equals(System.getProperty(\"apple.awt.application.name\")))"
                + " throw new AssertionError(\"Name was not set before application loading\");"
                + "if (args.length != 2 || !args[1].equals(\"forwarded\")) throw new AssertionError();"
                + "System.out.println(\"launcher-ok\"); }}").getBytes(StandardCharsets.UTF_8));
        assertEquals(0, ToolProvider.getSystemJavaCompiler().run(null, null, null,
                "--release", "8", "-d", root.toString(), source.toString()));
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, ZettelkastenLauncher.class.getName());
        jar = root.resolve("launcher.jar");
        String pkg = "de/danielluedecke/zettelkasten/";
        try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(jar), manifest)) {
            out.putNextEntry(new JarEntry(pkg + "ZettelkastenLauncher.class"));
            try (InputStream in = ZettelkastenLauncher.class.getResourceAsStream("ZettelkastenLauncher.class")) {
                assertNotNull(in);
                byte[] bytes = in.readAllBytes();
                assertEquals("Launcher must still load on Java 8", 52, ((bytes[6] & 255) << 8) | (bytes[7] & 255));
                out.write(bytes);
            }
            out.closeEntry();
            out.putNextEntry(new JarEntry(pkg + "ZettelkastenApp.class"));
            Files.copy(root.resolve(pkg + "ZettelkastenApp.class"), out);
            out.closeEntry();
        }
    }

    private void launch(String os, String expected) throws Exception {
        String executable = System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java";
        Process process = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", executable).toString(),
                "-Dos.name=" + os, "-Dapple.awt.application.name=Existing", "-jar", jar.toString(), expected, "forwarded")
                .redirectErrorStream(true).start();
        try {
            assertTrue("Launcher timed out", process.waitFor(30, TimeUnit.SECONDS));
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            assertEquals(output, 0, process.exitValue());
            assertTrue(output, output.contains("launcher-ok"));
        } finally {
            process.destroyForcibly();
        }
    }

    @Test public void macNameIsSetBeforeAppMain() throws Exception { launch("Mac OS X", "Zettelkasten"); }
    @Test public void linuxNameIsUntouched() throws Exception { launch("Linux", "Existing"); }
    @Test public void windowsNameIsUntouched() throws Exception { launch("Windows 11", "Existing"); }
}
