package fruitshop.order_service.service.impl;

import fruitshop.order_service.dto.request.ShippingRequest;
import fruitshop.order_service.entity.Order;
import fruitshop.order_service.entity.Shipping;
import fruitshop.order_service.repository.OrderRepository;
import fruitshop.order_service.repository.ShippingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShippingServiceImplTest {

    @Mock
    private ShippingRepository shippingRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private ShippingServiceImpl shippingService;

    @Test
    void createShipping_existingShippingForOrder_throwsIllegalArgument() {
        Order order = new Order();
        order.setOrderId("o-1");
        when(orderRepository.findById("o-1")).thenReturn(Optional.of(order));
        when(shippingRepository.findByOrderOrderId("o-1")).thenReturn(Optional.of(new Shipping()));

        ShippingRequest request = ShippingRequest.builder().receiverName("A").build();

        assertThrows(IllegalArgumentException.class, () -> shippingService.createShipping("o-1", request));
    }

    @Test
    void findByOrderId_found_returnsData() {
        Shipping shipping = new Shipping();
        shipping.setShippingId("s-1");
        shipping.setStatus(1);
        when(shippingRepository.findByOrderOrderId("o-1")).thenReturn(Optional.of(shipping));

        var response = shippingService.findByOrderId("o-1");

        assertEquals("s-1", response.getShippingId());
    }

    @Test
    void createShipping_validOrder_savesSuccessfully() {
        Order order = new Order();
        order.setOrderId("o-1");
        when(orderRepository.findById("o-1")).thenReturn(Optional.of(order));
        when(shippingRepository.findByOrderOrderId("o-1")).thenReturn(Optional.empty());

        Shipping newShipping = new Shipping();
        newShipping.setShippingId("s-2");
        newShipping.setStatus(0);
        when(shippingRepository.save(any(Shipping.class))).thenReturn(newShipping);

        ShippingRequest request = ShippingRequest.builder()
                .receiverName("Nguyen Van A")
                .receiverPhone("0999999999")
                .receiverAddress("123 Street")
                .build();

        var response = shippingService.createShipping("o-1", request);

        assertEquals("s-2", response.getShippingId());
        verify(shippingRepository).save(any(Shipping.class));
    }

    @Test
    void getShippingById_found_returnsData() {
        Shipping shipping = new Shipping();
        shipping.setShippingId("s-1");
        shipping.setStatus(1);
        when(shippingRepository.findById("s-1")).thenReturn(Optional.of(shipping));

        var response = shippingService.getShippingById("s-1");

        assertEquals("s-1", response.getShippingId());
        assertEquals(1, response.getStatus());
    }

    @Test
    void findByOrderId_notFound_throwsException() {
        when(shippingRepository.findByOrderOrderId("missing")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> shippingService.findByOrderId("missing"));
    }
}
