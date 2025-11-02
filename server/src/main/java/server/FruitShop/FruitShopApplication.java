package server.FruitShop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class FruitShopApplication {

	public static void main(String[] args) {
		SpringApplication.run(FruitShopApplication.class, args);
	}

}
