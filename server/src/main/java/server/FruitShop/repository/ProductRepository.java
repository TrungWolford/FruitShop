package server.FruitShop.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import server.FruitShop.entity.Product;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {

    @Query("SELECT DISTINCT p " +
            "FROM Product p " +
            "LEFT JOIN FETCH p.categories " +
            "WHERE p.productId IN :productIds")
    List<Product> findByIdsWithCategories(@Param("productIds") List<String> productIds);

    @Query("SELECT DISTINCT p " +
            "FROM Product p " +
            "JOIN p.categories c " +
            "WHERE c.categoryId IN :categoryIds " +
            "AND c.status = :status " +
            "AND p.price BETWEEN :minPrice AND :maxPrice")
    Page<Product> findProductsByCategoryIdsAndStatusAndInRangePrice(@Param("categoryIds") List<String> categoryIds, @Param("status") Integer status,
                                                                    @Param("minPrice") long minPrice,
                                                                    @Param("maxPrice") long maxPrice, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice")
    Page<Product> findAllByPriceRange(@Param("minPrice") long minPrice,
                                      @Param("maxPrice") long maxPrice,
                                      Pageable pageable);


    @Query("SELECT DISTINCT p " +
            "FROM Product p " +
            "JOIN p.categories c " +
            "WHERE c.status = :status " +
            "AND p.price BETWEEN :minPrice AND :maxPrice")
    Page<Product> findProductsByCategoryStatusAndInRangePrice(@Param("status") Integer status,
                                                              @Param("minPrice") long minPrice,
                                                              @Param("maxPrice") long maxPrice, Pageable pageable);

    @Query("SELECT DISTINCT p " +
            "FROM Product p " +
            "WHERE LOWER(p.productName) LIKE LOWER(CONCAT('%', :productName, '%')) ")
    Page<Product> findByProductName(@Param("productName") String productName, Pageable pageable);

    @Query("SELECT DISTINCT p " +
            "FROM Product p " +
            "JOIN p.categories c " +
            "WHERE c.categoryId IN :categoryIds " +
            "AND p.price BETWEEN :minPrice AND :maxPrice")
    Page<Product> findProductsByCategoryIdsAndInRangePrice(@Param("categoryIds") List<String> categoryIds,
                                                           @Param("minPrice") long minPrice,
                                                           @Param("maxPrice") long maxPrice, Pageable pageable);

    // Load product with images and categories (separate queries to avoid MultipleBagFetchException)
    @Query("SELECT DISTINCT p " +
            "FROM Product p " +
            "LEFT JOIN FETCH p.categories " +
            "WHERE p.productId = :productId")
    Product findByIdWithCategories(@Param("productId") String productId);

    @Query("SELECT DISTINCT p " +
            "FROM Product p " +
            "LEFT JOIN FETCH p.images " +
            "WHERE p.productId = :productId")
    Product findByIdWithImages(@Param("productId") String productId);

    List<Product> findTop10ByOrderByStockAsc();
}
