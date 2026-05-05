package server.FruitShop.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import server.FruitShop.dto.request.Shipping.ShippingRequest;
import server.FruitShop.dto.response.Shipping.ShippingResponse;

import java.util.List;

public interface ShippingService {
    // Basic CRUD operations
    ShippingResponse createShipping(ShippingRequest request);
    ShippingResponse getShippingById(String shippingId);
    ShippingResponse updateShipping(String shippingId, ShippingRequest request);
    void deleteShipping(String shippingId);
    
    // Query operations
    List<ShippingResponse> getAllShippings();
    Page<ShippingResponse> getAllShippingsPaginated(Pageable pageable);
    List<ShippingResponse> getShippingsByAccountId(String accountId);
    
    // Status operations
    ShippingResponse updateShippingStatus(String shippingId, int status);
    
    // Filter and search
    Page<ShippingResponse> getShippingsByStatus(int status, Pageable pageable);
    Page<ShippingResponse> searchShippings(String keyword, Pageable pageable);
    Page<ShippingResponse> searchAndFilterShippings(String keyword, Integer status, Pageable pageable);
}
