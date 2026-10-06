package portal.expenses;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mockStatic;

class ExpensesPortalApplicationTest {

    @Test
    void testMainMethod() {
        try (MockedStatic<SpringApplication> mockedSpringApp = mockStatic(SpringApplication.class)) {
            mockedSpringApp.when(() -> SpringApplication.run(ExpensesPortalApplication.class, new String[]{}))
                    .thenReturn(null);

            ExpensesPortalApplication.main(new String[]{});

            mockedSpringApp.verify(() -> SpringApplication.run(ExpensesPortalApplication.class, new String[]{}));
        }
    }

    @Test
    void testConstructor() {
        ExpensesPortalApplication application = new ExpensesPortalApplication();
        assertNotNull(application);
    }
}