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

/**
 * Unit Test cho ShippingService
 * Class này test các chức năng quản lý thông tin giao hàng
 * bao gồm: CRUD operations, cập nhật trạng thái giao hàng, quản lý địa chỉ người nhận
 */
@ExtendWith(MockitoExtension.class) // Kích hoạt Mockito framework
@DisplayName("Unit Test - Shipping Service")
class ShippingServiceImplTest {

    @Mock // Mock repository quản lý thông tin giao hàng
    private ShippingRepository shippingRepository;

    @Mock // Mock repository quản lý tài khoản
    private AccountRepository accountRepository;

    @InjectMocks // Inject các mock vào service
    private ShippingServiceImpl shippingService;

    // Các entity mẫu để test
    private Shipping testShipping;
    private Account testAccount;

    /**
     * Khởi tạo dữ liệu test trước mỗi test case
     * Tạo account và shipping mẫu với thông tin đầy đủ
     */
    @BeforeEach
    void setUp() {
        // Tạo account test
        testAccount = new Account();
        testAccount.setAccountId("acc-001");
        testAccount.setAccountName("Nguyễn Văn A");

        // Tạo shipping test với thông tin người nhận
        testShipping = new Shipping();
        testShipping.setShippingId("ship-001");
        testShipping.setReceiverName("Trần Thị B"); // Tên người nhận
        testShipping.setReceiverPhone("0987654321"); // SĐT người nhận
        testShipping.setReceiverAddress("123 Đường ABC"); // Địa chỉ nhận hàng
        testShipping.setCity("Hồ Chí Minh"); // Thành phố
        testShipping.setShippingFee(30000); // Phí ship 30k
        testShipping.setStatus(0); // Status 0: Chưa giao
        testShipping.setAccount(testAccount); // Liên kết với account
    }

    /**
     * Test 1: Lấy thông tin giao hàng theo ID - Trường hợp thành công
     * Kịch bản: Tìm shipping với ID hợp lệ đang tồn tại
     * Kỳ vọng: Trả về ShippingResponse chứa đầy đủ thông tin người nhận và địa chỉ
     */
    @Test
    @DisplayName("Test 1: Lấy shipping theo ID - Thành công")
    void testGetShippingById_Success() {
        // ARRANGE: Giả lập repository trả về shipping
        when(shippingRepository.findById("ship-001")).thenReturn(Optional.of(testShipping));

        // ACT: Gọi service lấy shipping
        ShippingResponse result = shippingService.getShippingById("ship-001");

        // ASSERT: Kiểm tra kết quả
        assertNotNull(result); // Kết quả không null
        assertEquals("ship-001", result.getShippingId()); // ID đúng
        assertEquals("Trần Thị B", result.getReceiverName()); // Tên người nhận đúng
        verify(shippingRepository, times(1)).findById("ship-001"); // Gọi repo 1 lần
    }

    /**
     * Test 2: Lấy shipping theo ID - Trường hợp không tìm thấy
     * Kịch bản: Tìm shipping với ID không tồn tại trong hệ thống
     * Kỳ vọng: Nem RuntimeException vì không tìm thấy thông tin giao hàng
     */
    @Test
    @DisplayName("Test 2: Lấy shipping theo ID - Không tìm thấy")
    void testGetShippingById_NotFound() {
        // ARRANGE: Giả lập không tìm thấy shipping
        when(shippingRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // ACT & ASSERT: Gọi service và kiểm tra exception
        assertThrows(RuntimeException.class, () -> {
            shippingService.getShippingById("invalid-id"); // Phải ném exception
        });
        
        verify(shippingRepository, times(1)).findById("invalid-id"); // Đã gọi repo
    }

    /**
     * Test 3: Tạo thông tin giao hàng mới - Trường hợp thành công
     * Kịch bản: Khách hàng thêm địa chỉ nhận hàng mới cho đơn hàng
     * Kỳ vọng: Tạo shipping thành công với đầy đủ thông tin người nhận
     */
    @Test
    @DisplayName("Test 3: Tạo shipping - Thành công")
    void testCreateShipping_Success() {
        // ARRANGE: Chuẩn bị request tạo shipping
        ShippingRequest request = new ShippingRequest();
        request.setReceiverName("Lê Văn C"); // Tên người nhận mới
        request.setReceiverPhone("0123456789"); // SĐT người nhận
        request.setReceiverAddress("456 Đường XYZ"); // Địa chỉ giao hàng
        request.setCity("Hà Nội"); // Thành phố
        request.setShippingFee(25000); // Phí ship 25k
        request.setAccountId("acc-001"); // Liên kết với account

        // Giả lập tìm thấy account và lưu shipping thành công
        when(accountRepository.findById("acc-001")).thenReturn(Optional.of(testAccount));
        when(shippingRepository.save(any(Shipping.class))).thenReturn(testShipping);

        // ACT: Gọi service tạo shipping
        ShippingResponse result = shippingService.createShipping(request);

        // ASSERT: Kiểm tra kết quả
        assertNotNull(result); // Kết quả không null
        verify(accountRepository, times(1)).findById("acc-001"); // Đã tìm account
        verify(shippingRepository, times(1)).save(any(Shipping.class)); // Đã lưu shipping
    }

    /**
     * Test 4: Tạo shipping - Trường hợp account không tồn tại
     * Kịch bản: Tạo shipping với account ID không hợp lệ
     * Kỳ vọng: Nem RuntimeException, không tạo shipping trong DB
     */
    @Test
    @DisplayName("Test 4: Tạo shipping - Account không tồn tại")
    void testCreateShipping_AccountNotFound() {
        // ARRANGE: Chuẩn bị request với account ID không tồn tại
        ShippingRequest request = new ShippingRequest();
        request.setReceiverName("Test");
        request.setReceiverPhone("0123456789");
        request.setReceiverAddress("Test Address");
        request.setCity("Test City");
        request.setShippingFee(20000);
        request.setAccountId("invalid-id"); // Account không tồn tại

        // Giả lập không tìm thấy account
        when(accountRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // ACT & ASSERT: Gọi service và kiểm tra exception
        assertThrows(RuntimeException.class, () -> {
            shippingService.createShipping(request); // Phải ném exception
        });
        
        verify(shippingRepository, never()).save(any(Shipping.class)); // Không lưu shipping
    }

    /**
     * Test 5: Cập nhật thông tin giao hàng - Trường hợp thành công
     * Kịch bản: Khách hàng sửa đổi thông tin người nhận, địa chỉ, phí ship
     * Kỳ vọng: Shipping được cập nhật và lưu lại trong DB
     */
    @Test
    @DisplayName("Test 5: Cập nhật shipping - Thành công")
    void testUpdateShipping_Success() {
        // ARRANGE: Chuẩn bị request cập nhật
        ShippingRequest request = new ShippingRequest();
        request.setReceiverName("Nguyễn Thị D"); // Đổi tên người nhận
        request.setReceiverPhone("0999888777"); // Đổi số điện thoại
        request.setReceiverAddress("789 Đường DEF"); // Đổi địa chỉ
        request.setCity("Đà Nẵng"); // Đổi thành phố
        request.setShippingFee(35000); // Tăng phí ship lên 35k
        request.setStatus(1); // Cập nhật status: 1 = Đang giao

        when(shippingRepository.findById("ship-001")).thenReturn(Optional.of(testShipping));
        when(shippingRepository.save(any(Shipping.class))).thenReturn(testShipping);

        // ACT: Gọi service cập nhật
        ShippingResponse result = shippingService.updateShipping("ship-001", request);

        // ASSERT: Kiểm tra kết quả
        assertNotNull(result); // Kết quả không null
        verify(shippingRepository, times(1)).findById("ship-001"); // Đã tìm shipping
        verify(shippingRepository, times(1)).save(any(Shipping.class)); // Đã lưu cập nhật
    }

    /**
     * Test 6: Cập nhật trạng thái giao hàng - Trường hợp thành công
     * Kịch bản: Shipper cập nhật trạng thái đơn hàng (0: Chưa giao → 2: Đã giao)
     * Kỳ vọng: Status của shipping được cập nhật thành công
     */
    @Test
    @DisplayName("Test 6: Cập nhật shipping status - Thành công")
    void testUpdateShippingStatus_Success() {
        // ARRANGE: Giả lập tìm thấy shipping
        when(shippingRepository.findById("ship-001")).thenReturn(Optional.of(testShipping));
        when(shippingRepository.save(any(Shipping.class))).thenReturn(testShipping);

        // ACT: Cập nhật status sang 2 (Đã giao)
        ShippingResponse result = shippingService.updateShippingStatus("ship-001", 2);

        // ASSERT: Kiểm tra kết quả
        assertNotNull(result); // Kết quả không null
        verify(shippingRepository, times(1)).findById("ship-001"); // Đã tìm shipping
        verify(shippingRepository, times(1)).save(any(Shipping.class)); // Đã lưu status mới
    }

    /**
     * Test 7: Xóa thông tin giao hàng - Trường hợp thành công
     * Kịch bản: Khách hàng xóa địa chỉ giao hàng không còn dùng
     * Kỳ vọng: Shipping bị xóa khỏi hệ thống
     */
    @Test
    @DisplayName("Test 7: Xóa shipping - Thành công")
    void testDeleteShipping_Success() {
        // ARRANGE: Giả lập shipping tồn tại
        when(shippingRepository.existsById("ship-001")).thenReturn(true);
        doNothing().when(shippingRepository).deleteById("ship-001");

        // ACT: Gọi service xóa shipping
        shippingService.deleteShipping("ship-001");

        // ASSERT: Kiểm tra đã xóa
        verify(shippingRepository, times(1)).existsById("ship-001"); // Đã kiểm tra tồn tại
        verify(shippingRepository, times(1)).deleteById("ship-001"); // Đã xóa
    }

    /**
     * Test 8: Xóa shipping - Trường hợp không tìm thấy
     * Kịch bản: Xóa shipping với ID không tồn tại
     * Kỳ vọng: Nem RuntimeException, không thực hiện xóa
     */
    @Test
    @DisplayName("Test 8: Xóa shipping - Không tìm thấy")
    void testDeleteShipping_NotFound() {
        // ARRANGE: Giả lập shipping không tồn tại
        when(shippingRepository.existsById("invalid-id")).thenReturn(false);

        // ACT & ASSERT: Gọi service và kiểm tra exception
        assertThrows(RuntimeException.class, () -> {
            shippingService.deleteShipping("invalid-id"); // Phải ném exception
        });
        
        verify(shippingRepository, times(1)).existsById("invalid-id"); // Đã kiểm tra
        verify(shippingRepository, never()).deleteById(any()); // Không xóa
    }
}
