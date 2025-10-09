package server.FruitShop.dto.request.Category;

import lombok.Data;

@Data
public class CreateCategoryRequest {
    private String categoryName;

    private int status = 1;
}
