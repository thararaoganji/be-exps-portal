package portal.expenses;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.junit.jupiter.api.Assertions.assertNotNull;
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
			mockedSpringApp.when(() -> SpringApplication.run(ExpensesPortalApplication.class, new String[]{}))
					.thenReturn(null);

			ExpensesPortalApplication.main(new String[]{});

			mockedSpringApp.verify(() -> SpringApplication.run(ExpensesPortalApplication.class, new String[]{}));
		}
	}
}