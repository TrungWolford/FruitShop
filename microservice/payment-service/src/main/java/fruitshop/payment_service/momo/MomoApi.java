package fruitshop.payment_service.momo;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import fruitshop.payment_service.dto.request.Momo.CreateMomoRequest;
import fruitshop.payment_service.dto.response.Momo.CreateMomoResponse;

@FeignClient(name = "momo", url = "${momo.endpoint}")
public interface MomoApi {
    @PostMapping("/create")
    CreateMomoResponse createMomoQR(@RequestBody CreateMomoRequest request);
}