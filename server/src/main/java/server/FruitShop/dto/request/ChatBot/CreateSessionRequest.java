package server.FruitShop.dto.request.ChatBot;

import lombok.Data;

/**
 * Request tạo chat session mới
 */
@Data
public class CreateSessionRequest {

    // ID tài khoản người dùng (null nếu là guest)
    private String accountId;

    // Tiêu đề cuộc trò chuyện (tuỳ chọn, có thể để trống)
    private String title;
}
