package portal.expenses.config;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.S3Client;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class AwsConfigTest {

    @Test
    void testS3ClientCreation() {
        AwsConfig awsConfig = new AwsConfig();
        try (S3Client s3Client = awsConfig.s3Client("us-east-1")) {
            assertNotNull(s3Client);
        }
    }
}