package fruitshop.order_service.service.impl;

import fruitshop.order_service.dto.request.ShippingRequest;
import fruitshop.order_service.dto.response.ShippingResponse;
import fruitshop.order_service.entity.Order;
import fruitshop.order_service.entity.Shipping;
import fruitshop.order_service.repository.OrderRepository;
import fruitshop.order_service.repository.ShippingRepository;
import fruitshop.order_service.service.ShippingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShippingServiceImpl implements ShippingService {
    private final ShippingRepository shippingRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public ShippingResponse upsert(String orderId, ShippingRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        Shipping existing = shippingRepository.findByOrderOrderId(orderId).orElse(null);
        if (existing != null) {
            updateFields(existing, request);
            return mapToResponse(shippingRepository.save(existing));
        }

        Shipping shipping = new Shipping();
        shipping.setOrder(order);
        updateFields(shipping, request);
        return mapToResponse(shippingRepository.save(shipping));
    }

    @Override
    public ShippingResponse findByOrderId(String orderId) {
        return shippingRepository.findByOrderOrderId(orderId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new IllegalArgumentException("Shipping not found for order: " + orderId));
    }

    @Override
    public ShippingResponse findByOrderIdSafe(String orderId) {
        return shippingRepository.findByOrderOrderId(orderId)
                .map(this::mapToResponse)
                .orElse(null);
    }

    @Override
    public ShippingResponse getShippingById(String shippingId) {
        return shippingRepository.findById(shippingId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new IllegalArgumentException("Shipping not found with id: " + shippingId));
    }

    @Override
    public Shipping getRawEntityById(String shippingId) {
        return shippingRepository.findById(shippingId).orElse(null);
    }

    @Override
    @Transactional
    public ShippingResponse createShipping(String orderId, ShippingRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        if (shippingRepository.findByOrderOrderId(orderId).isPresent()) {
            throw new IllegalArgumentException("Shipping already exists for order: " + orderId);
        }
        Shipping shipping = new Shipping();
        shipping.setOrder(order);
        updateFields(shipping, request);
        shipping.setStatus(0); // Default status when creating from order
        return mapToResponse(shippingRepository.save(shipping));
    }

    @Override
    @Transactional
    public ShippingResponse createShipping(ShippingRequest request) {
        Shipping shipping = new Shipping();
        updateFields(shipping, request);
        shipping.setStatus(0); // Default status when creating new
        return mapToResponse(shippingRepository.save(shipping));
    }

    @Override
    @Transactional
    public ShippingResponse updateShipping(String shippingId, ShippingRequest request) {
        Shipping existing = shippingRepository.findById(shippingId)
                .orElseThrow(() -> new IllegalArgumentException("Shipping not found with id: " + shippingId));
        updateFields(existing, request);
        existing.setStatus(request.getStatus()); // Update status from request
        return mapToResponse(shippingRepository.save(existing));
    }

    private void updateFields(Shipping target, ShippingRequest source) {
        if (source.getAccountId() != null) target.setAccountId(source.getAccountId());
        if (source.getReceiverName() != null) target.setReceiverName(source.getReceiverName());
        if (source.getReceiverPhone() != null) target.setReceiverPhone(source.getReceiverPhone());
        if (source.getReceiverAddress() != null) target.setReceiverAddress(source.getReceiverAddress());
        if (source.getCity() != null) target.setCity(source.getCity());
        if (source.getShipperName() != null) target.setShipperName(source.getShipperName());
        target.setShippingFee(source.getShippingFee());
        if (source.getShippedAt() != null) target.setShippedAt(source.getShippedAt());
    }

    private ShippingResponse mapToResponse(Shipping shipping) {
        if (shipping == null) return null;
        return ShippingResponse.builder()
                .shippingId(shipping.getShippingId())
                .orderId(shipping.getOrder() != null ? shipping.getOrder().getOrderId() : null)
                .accountId(shipping.getAccountId())
                .receiverName(shipping.getReceiverName())
                .receiverPhone(shipping.getReceiverPhone())
                .receiverAddress(shipping.getReceiverAddress())
                .city(shipping.getCity())
                .shipperName(shipping.getShipperName())
                .shippingFee(shipping.getShippingFee())
                .shippedAt(shipping.getShippedAt())
                .status(shipping.getStatus())
                .build();
    }

    @Override
    public void deleteShipping(String shippingId) {
        if (!shippingRepository.existsById(shippingId)) {
            throw new IllegalArgumentException("Shipping not found with id: " + shippingId);
        }
        shippingRepository.deleteById(shippingId);
    }

    @Override
    public List<ShippingResponse> getAllShippings() {
        return shippingRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public Page<ShippingResponse> getAllShippingsPaginated(Pageable pageable) {
        return shippingRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Override
    public List<ShippingResponse> getShippingsByAccountId(String accountId) {
        return shippingRepository.findByAccountId(accountId).stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ShippingResponse updateShippingStatus(String shippingId, int status) {
        Shipping shipping = shippingRepository.findById(shippingId)
                .orElseThrow(() -> new IllegalArgumentException("Shipping not found with id: " + shippingId));
        shipping.setStatus(status);
        return mapToResponse(shippingRepository.save(shipping));
    }

    @Override
    public Page<ShippingResponse> getShippingsByStatus(int status, Pageable pageable) {
        return shippingRepository.findByStatus(status, pageable).map(this::mapToResponse);
    }

    @Override
    public Page<ShippingResponse> searchShippings(String keyword, Pageable pageable) {
        return shippingRepository.searchShippings(keyword, pageable).map(this::mapToResponse);
    }

    @Override
    public Page<ShippingResponse> searchAndFilterShippings(String keyword, Integer status, Pageable pageable) {
        Page<Shipping> shippings;
        if (keyword != null && !keyword.trim().isEmpty() && status != null) {
            shippings = shippingRepository.searchAndFilterShippings(keyword, status, pageable);
        } else if (keyword != null && !keyword.trim().isEmpty()) {
            shippings = shippingRepository.searchShippings(keyword, pageable);
        } else if (status != null) {
            shippings = shippingRepository.findByStatus(status, pageable);
        } else {
            shippings = shippingRepository.findAll(pageable);
        }
        return shippings.map(this::mapToResponse);
    }
}
