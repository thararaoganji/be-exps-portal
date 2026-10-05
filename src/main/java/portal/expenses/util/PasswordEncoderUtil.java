package portal.expenses.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncoderUtil {

    private static final Logger logger = LoggerFactory.getLogger(PasswordEncoderUtil.class);

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // --- Generate hashes for your initial users ---
        String employeePassword = args.length > 0 ? args[0] : System.getenv("EMPLOYEE_PASSWORD");
        String managerPassword = args.length > 1 ? args[1] : System.getenv("MANAGER_PASSWORD");
        String financePassword = args.length > 2 ? args[2] : System.getenv("FINANCE_PASSWORD");

        if (logger.isInfoEnabled()) {
            if (employeePassword != null) {
                String encodedEmployee = encoder.encode(employeePassword);
                logger.info("Employee hash: {}", encodedEmployee);
            }
            if (managerPassword != null) {
                String encodedManager = encoder.encode(managerPassword);
                logger.info("Manager hash: {}", encodedManager);
            }
            if (financePassword != null) {
                String encodedFinance = encoder.encode(financePassword);
                logger.info("Finance hash: {}", encodedFinance);
            }
        }
    }
}
