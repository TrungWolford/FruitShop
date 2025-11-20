package server.FruitShop;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.properties")
class FruitShopApplicationTests {

	@Test
	void contextLoads() {
		// Test will pass if application context loads successfully
	}

}
