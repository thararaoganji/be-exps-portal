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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private ResponseBytes<GetObjectResponse> responseBytes;

    private S3Service s3Service;

    private final String bucketName = "test-bucket";

    @BeforeEach
    void setUp() {
        s3Service = new S3Service(s3Client);
        ReflectionTestUtils.setField(s3Service, "bucketName", bucketName);
    }

    @Test
    void uploadFile_ShouldCallPutObject() throws IOException {
        String key = "test/file.txt";
        byte[] fileBytes = "hello world".getBytes();

        s3Service.uploadFile(key, fileBytes);

        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        ArgumentCaptor<RequestBody> bodyCaptor = ArgumentCaptor.forClass(RequestBody.class);

        verify(s3Client).putObject(requestCaptor.capture(), bodyCaptor.capture());

        PutObjectRequest capturedRequest = requestCaptor.getValue();
        assertEquals(bucketName, capturedRequest.bucket());
        assertEquals(key, capturedRequest.key());
        assertNotNull(bodyCaptor.getValue());
    }

    @Test
    void downloadFile_ShouldCallGetObjectAsBytesAndReturnByteArray() {
        String key = "test/file.txt";
        byte[] expectedBytes = "downloaded content".getBytes();

        when(s3Client.getObjectAsBytes(any(GetObjectRequest.class))).thenReturn(responseBytes);
        when(responseBytes.asByteArray()).thenReturn(expectedBytes);

        byte[] actualBytes = s3Service.downloadFile(key);

        ArgumentCaptor<GetObjectRequest> requestCaptor = ArgumentCaptor.forClass(GetObjectRequest.class);
        verify(s3Client).getObjectAsBytes(requestCaptor.capture());

        GetObjectRequest capturedRequest = requestCaptor.getValue();
        assertEquals(bucketName, capturedRequest.bucket());
        assertEquals(key, capturedRequest.key());
        assertArrayEquals(expectedBytes, actualBytes);
    }
}