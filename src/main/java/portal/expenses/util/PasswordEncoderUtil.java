package portal.expenses.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncoderUtil {

    private static final Logger logger = LoggerFactory.getLogger(PasswordEncoderUtil.class);

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // --- Generate hashes for your initial users ---
        String employeePassword = System.getenv("EMPLOYEE_PASSWORD");
        String managerPassword = System.getenv("MANAGER_PASSWORD");
        String financePassword = System.getenv("FINANCE_PASSWORD");

        if (logger.isInfoEnabled()) {
            if (employeePassword != null) {
                logger.info("Employee Password ('{}'): {}", employeePassword, encoder.encode(employeePassword));
            }
            if (managerPassword != null) {
                logger.info("Manager Password ('{}'): {}", managerPassword, encoder.encode(managerPassword));
            }
            if (financePassword != null) {
                logger.info("Finance Password ('{}'): {}", financePassword, encoder.encode(financePassword));
            }
        }
    }
}
