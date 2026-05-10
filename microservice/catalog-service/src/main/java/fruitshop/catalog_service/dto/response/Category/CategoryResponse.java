package fruitshop.catalog_service.dto.response.Category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import fruitshop.catalog_service.entity.Category;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private String categoryId;
    private String categoryName;
    private int status;

    public static CategoryResponse fromEntity(Category category){
        if (category == null) return null;
        CategoryResponse response = new CategoryResponse();
        response.setCategoryId(category.getCategoryId());
        response.setCategoryName(category.getCategoryName());
        response.setStatus(category.getStatus());
        return response;
    }
}
