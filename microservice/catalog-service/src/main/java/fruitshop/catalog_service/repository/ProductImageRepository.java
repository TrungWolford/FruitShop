package fruitshop.catalog_service.repository;

import fruitshop.catalog_service.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
	List<ProductImage> findByProductProductId(String productId);
}
