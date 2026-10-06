package portal.expenses.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class LocalStorageServiceTest {

    private LocalStorageService localStorageService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        localStorageService = new LocalStorageService();
        ReflectionTestUtils.setField(localStorageService, "uploadDir", tempDir.toString());
    }

    @Test
    void uploadFile_createsDirectoriesAndWritesFile() throws IOException {
        String objectKey = "subfolder/test.txt";
        byte[] content = "Hello World".getBytes();

        localStorageService.uploadFile(objectKey, content);

        Path expectedFile = tempDir.resolve(objectKey);
        assertTrue(Files.exists(expectedFile));
        assertArrayEquals(content, Files.readAllBytes(expectedFile));
    }

    @Test
    void uploadFile_whenUploadDirDoesNotExist_createsDir() throws IOException {
        Path newUploadDir = tempDir.resolve("nested/dir");
        ReflectionTestUtils.setField(localStorageService, "uploadDir", newUploadDir.toString());

        String objectKey = "file.txt";
        byte[] content = "Content".getBytes();

        localStorageService.uploadFile(objectKey, content);

        Path expectedFile = newUploadDir.resolve(objectKey);
        assertTrue(Files.exists(expectedFile));
        assertArrayEquals(content, Files.readAllBytes(expectedFile));
    }

    @Test
    void downloadFile_existingFile_returnsBytes() throws IOException {
        String objectKey = "test-download.txt";
        byte[] content = "Sample bytes".getBytes();
        Path filePath = tempDir.resolve(objectKey);
        Files.write(filePath, content);

        byte[] downloaded = localStorageService.downloadFile(objectKey);

        assertArrayEquals(content, downloaded);
    }

    @Test
    void downloadFile_fileNotFound_throwsRuntimeException() {
        String objectKey = "non-existent.txt";

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                localStorageService.downloadFile(objectKey)
        );
        assertTrue(exception.getMessage().contains("File not found"));
    }

    @Test
    void deleteFile_existingFile_deletesFile() throws IOException {
        String objectKey = "to-delete.txt";
        Path filePath = tempDir.resolve(objectKey);
        Files.write(filePath, "delete me".getBytes());
        assertTrue(Files.exists(filePath));

        localStorageService.deleteFile(objectKey);

        assertFalse(Files.exists(filePath));
    }

    @Test
    void deleteFile_nonExistingFile_doesNotThrow() {
        String objectKey = "does-not-exist.txt";
        assertDoesNotThrow(() -> localStorageService.deleteFile(objectKey));
    }
}