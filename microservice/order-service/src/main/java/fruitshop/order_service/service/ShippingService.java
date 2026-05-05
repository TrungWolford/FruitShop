package fruitshop.order_service.service;

import fruitshop.order_service.entity.Shipping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ShippingService {
    Shipping upsert(String orderId, Shipping shipping);

    Shipping findByOrderId(String orderId);

    Shipping getShippingById(String shippingId);

    Shipping createShipping(String orderId, Shipping shipping);

    Shipping updateShipping(String shippingId, Shipping shipping);

    void deleteShipping(String shippingId);

    List<Shipping> getAllShippings();

    Page<Shipping> getAllShippingsPaginated(Pageable pageable);

    List<Shipping> getShippingsByAccountId(String accountId);

    Shipping updateShippingStatus(String shippingId, int status);

    Page<Shipping> getShippingsByStatus(int status, Pageable pageable);

    Page<Shipping> searchShippings(String keyword, Pageable pageable);

    Page<Shipping> searchAndFilterShippings(String keyword, Integer status, Pageable pageable);
}
