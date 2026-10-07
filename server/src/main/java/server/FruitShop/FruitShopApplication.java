package server.FruitShop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

import java.util.TimeZone;

@SpringBootApplication
@EnableFeignClients
public class FruitShopApplication {

	/**
	 * Set JVM timezone = UTC TRƯỚC KHI Spring context khởi động.
	 *
	 * Lý do: PostgreSQL JDBC driver gửi JVM timezone (java.util.TimeZone.getDefault())
	 * trong startup handshake. Windows đặt timezone là "Asia/Saigon" — alias này không
	 * tồn tại trong PostgreSQL timezone database → FATAL error.
	 *
	 * Dùng static block thay vì đặt trong main() để đảm bảo chạy trước mọi thứ khác,
	 * kể cả class initializer của các dependency.
	 */
	static {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}

	public static void main(String[] args) {
		SpringApplication.run(FruitShopApplication.class, args);
	}

}
