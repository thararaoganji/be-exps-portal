package portal.expenses.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @Mock
    private S3Client s3Client;

    private S3Service s3Service;

    @BeforeEach
    void setUp() {
        s3Service = new S3Service(s3Client);
        ReflectionTestUtils.setField(s3Service, "bucketName", "my-test-bucket");
    }

    @Test
    void uploadFile_successfullyUploads() throws IOException {
        byte[] content = "test content".getBytes(StandardCharsets.UTF_8);
        String key = "receipts/receipt-123.pdf";

        s3Service.uploadFile(key, content);

        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        ArgumentCaptor<RequestBody> bodyCaptor = ArgumentCaptor.forClass(RequestBody.class);

        verify(s3Client).putObject(requestCaptor.capture(), bodyCaptor.capture());

        PutObjectRequest capturedRequest = requestCaptor.getValue();
        assertEquals("my-test-bucket", capturedRequest.bucket());
        assertEquals(key, capturedRequest.key());
    }

    @Test
    @SuppressWarnings("unchecked")
    void downloadFile_successfullyDownloads() {
        String key = "receipts/receipt-123.pdf";
        byte[] expectedBytes = "downloaded content".getBytes(StandardCharsets.UTF_8);

        ResponseBytes<GetObjectResponse> responseBytesMock = mock(ResponseBytes.class);
        when(responseBytesMock.asByteArray()).thenReturn(expectedBytes);
        when(s3Client.getObjectAsBytes(any(GetObjectRequest.class))).thenReturn(responseBytesMock);

        byte[] actualBytes = s3Service.downloadFile(key);

        assertArrayEquals(expectedBytes, actualBytes);

        ArgumentCaptor<GetObjectRequest> requestCaptor = ArgumentCaptor.forClass(GetObjectRequest.class);
        verify(s3Client).getObjectAsBytes(requestCaptor.capture());

        GetObjectRequest capturedRequest = requestCaptor.getValue();
        assertEquals("my-test-bucket", capturedRequest.bucket());
        assertEquals(key, capturedRequest.key());
    }
}