package portal.expenses.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncoderUtil {

    private static final Logger logger = LoggerFactory.getLogger(PasswordEncoderUtil.class);

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // --- Generate hashes for your initial users ---
        String employeePassword = "password-employee";
        String managerPassword = "password-manager";
        String financePassword = "password-finance";

        if (logger.isInfoEnabled()) {
            String encodedEmployee = encoder.encode(employeePassword);
            logger.info("Employee Password ('{}'): {}", employeePassword, encodedEmployee);
            String encodedManager = encoder.encode(managerPassword);
            logger.info("Manager Password ('{}'): {}", managerPassword, encodedManager);
            String encodedFinance = encoder.encode(financePassword);
            logger.info("Finance Password ('{}'): {}", financePassword, encodedFinance);
        }
    }
}
