package portal.expenses.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncoderUtil {

    private static final Logger logger = LoggerFactory.getLogger(PasswordEncoderUtil.class);

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // --- Generate hashes for your initial users ---
        String employeePassword = System.getenv("INITIAL_EMPLOYEE_PASSWORD");
        String managerPassword = System.getenv("INITIAL_MANAGER_PASSWORD");
        String financePassword = System.getenv("INITIAL_FINANCE_PASSWORD");

        String employeeHash = encoder.encode(employeePassword);
        String managerHash = encoder.encode(managerPassword);
        String financeHash = encoder.encode(financePassword);
        logger.info("Employee Password ('{}'): {}", employeePassword, employeeHash);
        logger.info("Manager Password ('{}'): {}", managerPassword, managerHash);
        logger.info("Finance Password ('{}'): {}", financePassword, financeHash);
    }
}
