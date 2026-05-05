package server.FruitShop.dto.request.ChatBot;

import lombok.Data;

/**
 * Request cập nhật trạng thái session
 * Ví dụ: đóng session, đổi tiêu đề, reset unread count
 */
@Data
public class UpdateSessionRequest {

    // Tiêu đề mới (null = không đổi)
    private String title;

    // Trạng thái mới: 0=Đóng, 1=Mở, 2=Chờ phản hồi (null = không đổi)
    private Integer status;

    // Reset số tin chưa đọc về 0
    private boolean resetUnread;
}
