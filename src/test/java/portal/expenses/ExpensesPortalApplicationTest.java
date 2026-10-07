package portal.expenses;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class ExpensesPortalApplicationTest {

    @Test
    void testConstructor() {
        ExpensesPortalApplication app = new ExpensesPortalApplication();
        assertNotNull(app);
    }

    @Test
    void testMain() {
        try (MockedStatic<SpringApplication> mockedSpringApp = mockStatic(SpringApplication.class)) {
            ConfigurableApplicationContext contextMock = mock(ConfigurableApplicationContext.class);
            mockedSpringApp.when(() -> SpringApplication.run(eq(ExpensesPortalApplication.class), any(String[].class)))
                    .thenReturn(contextMock);

            ExpensesPortalApplication.main(new String[]{});

            mockedSpringApp.verify(() -> SpringApplication.run(ExpensesPortalApplication.class, new String[]{}));
        }
    }
}