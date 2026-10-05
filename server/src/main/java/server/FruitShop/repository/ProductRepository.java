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

    // =========================================================================
    // Single-entity lookups
    // =========================================================================

    /** Fetch product + categories (dùng cho getByProductId, updateProduct). */
    @EntityGraph(attributePaths = "categories")
    @Query("SELECT p FROM Product p WHERE p.productId = :productId")
    Optional<Product> findByIdWithCategories(@Param("productId") String productId);

    /** Fetch product + images (dùng kết hợp sau khi đã load categories). */
    @EntityGraph(attributePaths = "images")
    @Query("SELECT p FROM Product p WHERE p.productId = :productId")
    Optional<Product> findByIdWithImages(@Param("productId") String productId);

    // =========================================================================
    // Batch image fetch – vẫn cần List vì đã có IDs từ page, không phân trang lại
    // =========================================================================

    /**
     * Batch-fetch images cho một tập IDs đã được phân trang sẵn.
     * Chỉ fetch images (không categories) để tránh MultipleBagFetchException.
     */
    @EntityGraph(attributePaths = "images")
    @Query("SELECT DISTINCT p FROM Product p WHERE p.productId IN :ids")
    List<Product> findAllWithImagesByIds(@Param("ids") List<String> ids);

    // =========================================================================
    // Paginated queries – categories được fetch trực tiếp qua @EntityGraph
    // countQuery tách riêng để Hibernate đếm bằng SQL COUNT, không JOIN fetch
    // =========================================================================

    /**
     * Lấy tất cả sản phẩm theo khoảng giá, kèm categories.
     * countQuery không dùng DISTINCT JOIN nên không bị count sai.
     */
    @EntityGraph(attributePaths = "categories")
    @Query(value = "SELECT DISTINCT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice",
           countQuery = "SELECT COUNT(DISTINCT p) FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice")
    Page<Product> findAllByPriceRange(@Param("minPrice") long minPrice,
                                      @Param("maxPrice") long maxPrice,
                                      Pageable pageable);

    /**
     * Lọc theo danh sách categoryId + status + khoảng giá, kèm categories.
     * JOIN p.categories c dùng để filter (WHERE), @EntityGraph fetch toàn bộ categories của product.
     */
    @EntityGraph(attributePaths = "categories")
    @Query(value = "SELECT DISTINCT p FROM Product p JOIN p.categories c " +
                   "WHERE c.categoryId IN :categoryIds AND c.status = :status " +
                   "AND p.price BETWEEN :minPrice AND :maxPrice",
           countQuery = "SELECT COUNT(DISTINCT p) FROM Product p JOIN p.categories c " +
                        "WHERE c.categoryId IN :categoryIds AND c.status = :status " +
                        "AND p.price BETWEEN :minPrice AND :maxPrice")
    Page<Product> findProductsByCategoryIdsAndStatusAndInRangePrice(
            @Param("categoryIds") List<String> categoryIds,
            @Param("status") Integer status,
            @Param("minPrice") long minPrice,
            @Param("maxPrice") long maxPrice,
            Pageable pageable);

    /** Lọc theo status + khoảng giá, kèm categories. */
    @EntityGraph(attributePaths = "categories")
    @Query(value = "SELECT DISTINCT p FROM Product p JOIN p.categories c " +
                   "WHERE c.status = :status AND p.price BETWEEN :minPrice AND :maxPrice",
           countQuery = "SELECT COUNT(DISTINCT p) FROM Product p JOIN p.categories c " +
                        "WHERE c.status = :status AND p.price BETWEEN :minPrice AND :maxPrice")
    Page<Product> findProductsByCategoryStatusAndInRangePrice(
            @Param("status") Integer status,
            @Param("minPrice") long minPrice,
            @Param("maxPrice") long maxPrice,
            Pageable pageable);

    /** Lọc theo danh sách categoryId + khoảng giá, kèm categories. */
    @EntityGraph(attributePaths = "categories")
    @Query(value = "SELECT DISTINCT p FROM Product p JOIN p.categories c " +
                   "WHERE c.categoryId IN :categoryIds AND p.price BETWEEN :minPrice AND :maxPrice",
           countQuery = "SELECT COUNT(DISTINCT p) FROM Product p JOIN p.categories c " +
                        "WHERE c.categoryId IN :categoryIds AND p.price BETWEEN :minPrice AND :maxPrice")
    Page<Product> findProductsByCategoryIdsAndInRangePrice(
            @Param("categoryIds") List<String> categoryIds,
            @Param("minPrice") long minPrice,
            @Param("maxPrice") long maxPrice,
            Pageable pageable);

    /** Tìm kiếm theo tên + khoảng giá, kèm categories. */
    @EntityGraph(attributePaths = "categories")
    @Query(value = "SELECT DISTINCT p FROM Product p " +
                   "WHERE LOWER(p.productName) LIKE LOWER(CONCAT('%', :productName, '%')) " +
                   "AND p.price BETWEEN :minPrice AND :maxPrice",
           countQuery = "SELECT COUNT(DISTINCT p) FROM Product p " +
                        "WHERE LOWER(p.productName) LIKE LOWER(CONCAT('%', :productName, '%')) " +
                        "AND p.price BETWEEN :minPrice AND :maxPrice")
    Page<Product> findByProductNameAndPriceRange(
            @Param("productName") String productName,
            @Param("minPrice") double minPrice,
            @Param("maxPrice") double maxPrice,
            Pageable pageable);

    /** Tìm kiếm theo tên, kèm categories. */
    @EntityGraph(attributePaths = "categories")
    @Query(value = "SELECT DISTINCT p FROM Product p " +
                   "WHERE LOWER(p.productName) LIKE LOWER(CONCAT('%', :productName, '%'))",
           countQuery = "SELECT COUNT(DISTINCT p) FROM Product p " +
                        "WHERE LOWER(p.productName) LIKE LOWER(CONCAT('%', :productName, '%'))")
    Page<Product> findByProductName(@Param("productName") String productName, Pageable pageable);

    // =========================================================================
    // Top-sold – trả List vì cố định 10 bản ghi, không cần phân trang
    // =========================================================================

    /** Lấy top-10 sản phẩm stock thấp nhất, kèm categories. */
    @EntityGraph(attributePaths = "categories")
    @Query("SELECT p FROM Product p ORDER BY p.stock ASC LIMIT 10")
    List<Product> findTop10WithCategoriesOrderByStockAsc();
}
