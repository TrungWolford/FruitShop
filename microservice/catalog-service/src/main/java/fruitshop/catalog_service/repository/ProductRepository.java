package fruitshop.catalog_service.repository;

import fruitshop.catalog_service.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, String> {

  @Query("SELECT p FROM Product p WHERE LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
  Page<Product> searchProducts(@Param("keyword") String keyword, Pageable pageable);

  @Query("SELECT p FROM Product p JOIN p.categories c WHERE c.categoryId = :categoryId")
  Page<Product> findByCategoryId(@Param("categoryId") String categoryId, Pageable pageable);

  @Query("SELECT DISTINCT p FROM Product p LEFT JOIN p.categories c WHERE " +
      "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
      "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
      "(:categoryId IS NULL OR c.categoryId = :categoryId)")
  Page<Product> filterProducts(@Param("minPrice") Long minPrice,
      @Param("maxPrice") Long maxPrice,
      @Param("categoryId") String categoryId,
      Pageable pageable);

  @Query("SELECT DISTINCT p FROM Product p LEFT JOIN p.categories c WHERE " +
      "(:categoryIds IS NULL OR c.categoryId IN :categoryIds) AND " +
      "(:status IS NULL OR p.status = :status) AND " +
      "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
      "(:maxPrice IS NULL OR p.price <= :maxPrice)")
  Page<Product> filter(@Param("categoryIds") List<String> categoryIds,
                       @Param("status") Integer status,
                       @Param("minPrice") Long minPrice,
                       @Param("maxPrice") Long maxPrice,
                       Pageable pageable);

  @Query("SELECT p FROM Product p JOIN p.categories c WHERE c.categoryId = :categoryId AND p.productId != :productId")
  Page<Product> findRelatedProducts(@Param("categoryId") String categoryId, @Param("productId") String productId,
      Pageable pageable);

  @Query("SELECT p FROM Product p ORDER BY p.soldQuantity DESC")
  List<Product> findTop8BestSelling(Pageable pageable);
}
