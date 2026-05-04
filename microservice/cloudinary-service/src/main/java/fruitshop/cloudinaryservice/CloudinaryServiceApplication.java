package fruitshop.cloudinaryservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class CloudinaryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudinaryServiceApplication.class, args);
    }

}
