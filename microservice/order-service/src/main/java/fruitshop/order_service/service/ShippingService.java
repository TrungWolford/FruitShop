package fruitshop.order_service.service;

import fruitshop.order_service.dto.request.ShippingRequest;
import fruitshop.order_service.dto.response.ShippingResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ShippingService {
    ShippingResponse upsert(String orderId, ShippingRequest request);

    ShippingResponse findByOrderId(String orderId);

    ShippingResponse findByOrderIdSafe(String orderId);

    ShippingResponse getShippingById(String shippingId);

    fruitshop.order_service.entity.Shipping getRawEntityById(String shippingId);

    ShippingResponse createShipping(String orderId, ShippingRequest request);
    ShippingResponse createShipping(ShippingRequest request);

    ShippingResponse updateShipping(String shippingId, ShippingRequest request);

    void deleteShipping(String shippingId);

    List<ShippingResponse> getAllShippings();

    Page<ShippingResponse> getAllShippingsPaginated(Pageable pageable);

    List<ShippingResponse> getShippingsByAccountId(String accountId);

    ShippingResponse updateShippingStatus(String shippingId, int status);

    Page<ShippingResponse> getShippingsByStatus(int status, Pageable pageable);

    Page<ShippingResponse> searchShippings(String keyword, Pageable pageable);

    Page<ShippingResponse> searchAndFilterShippings(String keyword, Integer status, Pageable pageable);
}
