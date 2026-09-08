package portal.expenses;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class ExpensesPortalApplicationTest {

	@Test
	void contextLoads() {
		ExpensesPortalApplication application = new ExpensesPortalApplication();
		assertNotNull(application);
	}

	@Test
	void mainMethodRunsSuccessfully() {
		try (MockedStatic<SpringApplication> mockedSpringApplication = Mockito.mockStatic(SpringApplication.class)) {
			mockedSpringApplication.when(() -> SpringApplication.run(eq(ExpensesPortalApplication.class), any(String[].class)))
					.thenReturn(Mockito.mock(ConfigurableApplicationContext.class));

			assertDoesNotThrow(() -> ExpensesPortalApplication.main(new String[]{}));
			mockedSpringApplication.verify(() -> SpringApplication.run(eq(ExpensesPortalApplication.class), any(String[].class)));
		}
	}
}