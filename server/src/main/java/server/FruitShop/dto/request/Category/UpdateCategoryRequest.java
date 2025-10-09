package server.FruitShop.dto.request.Category;

import lombok.Data;

@Data
public class UpdateCategoryRequest {
    private String categoryName;

    private int status;
}
