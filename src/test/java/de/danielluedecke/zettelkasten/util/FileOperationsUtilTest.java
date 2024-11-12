package de.danielluedecke.zettelkasten.util;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

public class FileOperationsUtilTest {

    @TempDir
    File tempDir;

    private File createTestZipFile(String filename, String xmlContent) throws IOException {
        File zipFile = new File(tempDir, "test.zip");
        try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFile))) {
            ZipEntry entry = new ZipEntry(filename);
            zipOut.putNextEntry(entry);
            zipOut.write(xmlContent.getBytes());
            zipOut.closeEntry();
        }
        return zipFile;
    }

    @Test
    public void testReadXMLFileFromZipFile_Success() throws Exception {
        // Arrange
        String filename = "test.xml";
        String xmlContent = "<root><child>Test</child></root>";
        File zipFile = createTestZipFile(filename, xmlContent);

        // Act
        Document document = FileOperationsUtil.readXMLFileFromZipFile(zipFile, filename);

        // Assert
        assertNotNull(document, "Document should not be null");
        assertEquals("root", document.getRootElement().getName(), "Root element should be 'root'");
    }

    @Test
    public void testReadXMLFileFromZipFile_FileNotFound() {
        // Arrange
        String missingFilename = "missing.xml";
        File zipFile = new File(tempDir, "test.zip");

        // Act & Assert
        Exception exception = assertThrows(FileNotFoundException.class, () ->
                FileOperationsUtil.readXMLFileFromZipFile(zipFile, missingFilename)
        );

        assertEquals("File missing.xml not found in zip archive " + zipFile.getPath(), exception.getMessage());
    }

    @Test
    public void testReadXMLFileFromZipFile_InvalidXMLContent() throws IOException {
        // Arrange
        String filename = "test.xml";
        String invalidXmlContent = "<root><child>Test"; // Unclosed tag
        File zipFile = createTestZipFile(filename, invalidXmlContent);

        // Act & Assert
        assertThrows(JDOMException.class, () ->
                FileOperationsUtil.readXMLFileFromZipFile(zipFile, filename)
        );
    }

    @Test
    public void testReadXMLFileFromZipFile_EmptyZipFile() throws IOException {
        // Arrange
        File emptyZipFile = new File(tempDir, "empty.zip");
        try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(emptyZipFile))) {
            // Creating an empty zip
        }

        // Act & Assert
        Exception exception = assertThrows(FileNotFoundException.class, () ->
                FileOperationsUtil.readXMLFileFromZipFile(emptyZipFile, "test.xml")
        );

        assertTrue(exception.getMessage().contains("not found in zip archive"), "Expected file not found message");
    }

    @Test
    public void testReadXMLFileFromZipFile_NonExistentZipFile() {
        // Arrange
        File nonExistentZipFile = new File(tempDir, "nonexistent.zip");

        // Act & Assert
        assertThrows(IOException.class, () ->
                FileOperationsUtil.readXMLFileFromZipFile(nonExistentZipFile, "test.xml")
        );
    }
}

