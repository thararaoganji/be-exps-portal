package portal.expenses.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalStorageServiceTest {

    private LocalStorageService localStorageService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        localStorageService = new LocalStorageService();
        ReflectionTestUtils.setField(localStorageService, "uploadDir", tempDir.toAbsolutePath().toString());
    }

    @Test
    void testUploadAndDownloadFile_Success() throws IOException {
        String objectKey = "subfolder/test-file.txt";
        byte[] content = "Hello Local Storage".getBytes();

        localStorageService.uploadFile(objectKey, content);

        Path expectedPath = tempDir.resolve(objectKey);
        assertTrue(Files.exists(expectedPath));

        byte[] downloadedContent = localStorageService.downloadFile(objectKey);
        assertNotNull(downloadedContent);
        assertArrayEquals(content, downloadedContent);
    }

    @Test
    void testUploadFile_WhenUploadDirDoesNotExist() throws IOException {
        Path nonExistentBaseDir = tempDir.resolve("non_existent_folder");
        ReflectionTestUtils.setField(localStorageService, "uploadDir", nonExistentBaseDir.toAbsolutePath().toString());

        String objectKey = "another-file.txt";
        byte[] content = "Some content".getBytes();

        localStorageService.uploadFile(objectKey, content);

        Path expectedPath = nonExistentBaseDir.resolve(objectKey);
        assertTrue(Files.exists(expectedPath));
    }

    @Test
    void testDownloadFile_NotFound() {
        String objectKey = "non_existent.txt";

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                localStorageService.downloadFile(objectKey)
        );

        assertTrue(exception.getMessage().contains("File not found: " + objectKey));
    }

    @Test
    void testDownloadFile_IoException_DirectoryAsFile() throws IOException {
        String dirKey = "directory_key";
        Path dirPath = tempDir.resolve(dirKey);
        Files.createDirectories(dirPath);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                localStorageService.downloadFile(dirKey)
        );

        assertTrue(exception.getMessage().contains("Failed to download file: " + dirKey));
    }

    @Test
    void testDeleteFile_ExistingFile() throws IOException {
        String objectKey = "delete-me.txt";
        Path filePath = tempDir.resolve(objectKey);
        Files.write(filePath, "delete me".getBytes());
        assertTrue(Files.exists(filePath));

        localStorageService.deleteFile(objectKey);

        assertFalse(Files.exists(filePath));
    }

    @Test
    void testDeleteFile_NonExistingFile() {
        String objectKey = "does-not-exist.txt";
        // Should not throw any exception
        localStorageService.deleteFile(objectKey);
    }

    @Test
    void testDeleteFile_IoException_NonEmptyDirectory() throws IOException {
        String dirKey = "non_empty_dir";
        Path dirPath = tempDir.resolve(dirKey);
        Files.createDirectories(dirPath);
        Files.write(dirPath.resolve("child.txt"), "child content".getBytes());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                localStorageService.deleteFile(dirKey)
        );

        assertTrue(exception.getMessage().contains("Failed to delete file: " + dirKey));
    }
}