package portal.expenses.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PasswordEncoderUtilTest {

    @Test
    void testMainMethodExecutesSuccessfully() {
        assertDoesNotThrow(() -> PasswordEncoderUtil.main(new String[]{}));
    }

    @Test
    void testDefaultConstructor() {
        PasswordEncoderUtil util = new PasswordEncoderUtil();
        assertNotNull(util);
    }
}