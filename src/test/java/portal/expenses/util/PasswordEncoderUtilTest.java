package portal.expenses.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PasswordEncoderUtilTest {

    @Test
    void testConstructor() {
        PasswordEncoderUtil util = new PasswordEncoderUtil();
        assertNotNull(util);
    }

    @Test
    void testMainMethod() {
        assertDoesNotThrow(() -> PasswordEncoderUtil.main(new String[]{}));
    }
}