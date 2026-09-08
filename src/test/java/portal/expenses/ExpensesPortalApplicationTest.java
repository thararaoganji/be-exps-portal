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
	void contextLoads() {
		ExpensesPortalApplication application = new ExpensesPortalApplication();
		assertNotNull(application);
	}

	@Test
	void mainMethodRunsSpringApplication() {
		try (MockedStatic<SpringApplication> mockedSpringApplication = mockStatic(SpringApplication.class)) {
			ConfigurableApplicationContext context = mock(ConfigurableApplicationContext.class);
			mockedSpringApplication.when(() -> SpringApplication.run(ExpensesPortalApplication.class, new String[]{}))
					.thenReturn(context);

			ExpensesPortalApplication.main(new String[]{});

			mockedSpringApplication.verify(() -> SpringApplication.run(ExpensesPortalApplication.class, new String[]{}));
		}
	}

}