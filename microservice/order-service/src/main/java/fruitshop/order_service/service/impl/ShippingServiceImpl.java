package fruitshop.order_service.service.impl;

import fruitshop.order_service.entity.Order;
import fruitshop.order_service.entity.Shipping;
import fruitshop.order_service.repository.OrderRepository;
import fruitshop.order_service.repository.ShippingRepository;
import fruitshop.order_service.service.ShippingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShippingServiceImpl implements ShippingService {
    private final ShippingRepository shippingRepository;
    private final OrderRepository orderRepository;

    @Override
    public Shipping upsert(String orderId, Shipping shipping) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        Shipping existing = shippingRepository.findByOrderOrderId(orderId).orElse(null);
        if (existing != null) {
            existing.setAccountId(shipping.getAccountId());
            existing.setReceiverName(shipping.getReceiverName());
            existing.setReceiverPhone(shipping.getReceiverPhone());
            existing.setReceiverAddress(shipping.getReceiverAddress());
            existing.setCity(shipping.getCity());
            existing.setShipperName(shipping.getShipperName());
            existing.setShippingFee(shipping.getShippingFee());
            existing.setShippedAt(shipping.getShippedAt());
            existing.setStatus(shipping.getStatus());
            return shippingRepository.save(existing);
        }

        shipping.setOrder(order);
        return shippingRepository.save(shipping);
    }

    @Override
    public Shipping findByOrderId(String orderId) {
        return shippingRepository.findByOrderOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Shipping not found for order: " + orderId));
    }

    @Override
    public Shipping getShippingById(String shippingId) {
        return shippingRepository.findById(shippingId)
                .orElseThrow(() -> new IllegalArgumentException("Shipping not found with id: " + shippingId));
    }

    @Override
    public Shipping createShipping(String orderId, Shipping shipping) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        if (shippingRepository.findByOrderOrderId(orderId).isPresent()) {
            throw new IllegalArgumentException("Shipping already exists for order: " + orderId);
        }
        shipping.setOrder(order);
        return shippingRepository.save(shipping);
    }

    @Override
    public Shipping updateShipping(String shippingId, Shipping shipping) {
        Shipping existing = getShippingById(shippingId);
        existing.setAccountId(shipping.getAccountId());
        existing.setReceiverName(shipping.getReceiverName());
        existing.setReceiverPhone(shipping.getReceiverPhone());
        existing.setReceiverAddress(shipping.getReceiverAddress());
        existing.setCity(shipping.getCity());
        existing.setShipperName(shipping.getShipperName());
        existing.setShippingFee(shipping.getShippingFee());
        existing.setShippedAt(shipping.getShippedAt());
        return shippingRepository.save(existing);
    }

    @Override
    public void deleteShipping(String shippingId) {
        if (!shippingRepository.existsById(shippingId)) {
            throw new IllegalArgumentException("Shipping not found with id: " + shippingId);
        }
        shippingRepository.deleteById(shippingId);
    }

    @Override
    public List<Shipping> getAllShippings() {
        return shippingRepository.findAll();
    }

    @Override
    public Page<Shipping> getAllShippingsPaginated(Pageable pageable) {
        return shippingRepository.findAll(pageable);
    }

    @Override
    public List<Shipping> getShippingsByAccountId(String accountId) {
        return shippingRepository.findByAccountId(accountId);
    }

    @Override
    public Shipping updateShippingStatus(String shippingId, int status) {
        Shipping shipping = shippingRepository.findById(shippingId)
                .orElseThrow(() -> new IllegalArgumentException("Shipping not found with id: " + shippingId));
        shipping.setStatus(status);
        return shippingRepository.save(shipping);
    }

    @Override
    public Page<Shipping> getShippingsByStatus(int status, Pageable pageable) {
        return shippingRepository.findByStatus(status, pageable);
    }

    @Override
    public Page<Shipping> searchShippings(String keyword, Pageable pageable) {
        return shippingRepository.searchShippings(keyword, pageable);
    }

    @Override
    public Page<Shipping> searchAndFilterShippings(String keyword, Integer status, Pageable pageable) {
        if (keyword != null && !keyword.trim().isEmpty() && status != null) {
            return shippingRepository.searchAndFilterShippings(keyword, status, pageable);
        } else if (keyword != null && !keyword.trim().isEmpty()) {
            return shippingRepository.searchShippings(keyword, pageable);
        } else if (status != null) {
            return shippingRepository.findByStatus(status, pageable);
        } else {
            return shippingRepository.findAll(pageable);
        }
    }
}
