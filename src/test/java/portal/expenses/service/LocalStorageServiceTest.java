package portal.expenses.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
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
    void testUploadFile_Success() throws IOException {
        String objectKey = "subfolder/test-file.txt";
        byte[] content = "Hello World".getBytes();

        localStorageService.uploadFile(objectKey, content);

        byte[] downloaded = localStorageService.downloadFile(objectKey);
        assertArrayEquals(content, downloaded);
    }

    @Test
    void testUploadFile_DirectFileInUploadDir() throws IOException {
        String objectKey = "root-file.txt";
        byte[] content = "Root Content".getBytes();

        localStorageService.uploadFile(objectKey, content);

        byte[] downloaded = localStorageService.downloadFile(objectKey);
        assertArrayEquals(content, downloaded);
    }

    @Test
    void testDownloadFile_NotFound() {
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                localStorageService.downloadFile("non-existent-file.txt")
        );
        assertTrue(exception.getMessage().contains("File not found"));
    }

    @Test
    void testDeleteFile_ExistingFile() throws IOException {
        String objectKey = "file-to-delete.txt";
        byte[] content = "To be deleted".getBytes();

        localStorageService.uploadFile(objectKey, content);
        assertNotNull(localStorageService.downloadFile(objectKey));

        localStorageService.deleteFile(objectKey);

        assertThrows(RuntimeException.class, () -> localStorageService.downloadFile(objectKey));
    }

    @Test
    void testDeleteFile_NonExistentFile() {
        assertDoesNotThrow(() -> localStorageService.deleteFile("non-existent-file.txt"));
    }

    @Test
    void testUploadFile_WhenUploadDirDoesNotExist() throws IOException {
        Path subTempDir = tempDir.resolve("new-upload-dir");
        ReflectionTestUtils.setField(localStorageService, "uploadDir", subTempDir.toString());

        String objectKey = "nested/dir/test.txt";
        byte[] content = "Nested data".getBytes();

        localStorageService.uploadFile(objectKey, content);

        byte[] downloaded = localStorageService.downloadFile(objectKey);
        assertArrayEquals(content, downloaded);
    }

    @Test
    void testDownloadFile_IOExceptionHandling() {
        // If uploadDir points to an invalid path or null
        LocalStorageService badService = new LocalStorageService();
        ReflectionTestUtils.setField(badService, "uploadDir", "\0invalid-path");

        assertThrows(RuntimeException.class, () -> badService.downloadFile("any.txt"));
    }

    @Test
    void testDeleteFile_IOExceptionHandling() {
        LocalStorageService badService = new LocalStorageService();
        ReflectionTestUtils.setField(badService, "uploadDir", "\0invalid-path");

        assertThrows(RuntimeException.class, () -> badService.deleteFile("any.txt"));
    }
}