package server.FruitShop.dto.request.Category;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCategoryRequest {
    @NotBlank(message = "Tên danh mục không được để trống")
    private String categoryName;

    private int status = 1;
}
