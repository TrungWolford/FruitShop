package fruitshop.catalog_service.repository;

import fruitshop.catalog_service.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, String> {
    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN p.categories c WHERE " +
           "(:categoryIds IS NULL OR c.categoryId IN :categoryIds) AND " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(p.price BETWEEN :minPrice AND :maxPrice)")
    Page<Product> filterProducts(
            @Param("categoryIds") List<String> categoryIds,
            @Param("status") Integer status,
            @Param("minPrice") long minPrice,
            @Param("maxPrice") long maxPrice,
            Pageable pageable);
}
