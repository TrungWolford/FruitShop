package fruitshop.order_service.feign;

import fruitshop.order_service.feign.dto.PaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "payment-service", path = "/api/payment")
public interface PaymentClient {
    @GetMapping("/{paymentId}")
    PaymentResponse getByPaymentId(@PathVariable("paymentId") String paymentId);

    @PutMapping("/{paymentId}/status")
    PaymentResponse updateStatus(@PathVariable("paymentId") String paymentId, @RequestParam("status") int status);
}
