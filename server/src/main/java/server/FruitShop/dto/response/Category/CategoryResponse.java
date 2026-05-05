package server.FruitShop.dto.response.Category;

import lombok.Data;
import server.FruitShop.entity.Category;

@Data
public class CategoryResponse {
    private String categoryId;
    private String categoryName;
    private int status;

    public static CategoryResponse fromEntity(Category category){
        CategoryResponse response = new CategoryResponse();
        response.setCategoryId(category.getCategoryId());
        response.setCategoryName(category.getCategoryName());
        response.setStatus(category.getStatus());
        return response;
    }
}
