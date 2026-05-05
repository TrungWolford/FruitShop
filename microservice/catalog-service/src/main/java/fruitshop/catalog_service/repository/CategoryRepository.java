package fruitshop.catalog_service.repository;

import fruitshop.catalog_service.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, String> {
    Optional<Category> findByCategoryName(String categoryName);
    Page<Category> findByCategoryNameContainingIgnoreCase(String keyword, Pageable pageable);
}
