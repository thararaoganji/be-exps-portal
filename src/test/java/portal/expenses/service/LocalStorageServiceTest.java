package portal.expenses.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

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
        ReflectionTestUtils.setField(localStorageService, "uploadDir", tempDir.toString());
    }

    @Test
    void testUploadFile_DirectFile() throws IOException {
        String objectKey = "test-file.txt";
        byte[] content = "Hello World".getBytes(StandardCharsets.UTF_8);

        localStorageService.uploadFile(objectKey, content);

        Path expectedPath = tempDir.resolve(objectKey);
        assertTrue(Files.exists(expectedPath));
        assertArrayEquals(content, Files.readAllBytes(expectedPath));
    }

    @Test
    void testUploadFile_WithNestedDirectories() throws IOException {
        String objectKey = "nested/folder/path/test-file.txt";
        byte[] content = "Nested Content".getBytes(StandardCharsets.UTF_8);

        localStorageService.uploadFile(objectKey, content);

        Path expectedPath = tempDir.resolve(objectKey);
        assertTrue(Files.exists(expectedPath));
        assertArrayEquals(content, Files.readAllBytes(expectedPath));
    }

    @Test
    void testUploadFile_WhenUploadDirDoesNotExistInitially() throws IOException {
        Path nonExistentBaseDir = tempDir.resolve("new_upload_base");
        ReflectionTestUtils.setField(localStorageService, "uploadDir", nonExistentBaseDir.toString());

        String objectKey = "sub/file.txt";
        byte[] content = "Base Dir Creation Test".getBytes(StandardCharsets.UTF_8);

        assertFalse(Files.exists(nonExistentBaseDir));
        localStorageService.uploadFile(objectKey, content);

        assertTrue(Files.exists(nonExistentBaseDir));
        assertTrue(Files.exists(nonExistentBaseDir.resolve(objectKey)));
    }

    @Test
    void testDownloadFile_Success() throws IOException {
        String objectKey = "download-test.txt";
        byte[] content = "Download Content".getBytes(StandardCharsets.UTF_8);
        Files.write(tempDir.resolve(objectKey), content);

        byte[] downloaded = localStorageService.downloadFile(objectKey);
        assertNotNull(downloaded);
        assertArrayEquals(content, downloaded);
    }

    @Test
    void testDownloadFile_NotFoundThrowsRuntimeException() {
        String objectKey = "non-existent-file.txt";

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                localStorageService.downloadFile(objectKey)
        );

        assertTrue(exception.getMessage().contains("File not found"));
    }

    @Test
    void testDownloadFile_DirectoryInsteadOfFileThrowsRuntimeException() throws IOException {
        String dirName = "test-directory";
        Files.createDirectory(tempDir.resolve(dirName));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                localStorageService.downloadFile(dirName)
        );

        assertTrue(exception.getMessage().contains("Failed to download file"));
    }

    @Test
    void testDeleteFile_ExistingFile() throws IOException {
        String objectKey = "file-to-delete.txt";
        Path filePath = tempDir.resolve(objectKey);
        Files.write(filePath, "To be deleted".getBytes(StandardCharsets.UTF_8));
        assertTrue(Files.exists(filePath));

        localStorageService.deleteFile(objectKey);

        assertFalse(Files.exists(filePath));
    }

    @Test
    void testDeleteFile_NonExistentFileDoesNotThrow() {
        String objectKey = "already-absent.txt";
        Path filePath = tempDir.resolve(objectKey);
        assertFalse(Files.exists(filePath));

        localStorageService.deleteFile(objectKey);
        assertFalse(Files.exists(filePath));
    }

    @Test
    void testDeleteFile_NonEmptyDirectoryThrowsRuntimeException() throws IOException {
        String dirName = "non-empty-dir";
        Path dirPath = tempDir.resolve(dirName);
        Files.createDirectory(dirPath);
        Files.write(dirPath.resolve("child.txt"), "child".getBytes(StandardCharsets.UTF_8));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                localStorageService.deleteFile(dirName)
        );

        assertTrue(exception.getMessage().contains("Failed to delete file"));
    }
}