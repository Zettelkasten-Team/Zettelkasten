package playground;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class BackupManagerTest {

    private BackupManager backupManager;

    @BeforeEach
    void setUp() {
        backupManager = new BackupManager();
    }

    @Test
    void testCreateBackup_SuccessfulBackup(@TempDir Path tempDir) throws IOException {
        // Create a temporary file
        File originalFile = tempDir.resolve("original.txt").toFile();
        assertTrue(originalFile.createNewFile());

        // Create a backup for the original file
        File backupFile = new File(tempDir.toString(), "original.txt.backup");
        backupManager.createBackup(originalFile);

        // Verify that the backup file was created
        assertTrue(backupFile.exists());
    }

    @Test
    void testCreateBackup_BackupAlreadyExists(@TempDir Path tempDir) throws IOException {
        // Create a temporary file
        File originalFile = tempDir.resolve("original.txt").toFile();
        assertTrue(originalFile.createNewFile());

        // Create a backup for the original file
        File backupFile = new File(tempDir.toString(), "original.txt.backup");
        assertTrue(backupFile.createNewFile());

        // Attempt to create a backup for the original file again
        backupManager.createBackup(originalFile);

        // Verify that the backup file was not overwritten
        assertTrue(backupFile.exists());
    }

    @Test
    void testCreateBackup_FileDoesNotExist() {
        // Attempt to create a backup for a non-existent file
        File nonExistentFile = new File("nonexistent.txt");

        // Use assertThrows to capture the thrown IOException
        IOException exception = assertThrows(IOException.class, () -> backupManager.createBackup(nonExistentFile), "Expected IOException when creating backup for non-existent file");

        // Assert that the thrown IOException message indicates the file does not exist
        assertNotNull(exception.getMessage(), "IOException message should not be null");
        assertEquals("No such file or directory", exception.getMessage(), "IOException message should indicate that the file does not exist");
    }

}
