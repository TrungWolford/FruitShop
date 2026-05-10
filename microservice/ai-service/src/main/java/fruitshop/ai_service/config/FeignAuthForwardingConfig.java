package fruitshop.ai_service.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignAuthForwardingConfig {
  @Bean
  public RequestInterceptor authForwardingInterceptor() {
    return template -> {
      ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
      if (attrs == null) {
        return;
      }

      HttpServletRequest request = attrs.getRequest();
      String authHeader = request.getHeader("Authorization");
      if (authHeader != null && !authHeader.isBlank()) {
        template.header("Authorization", authHeader);
      }
    };
  }
}
