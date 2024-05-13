package playground;

import java.io.File;
import java.io.IOException;

public class BackupManager {

    public void createBackup(File originalFile) throws IOException {
        // Generate a new backup file name based on the original file's name
        String backupFileName = generateBackupFileName(originalFile);

        // Create a new backup file
        File backupFile = new File(backupFileName);

        // Perform the backup operation (e.g., copy the original file contents to the backup file)
        // This can be done using various file I/O techniques such as InputStream/OutputStream, Files.copy(), etc.
        // For simplicity, let's assume we're just creating an empty backup file for demonstration purposes
        boolean created = backupFile.createNewFile();
        if (created) {
            System.out.println("Backup file created: " + backupFileName);
        } else {
            System.err.println("Failed to create backup file: " + backupFileName);
        }
    }

    private String generateBackupFileName(File originalFile) {
        // Get the original file's name and path
        String originalFileName = originalFile.getName();
        String originalFilePath = originalFile.getParent();

        // Generate the backup file name by appending ".backup" to the original file name
        // You can add additional logic here to handle file name collisions or create numbered backups like ".backup-2", ".backup-3", etc.
        String backupFileName = originalFilePath + File.separator + originalFileName + ".backup";

        return backupFileName;
    }

    public static void main(String[] args) throws IOException {
        // Example usage: create a backup for a file named "example.txt"
        File originalFile = new File("example.txt");
        BackupManager backupManager = new BackupManager();
        backupManager.createBackup(originalFile);
    }
}
