package portal.expenses.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
        ReflectionTestUtils.setField(localStorageService, "uploadDir", tempDir.resolve("uploads").toString());
    }

    @Test
    void testUploadFile_Success() throws IOException {
        String objectKey = "test-file.txt";
        byte[] content = "Hello World".getBytes();

        localStorageService.uploadFile(objectKey, content);

        Path uploadedFilePath = tempDir.resolve("uploads").resolve(objectKey);
        assertTrue(Files.exists(uploadedFilePath));
        assertArrayEquals(content, Files.readAllBytes(uploadedFilePath));
    }

    @Test
    void testUploadFile_WithNestedDirectories() throws IOException {
        String objectKey = "sub/dir/nested-file.txt";
        byte[] content = "Nested Content".getBytes();

        localStorageService.uploadFile(objectKey, content);

        Path uploadedFilePath = tempDir.resolve("uploads").resolve(objectKey);
        assertTrue(Files.exists(uploadedFilePath));
        assertArrayEquals(content, Files.readAllBytes(uploadedFilePath));
    }

    @Test
    void testUploadFile_WhenDirectoryAlreadyExists() throws IOException {
        Path uploadPath = tempDir.resolve("uploads");
        Files.createDirectories(uploadPath);

        String objectKey = "file2.txt";
        byte[] content = "Content 2".getBytes();

        localStorageService.uploadFile(objectKey, content);

        Path uploadedFilePath = uploadPath.resolve(objectKey);
        assertTrue(Files.exists(uploadedFilePath));
        assertArrayEquals(content, Files.readAllBytes(uploadedFilePath));
    }

    @Test
    void testDownloadFile_Success() throws IOException {
        String objectKey = "download-test.txt";
        byte[] content = "Sample Data".getBytes();

        Path uploadPath = tempDir.resolve("uploads");
        Files.createDirectories(uploadPath);
        Files.write(uploadPath.resolve(objectKey), content);

        byte[] downloadedBytes = localStorageService.downloadFile(objectKey);

        assertNotNull(downloadedBytes);
        assertArrayEquals(content, downloadedBytes);
    }

    @Test
    void testDownloadFile_FileNotFound() {
        String objectKey = "non-existent.txt";

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                localStorageService.downloadFile(objectKey)
        );

        assertTrue(exception.getMessage().contains("File not found"));
    }

    @Test
    void testDeleteFile_ExistingFile() throws IOException {
        String objectKey = "to-delete.txt";
        Path uploadPath = tempDir.resolve("uploads");
        Files.createDirectories(uploadPath);
        Path filePath = uploadPath.resolve(objectKey);
        Files.write(filePath, "data".getBytes());

        assertTrue(Files.exists(filePath));

        localStorageService.deleteFile(objectKey);

        assertFalse(Files.exists(filePath));
    }

    @Test
    void testDeleteFile_NonExistentFile() {
        String objectKey = "already-deleted.txt";

        assertDoesNotThrow(() -> localStorageService.deleteFile(objectKey));
    }

    @Test
    void testDeleteFile_ExceptionHandling() throws IOException {
        // Create a non-empty directory at the objectKey path so Files.delete throws IOException (DirectoryNotEmptyException)
        Path uploadPath = tempDir.resolve("uploads");
        Files.createDirectories(uploadPath.resolve("dir-to-delete/inner-dir"));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                localStorageService.deleteFile("dir-to-delete")
        );

        assertTrue(exception.getMessage().contains("Failed to delete file"));
    }
}