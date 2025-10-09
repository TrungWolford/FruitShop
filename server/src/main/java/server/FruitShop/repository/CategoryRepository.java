package server.FruitShop.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import server.FruitShop.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {
    @Query("SELECT DISTINCT c " +
            "FROM Category c " +
            "WHERE LOWER(c.categoryName) LIKE LOWER(CONCAT('%', :categoryName, '%')) ")
    Page<Category> findByCategoryName(@Param("categoryName") String categoryName, Pageable pageable);
}
