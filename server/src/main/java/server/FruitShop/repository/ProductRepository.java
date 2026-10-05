package server.FruitShop.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import server.FruitShop.entity.Product;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {

    // -------------------------------------------------------------------------
    // Single-entity lookups – dùng @EntityGraph để fetch từng collection
    // riêng bằng subselect, tránh MultipleBagFetchException
    // -------------------------------------------------------------------------

    /** Fetch product + categories (dùng cho getByProductId, updateProduct). */
    @EntityGraph(attributePaths = "categories")
    @Query("SELECT p FROM Product p WHERE p.productId = :productId")
    Optional<Product> findByIdWithCategories(@Param("productId") String productId);

    /** Fetch product + images (dùng kết hợp sau khi đã có categories). */
    @EntityGraph(attributePaths = "images")
    @Query("SELECT p FROM Product p WHERE p.productId = :productId")
    Optional<Product> findByIdWithImages(@Param("productId") String productId);

    // -------------------------------------------------------------------------
    // Batch fetches – trả List để dùng cho các page queries
    // Mỗi query chỉ fetch 1 collection → tránh MultipleBagFetchException
    // -------------------------------------------------------------------------

    /** Fetch categories cho danh sách product IDs (1 query IN). */
    @EntityGraph(attributePaths = "categories")
    @Query("SELECT DISTINCT p FROM Product p WHERE p.productId IN :ids")
    List<Product> findAllWithCategoriesByIds(@Param("ids") List<String> ids);

    /** Fetch images cho danh sách product IDs (1 query IN). */
    @EntityGraph(attributePaths = "images")
    @Query("SELECT DISTINCT p FROM Product p WHERE p.productId IN :ids")
    List<Product> findAllWithImagesByIds(@Param("ids") List<String> ids);

    // -------------------------------------------------------------------------
    // Page queries – chỉ lấy IDs / scalar fields, KHÔNG fetch collections
    // (collections sẽ được batch-fetch ở tầng service)
    // -------------------------------------------------------------------------

    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice")
    Page<Product> findAllByPriceRange(@Param("minPrice") long minPrice,
                                      @Param("maxPrice") long maxPrice,
                                      Pageable pageable);

    @Query("SELECT DISTINCT p FROM Product p " +
            "JOIN p.categories c " +
            "WHERE c.categoryId IN :categoryIds " +
            "AND c.status = :status " +
            "AND p.price BETWEEN :minPrice AND :maxPrice")
    Page<Product> findProductsByCategoryIdsAndStatusAndInRangePrice(
            @Param("categoryIds") List<String> categoryIds,
            @Param("status") Integer status,
            @Param("minPrice") long minPrice,
            @Param("maxPrice") long maxPrice,
            Pageable pageable);

    @Query("SELECT DISTINCT p FROM Product p " +
            "JOIN p.categories c " +
            "WHERE c.status = :status " +
            "AND p.price BETWEEN :minPrice AND :maxPrice")
    Page<Product> findProductsByCategoryStatusAndInRangePrice(
            @Param("status") Integer status,
            @Param("minPrice") long minPrice,
            @Param("maxPrice") long maxPrice,
            Pageable pageable);

    @Query("SELECT DISTINCT p FROM Product p " +
            "JOIN p.categories c " +
            "WHERE c.categoryId IN :categoryIds " +
            "AND p.price BETWEEN :minPrice AND :maxPrice")
    Page<Product> findProductsByCategoryIdsAndInRangePrice(
            @Param("categoryIds") List<String> categoryIds,
            @Param("minPrice") long minPrice,
            @Param("maxPrice") long maxPrice,
            Pageable pageable);

    @Query("SELECT DISTINCT p FROM Product p " +
            "WHERE LOWER(p.productName) LIKE LOWER(CONCAT('%', :productName, '%')) " +
            "AND p.price BETWEEN :minPrice AND :maxPrice")
    Page<Product> findByProductNameAndPriceRange(
            @Param("productName") String productName,
            @Param("minPrice") double minPrice,
            @Param("maxPrice") double maxPrice,
            Pageable pageable);

    @Query("SELECT DISTINCT p FROM Product p " +
            "WHERE LOWER(p.productName) LIKE LOWER(CONCAT('%', :productName, '%'))")
    Page<Product> findByProductName(@Param("productName") String productName, Pageable pageable);

    /** Fetch top-10 product + categories + images bằng 2 query riêng. */
    @EntityGraph(attributePaths = "categories")
    @Query("SELECT p FROM Product p ORDER BY p.stock ASC LIMIT 10")
    List<Product> findTop10WithCategoriesOrderByStockAsc();

    @EntityGraph(attributePaths = "images")
    @Query("SELECT p FROM Product p ORDER BY p.stock ASC LIMIT 10")
    List<Product> findTop10WithImagesOrderByStockAsc();
}
