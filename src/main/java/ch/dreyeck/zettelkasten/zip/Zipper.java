package ch.dreyeck.zettelkasten.zip;

import java.io.IOException;
import java.io.PrintStream;
import java.util.zip.ZipFile;

public class Zipper {
    public static void main(String[] args) throws IOException {
        Zipper zipper = new Zipper();
        PrintStream printStream = new PrintStream(System.out);
        zipper.printEntries(printStream, "#330-manlinks.zkn3");
    }

    public void printEntries(PrintStream stream, String zip) throws IOException {
        try (ZipFile zipFile = new ZipFile(zip)) {
            zipFile.stream()
                    .forEach(stream::println);
        }
    }
}