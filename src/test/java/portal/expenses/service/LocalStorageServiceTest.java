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
        ReflectionTestUtils.setField(localStorageService, "uploadDir", tempDir.resolve("uploads").toString());
    }

    @Test
    void testUploadAndDownloadFile_Success() throws IOException {
        String objectKey = "test.txt";
        byte[] content = "Hello Local Storage".getBytes();

        localStorageService.uploadFile(objectKey, content);

        byte[] downloaded = localStorageService.downloadFile(objectKey);
        assertArrayEquals(content, downloaded);
    }

    @Test
    void testUploadFile_WithNestedDirectories() throws IOException {
        String objectKey = "subfolder/nested/test.txt";
        byte[] content = "Nested File Content".getBytes();

        localStorageService.uploadFile(objectKey, content);

        byte[] downloaded = localStorageService.downloadFile(objectKey);
        assertArrayEquals(content, downloaded);
    }

    @Test
    void testUploadFile_WhenDirectoryAlreadyExists() throws IOException {
        Path uploadDirPath = tempDir.resolve("uploads");
        Files.createDirectories(uploadDirPath);

        String objectKey = "existingDirFile.txt";
        byte[] content = "Content".getBytes();

        localStorageService.uploadFile(objectKey, content);

        byte[] downloaded = localStorageService.downloadFile(objectKey);
        assertArrayEquals(content, downloaded);
    }

    @Test
    void testDownloadFile_NotFound_ThrowsException() {
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                localStorageService.downloadFile("non_existent_file.txt"));
        assertTrue(exception.getMessage().contains("File not found"));
    }

    @Test
    void testDeleteFile_WhenFileExists() throws IOException {
        String objectKey = "to_delete.txt";
        byte[] content = "Delete me".getBytes();

        localStorageService.uploadFile(objectKey, content);
        assertNotNull(localStorageService.downloadFile(objectKey));

        localStorageService.deleteFile(objectKey);

        assertThrows(RuntimeException.class, () -> localStorageService.downloadFile(objectKey));
    }

    @Test
    void testDeleteFile_WhenFileDoesNotExist_DoesNotThrow() {
        assertDoesNotThrow(() -> localStorageService.deleteFile("non_existent_to_delete.txt"));
    }
}