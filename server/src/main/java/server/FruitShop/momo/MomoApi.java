package server.FruitShop.momo;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import server.FruitShop.dto.request.Momo.CreateMomoRequest;
import server.FruitShop.dto.response.Momo.CreateMomoResponse;

@FeignClient(name = "momo", url = "${momo.endpoint}")
public interface MomoApi {

    @PostMapping("/create")
    CreateMomoResponse createMomoQR(@RequestBody CreateMomoRequest request);
}
