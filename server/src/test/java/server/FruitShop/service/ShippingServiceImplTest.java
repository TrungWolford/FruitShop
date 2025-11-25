package server.FruitShop.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import server.FruitShop.dto.request.Shipping.ShippingRequest;
import server.FruitShop.dto.response.Shipping.ShippingResponse;
import server.FruitShop.entity.Account;
import server.FruitShop.entity.Shipping;
import server.FruitShop.repository.AccountRepository;
import server.FruitShop.repository.ShippingRepository;
import server.FruitShop.service.Impl.ShippingServiceImpl;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test - Shipping Service")
class ShippingServiceImplTest {

    @Mock
    private ShippingRepository shippingRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private ShippingServiceImpl shippingService;

    private Shipping testShipping;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        testAccount = new Account();
        testAccount.setAccountId("acc-001");
        testAccount.setAccountName("Nguyễn Văn A");

        testShipping = new Shipping();
        testShipping.setShippingId("ship-001");
        testShipping.setReceiverName("Trần Thị B");
        testShipping.setReceiverPhone("0987654321");
        testShipping.setReceiverAddress("123 Đường ABC");
        testShipping.setCity("Hồ Chí Minh");
        testShipping.setShippingFee(30000);
        testShipping.setStatus(0);
        testShipping.setAccount(testAccount);
    }

    @Test
    @DisplayName("Test 1: Lấy shipping theo ID - Thành công")
    void testGetShippingById_Success() {
        when(shippingRepository.findById("ship-001")).thenReturn(Optional.of(testShipping));

        ShippingResponse result = shippingService.getShippingById("ship-001");

        assertNotNull(result);
        assertEquals("ship-001", result.getShippingId());
        assertEquals("Trần Thị B", result.getReceiverName());
        verify(shippingRepository, times(1)).findById("ship-001");
    }

    @Test
    @DisplayName("Test 2: Lấy shipping theo ID - Không tìm thấy")
    void testGetShippingById_NotFound() {
        when(shippingRepository.findById("invalid-id")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            shippingService.getShippingById("invalid-id");
        });
        
        verify(shippingRepository, times(1)).findById("invalid-id");
    }

    @Test
    @DisplayName("Test 3: Tạo shipping - Thành công")
    void testCreateShipping_Success() {
        ShippingRequest request = new ShippingRequest();
        request.setReceiverName("Lê Văn C");
        request.setReceiverPhone("0123456789");
        request.setReceiverAddress("456 Đường XYZ");
        request.setCity("Hà Nội");
        request.setShippingFee(25000);
        request.setAccountId("acc-001");

        when(accountRepository.findById("acc-001")).thenReturn(Optional.of(testAccount));
        when(shippingRepository.save(any(Shipping.class))).thenReturn(testShipping);

        ShippingResponse result = shippingService.createShipping(request);

        assertNotNull(result);
        verify(accountRepository, times(1)).findById("acc-001");
        verify(shippingRepository, times(1)).save(any(Shipping.class));
    }

    @Test
    @DisplayName("Test 4: Tạo shipping - Account không tồn tại")
    void testCreateShipping_AccountNotFound() {
        ShippingRequest request = new ShippingRequest();
        request.setReceiverName("Test");
        request.setReceiverPhone("0123456789");
        request.setReceiverAddress("Test Address");
        request.setCity("Test City");
        request.setShippingFee(20000);
        request.setAccountId("invalid-id");

        when(accountRepository.findById("invalid-id")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            shippingService.createShipping(request);
        });
        
        verify(shippingRepository, never()).save(any(Shipping.class));
    }

    @Test
    @DisplayName("Test 5: Cập nhật shipping - Thành công")
    void testUpdateShipping_Success() {
        ShippingRequest request = new ShippingRequest();
        request.setReceiverName("Nguyễn Thị D");
        request.setReceiverPhone("0999888777");
        request.setReceiverAddress("789 Đường DEF");
        request.setCity("Đà Nẵng");
        request.setShippingFee(35000);
        request.setStatus(1);

        when(shippingRepository.findById("ship-001")).thenReturn(Optional.of(testShipping));
        when(shippingRepository.save(any(Shipping.class))).thenReturn(testShipping);

        ShippingResponse result = shippingService.updateShipping("ship-001", request);

        assertNotNull(result);
        verify(shippingRepository, times(1)).findById("ship-001");
        verify(shippingRepository, times(1)).save(any(Shipping.class));
    }

    @Test
    @DisplayName("Test 6: Cập nhật shipping status - Thành công")
    void testUpdateShippingStatus_Success() {
        when(shippingRepository.findById("ship-001")).thenReturn(Optional.of(testShipping));
        when(shippingRepository.save(any(Shipping.class))).thenReturn(testShipping);

        ShippingResponse result = shippingService.updateShippingStatus("ship-001", 2);

        assertNotNull(result);
        verify(shippingRepository, times(1)).findById("ship-001");
        verify(shippingRepository, times(1)).save(any(Shipping.class));
    }

    @Test
    @DisplayName("Test 7: Xóa shipping - Thành công")
    void testDeleteShipping_Success() {
        when(shippingRepository.existsById("ship-001")).thenReturn(true);
        doNothing().when(shippingRepository).deleteById("ship-001");

        shippingService.deleteShipping("ship-001");

        verify(shippingRepository, times(1)).existsById("ship-001");
        verify(shippingRepository, times(1)).deleteById("ship-001");
    }

    @Test
    @DisplayName("Test 8: Xóa shipping - Không tìm thấy")
    void testDeleteShipping_NotFound() {
        when(shippingRepository.existsById("invalid-id")).thenReturn(false);

        assertThrows(RuntimeException.class, () -> {
            shippingService.deleteShipping("invalid-id");
        });
        
        verify(shippingRepository, times(1)).existsById("invalid-id");
        verify(shippingRepository, never()).deleteById(any());
    }
}
