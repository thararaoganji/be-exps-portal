package portal.expenses;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ExpensesPortalApplicationTest {

	@Test
	void testApplicationInstantiation() {
		ExpensesPortalApplication application = new ExpensesPortalApplication();
		assertNotNull(application);
	}

	@Test
	void testMainMethod() {
		try (MockedStatic<SpringApplication> mockedSpringApplication = Mockito.mockStatic(SpringApplication.class)) {
			ConfigurableApplicationContext mockContext = Mockito.mock(ConfigurableApplicationContext.class);
			mockedSpringApplication.when(() -> SpringApplication.run(ExpensesPortalApplication.class, new String[]{}))
					.thenReturn(mockContext);

			ExpensesPortalApplication.main(new String[]{});

			mockedSpringApplication.verify(() -> SpringApplication.run(ExpensesPortalApplication.class, new String[]{}));
		}
	}

}