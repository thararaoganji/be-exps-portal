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
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalStorageServiceTest {

    @TempDir
    Path tempDir;

    private LocalStorageService localStorageService;
    private Path uploadDirPath;

    @BeforeEach
    void setUp() {
        localStorageService = new LocalStorageService();
        uploadDirPath = tempDir.resolve("uploads");
        ReflectionTestUtils.setField(localStorageService, "uploadDir", uploadDirPath.toString());
    }

    @Test
    void uploadFile_createsUploadDirAndSubDirsAndWritesFile() throws IOException {
        String objectKey = "subfolder/nested/test-file.txt";
        byte[] content = "Hello World".getBytes(StandardCharsets.UTF_8);

        assertFalse(Files.exists(uploadDirPath));

        localStorageService.uploadFile(objectKey, content);

        Path targetFile = uploadDirPath.resolve(objectKey);
        assertTrue(Files.exists(targetFile));
        assertArrayEquals(content, Files.readAllBytes(targetFile));

        // Upload again to test when uploadDir and parentDir already exist
        byte[] newContent = "Updated World".getBytes(StandardCharsets.UTF_8);
        localStorageService.uploadFile(objectKey, newContent);
        assertArrayEquals(newContent, Files.readAllBytes(targetFile));
    }

    @Test
    void uploadFile_inRootDirWithoutParentSubfolder() throws IOException {
        String objectKey = "direct-file.txt";
        byte[] content = "Direct file content".getBytes(StandardCharsets.UTF_8);

        localStorageService.uploadFile(objectKey, content);

        Path targetFile = uploadDirPath.resolve(objectKey);
        assertTrue(Files.exists(targetFile));
        assertArrayEquals(content, Files.readAllBytes(targetFile));
    }

    @Test
    void downloadFile_success() throws IOException {
        String objectKey = "download-test.txt";
        byte[] content = "Downloadable Content".getBytes(StandardCharsets.UTF_8);

        localStorageService.uploadFile(objectKey, content);

        byte[] downloaded = localStorageService.downloadFile(objectKey);
        assertNotNull(downloaded);
        assertArrayEquals(content, downloaded);
    }

    @Test
    void downloadFile_fileNotFound_throwsRuntimeException() {
        String objectKey = "non-existent.txt";

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                localStorageService.downloadFile(objectKey)
        );
        assertTrue(exception.getMessage().contains("File not found: " + objectKey));
    }

    @Test
    void downloadFile_ioException_wrappedInRuntimeException() throws IOException {
        // Create upload dir first
        Files.createDirectories(uploadDirPath);
        // Create a directory where the file is expected, so readAllBytes fails with an IOException
        String objectKey = "directoryAsFile";
        Files.createDirectories(uploadDirPath.resolve(objectKey));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                localStorageService.downloadFile(objectKey)
        );
        assertTrue(exception.getMessage().contains("Failed to download file: " + objectKey));
        assertNotNull(exception.getCause());
    }

    @Test
    void deleteFile_success() throws IOException {
        String objectKey = "file-to-delete.txt";
        byte[] content = "to be deleted".getBytes(StandardCharsets.UTF_8);

        localStorageService.uploadFile(objectKey, content);
        Path targetFile = uploadDirPath.resolve(objectKey);
        assertTrue(Files.exists(targetFile));

        localStorageService.deleteFile(objectKey);
        assertFalse(Files.exists(targetFile));
    }

    @Test
    void deleteFile_fileDoesNotExist_noop() {
        String objectKey = "already-absent.txt";
        // Should not throw an exception when the file does not exist
        assertDoesNotThrow(() -> localStorageService.deleteFile(objectKey));
        assertFalse(Files.exists(uploadDirPath.resolve(objectKey)));
    }

    @Test
    void deleteFile_ioException_wrappedInRuntimeException() throws IOException {
        // Create upload dir and a non-empty directory at the objectKey path
        // Files.delete on a non-empty directory throws DirectoryNotEmptyException (IOException)
        Path dirPath = uploadDirPath.resolve("nonEmptyDir");
        Files.createDirectories(dirPath);
        Files.write(dirPath.resolve("child.txt"), "child".getBytes(StandardCharsets.UTF_8));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                localStorageService.deleteFile("nonEmptyDir")
        );
        assertTrue(exception.getMessage().contains("Failed to delete file: nonEmptyDir"));
        assertNotNull(exception.getCause());
    }
}