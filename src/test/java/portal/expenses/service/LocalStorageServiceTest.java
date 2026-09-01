package portal.expenses.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalStorageServiceTest {

    @TempDir
    Path tempDir;

    private LocalStorageService localStorageService;

    @BeforeEach
    void setUp() {
        localStorageService = new LocalStorageService();
        ReflectionTestUtils.setField(localStorageService, "uploadDir", tempDir.resolve("uploads").toString());
    }

    @Test
    void testUploadFile_createsDirectoryAndFile() throws IOException {
        String objectKey = "test.txt";
        byte[] content = "Hello World".getBytes();

        localStorageService.uploadFile(objectKey, content);

        Path expectedFilePath = tempDir.resolve("uploads").resolve(objectKey);
        assertTrue(Files.exists(expectedFilePath));
        assertArrayEquals(content, Files.readAllBytes(expectedFilePath));
    }

    @Test
    void testUploadFile_nestedPath_createsParentDirectories() throws IOException {
        String objectKey = "sub/dir/nested_test.txt";
        byte[] content = "Nested Content".getBytes();

        localStorageService.uploadFile(objectKey, content);

        Path expectedFilePath = tempDir.resolve("uploads").resolve(objectKey);
        assertTrue(Files.exists(expectedFilePath));
        assertArrayEquals(content, Files.readAllBytes(expectedFilePath));
    }

    @Test
    void testUploadFile_whenDirectoryAlreadyExists() throws IOException {
        Path uploadDirPath = tempDir.resolve("uploads");
        Files.createDirectories(uploadDirPath);

        String objectKey = "existing_dir_file.txt";
        byte[] content = "Existing dir file".getBytes();

        localStorageService.uploadFile(objectKey, content);

        Path expectedFilePath = uploadDirPath.resolve(objectKey);
        assertTrue(Files.exists(expectedFilePath));
        assertArrayEquals(content, Files.readAllBytes(expectedFilePath));
    }

    @Test
    void testDownloadFile_success() throws IOException {
        String objectKey = "download_me.txt";
        byte[] content = "Download content".getBytes();

        localStorageService.uploadFile(objectKey, content);

        byte[] downloaded = localStorageService.downloadFile(objectKey);
        assertNotNull(downloaded);
        assertArrayEquals(content, downloaded);
    }

    @Test
    void testDownloadFile_notFound_throwsRuntimeException() {
        String objectKey = "non_existent.txt";

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            localStorageService.downloadFile(objectKey);
        });

        assertTrue(exception.getMessage().contains("File not found: " + objectKey));
    }

    @Test
    void testDownloadFile_directoryAsFile_throwsRuntimeException() throws IOException {
        String objectKey = "some_dir";
        Path dirPath = tempDir.resolve("uploads").resolve(objectKey);
        Files.createDirectories(dirPath);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            localStorageService.downloadFile(objectKey);
        });

        assertTrue(exception.getMessage().contains("Failed to download file: " + objectKey));
    }

    @Test
    void testDeleteFile_success() throws IOException {
        String objectKey = "delete_me.txt";
        byte[] content = "To be deleted".getBytes();

        localStorageService.uploadFile(objectKey, content);
        Path filePath = tempDir.resolve("uploads").resolve(objectKey);
        assertTrue(Files.exists(filePath));

        localStorageService.deleteFile(objectKey);
        assertFalse(Files.exists(filePath));
    }

    @Test
    void testDeleteFile_fileDoesNotExist_doesNothing() {
        String objectKey = "never_created.txt";
        localStorageService.deleteFile(objectKey);
        Path filePath = tempDir.resolve("uploads").resolve(objectKey);
        assertFalse(Files.exists(filePath));
    }

    @Test
    void testDeleteFile_nonEmptyDirectory_throwsRuntimeException() throws IOException {
        String objectKey = "non_empty_dir";
        Path dirPath = tempDir.resolve("uploads").resolve(objectKey);
        Files.createDirectories(dirPath);
        Files.write(dirPath.resolve("child.txt"), "child".getBytes());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            localStorageService.deleteFile(objectKey);
        });

        assertTrue(exception.getMessage().contains("Failed to delete file: " + objectKey));
    }
}