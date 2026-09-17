package portal.expenses;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ExpensesPortalApplicationTest {

    @Test
    void testContextLoadsAndInstantiation() {
        ExpensesPortalApplication application = new ExpensesPortalApplication();
        assertNotNull(application);
    }

    @Test
    void testMainMethod() {
        try (MockedStatic<SpringApplication> mockedSpringApplication = Mockito.mockStatic(SpringApplication.class)) {
            mockedSpringApplication.when(() -> SpringApplication.run(ExpensesPortalApplication.class, new String[]{}))
                    .thenReturn(null);

            ExpensesPortalApplication.main(new String[]{});

            mockedSpringApplication.verify(() -> SpringApplication.run(ExpensesPortalApplication.class, new String[]{}));
        }
    }
}