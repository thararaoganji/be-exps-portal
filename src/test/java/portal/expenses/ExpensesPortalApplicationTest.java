package portal.expenses;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class ExpensesPortalApplicationTest {

    @Test
    void testConstructor() {
        ExpensesPortalApplication application = new ExpensesPortalApplication();
        assertNotNull(application);
    }

    @Test
    void testMain() {
        try (MockedStatic<SpringApplication> mockedSpringApp = mockStatic(SpringApplication.class)) {
            mockedSpringApp.when(() -> SpringApplication.run(ExpensesPortalApplication.class, new String[]{}))
                    .thenReturn(mock(ConfigurableApplicationContext.class));

            ExpensesPortalApplication.main(new String[]{});

            mockedSpringApp.verify(() -> SpringApplication.run(ExpensesPortalApplication.class, new String[]{}));
        }
    }
}