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
                String encodedEmployeePassword = encoder.encode(employeePassword);
                logger.info("Employee encoded password: {}", encodedEmployeePassword);
            }
            if (managerPassword != null) {
                String encodedManagerPassword = encoder.encode(managerPassword);
                logger.info("Manager encoded password: {}", encodedManagerPassword);
            }
            if (financePassword != null) {
                String encodedFinancePassword = encoder.encode(financePassword);
                logger.info("Finance encoded password: {}", encodedFinancePassword);
            }
        }
    }
}