package portal.expenses.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void testUploadAndDownloadFileSuccess() throws IOException {
        String objectKey = "subfolder/test-file.txt";
        byte[] content = "Hello Local Storage".getBytes(StandardCharsets.UTF_8);

        localStorageService.uploadFile(objectKey, content);

        Path uploadedPath = tempDir.resolve("uploads").resolve(objectKey);
        assertTrue(Files.exists(uploadedPath));

        byte[] downloaded = localStorageService.downloadFile(objectKey);
        assertArrayEquals(content, downloaded);
    }

    @Test
    void testUploadFileWhenDirAlreadyExists() throws IOException {
        String objectKey = "direct-file.txt";
        byte[] content = "Direct file content".getBytes(StandardCharsets.UTF_8);

        // Upload first file creating uploadDir
        localStorageService.uploadFile(objectKey, content);

        // Upload second file when uploadDir and parent dir already exist
        String secondKey = "second-file.txt";
        byte[] secondContent = "Second file content".getBytes(StandardCharsets.UTF_8);
        localStorageService.uploadFile(secondKey, secondContent);

        byte[] downloaded = localStorageService.downloadFile(secondKey);
        assertArrayEquals(secondContent, downloaded);
    }

    @Test
    void testDownloadFileNotFound() {
        String nonExistentKey = "non-existent.txt";

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                localStorageService.downloadFile(nonExistentKey));

        assertTrue(exception.getMessage().contains("File not found: " + nonExistentKey));
    }

    @Test
    void testDeleteFileExisting() throws IOException {
        String objectKey = "to-delete.txt";
        byte[] content = "Delete me".getBytes(StandardCharsets.UTF_8);

        localStorageService.uploadFile(objectKey, content);
        Path filePath = tempDir.resolve("uploads").resolve(objectKey);
        assertTrue(Files.exists(filePath));

        localStorageService.deleteFile(objectKey);
        assertFalse(Files.exists(filePath));
    }

    @Test
    void testDeleteFileNonExisting() {
        // Deleting non-existing file should simply do nothing without throwing exception
        localStorageService.deleteFile("non-existing-file.txt");
    }

    @Test
    void testDownloadFileIOExceptionHandling() throws IOException {
        // Create a directory where a file is expected so reading bytes fails with IOException
        Path uploadDirPath = tempDir.resolve("uploads");
        Files.createDirectories(uploadDirPath);
        Path subDir = uploadDirPath.resolve("dir-as-file");
        Files.createDirectories(subDir);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                localStorageService.downloadFile("dir-as-file"));

        assertTrue(exception.getMessage().contains("Failed to download file: dir-as-file"));
        assertNotNull(exception.getCause());
    }

    @Test
    void testDeleteFileIOExceptionHandling() throws IOException {
        // Create a non-empty directory at the path to delete, so Files.delete(path) throws DirectoryNotEmptyException (which is an IOException)
        Path uploadDirPath = tempDir.resolve("uploads");
        Path subDir = uploadDirPath.resolve("non-empty-dir");
        Files.createDirectories(subDir);
        Files.write(subDir.resolve("child.txt"), "data".getBytes(StandardCharsets.UTF_8));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                localStorageService.deleteFile("non-empty-dir"));

        assertTrue(exception.getMessage().contains("Failed to delete file: non-empty-dir"));
        assertNotNull(exception.getCause());
    }
}