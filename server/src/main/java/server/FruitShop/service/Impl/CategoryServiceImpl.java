package server.FruitShop.service.Impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import server.FruitShop.dto.request.Category.CreateCategoryRequest;
import server.FruitShop.dto.request.Category.UpdateCategoryRequest;
import server.FruitShop.dto.response.Category.CategoryResponse;
import server.FruitShop.entity.Category;
import server.FruitShop.repository.CategoryRepository;
import server.FruitShop.service.CategoryService;
import server.FruitShop.exception.ResourceNotFoundException;

import java.time.Duration;
import java.util.List;

/**
 * CategoryServiceImpl — cache strategy: Caffeine (L1) + Redis (L2).
 *
 * getByCategoryId   → L1 @Cacheable("categories") + L2 Redis thủ công
 *                     key: "fruitshop:category:{id}"
 *
 * getAllCategory     → L1 @Cacheable("categories") với key "all::{page}:{size}"
 *                     (không cache vào Redis vì page có nhiều biến thể)
 *
 * Write ops         → @CacheEvict để xóa L1 + thủ công xóa L2
 */
@Slf4j
@Service
public class CategoryServiceImpl implements CategoryService {

    private static final String REDIS_PREFIX   = "fruitshop:category:";
    private static final Duration CATEGORY_TTL = Duration.ofMinutes(10);

    private final CategoryRepository categoryRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository,
                               RedisTemplate<String, Object> redisTemplate) {
        this.categoryRepository = categoryRepository;
        this.redisTemplate = redisTemplate;
    }

    // =========================================================================
    // READ
    // =========================================================================

    /**
     * Lấy tất cả category — cache L1 (Caffeine) theo page/size.
     * Không cache Redis vì pageable có nhiều biến thể → không worth it.
     */
    @Override
    @Cacheable(cacheNames = "categories", key = "'all::' + #pageable.pageNumber + '::' + #pageable.pageSize")
    public Page<CategoryResponse> getAllCategory(Pageable pageable) {
        log.debug("[Cache MISS] getAllCategory page={}", pageable.getPageNumber());
        return categoryRepository.findAll(pageable)
                .map(CategoryResponse::fromEntity);
    }

    /**
     * Lấy category theo id — two-level: L1 Caffeine (@Cacheable) + L2 Redis thủ công.
     */
    @Override
    @Cacheable(cacheNames = "categories", key = "#categoryId")
    public CategoryResponse getByCategoryId(String categoryId) {
        // L2: Redis
        String redisKey = REDIS_PREFIX + categoryId;
        Object raw = redisTemplate.opsForValue().get(redisKey);
        if (raw instanceof CategoryResponse cached) {
            log.debug("[Cache L2 HIT] category:{}", categoryId);
            return cached;
        }

        // DB
        log.debug("[Cache MISS] getByCategoryId:{} → query DB", categoryId);
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));

        CategoryResponse response = CategoryResponse.fromEntity(category);

        // Nạp L2
        redisTemplate.opsForValue().set(redisKey, response, CATEGORY_TTL);

        return response;
    }

    @Override
    @Cacheable(cacheNames = "categories", key = "'search::' + #keyword + '::' + #pageable.pageNumber")
    public Page<CategoryResponse> searchCategory(String keyword, Pageable pageable) {
        log.debug("[Cache MISS] searchCategory keyword={}", keyword);
        return categoryRepository.findByCategoryName(keyword, pageable)
                .map(CategoryResponse::fromEntity);
    }

    // =========================================================================
    // WRITE — evict cache sau mỗi thao tác ghi
    // =========================================================================

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "categories", key = "#result.categoryId"),   // evict single
            @CacheEvict(cacheNames = "categories", allEntries = true)             // evict all-page
    })
    public CategoryResponse createCategoryId(CreateCategoryRequest request) {
        Category category = new Category();
        category.setCategoryName(request.getCategoryName());
        category.setStatus(request.getStatus());
        categoryRepository.saveAndFlush(category);
        log.debug("[Cache EVICT] after createCategory");
        return CategoryResponse.fromEntity(category);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "categories", key = "#categoryId"),
            @CacheEvict(cacheNames = "categories", allEntries = true)
    })
    public CategoryResponse updateCategoryId(UpdateCategoryRequest request, String categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));

        category.setCategoryName(request.getCategoryName());
        category.setStatus(request.getStatus());
        categoryRepository.saveAndFlush(category);

        // Evict L2 Redis
        redisTemplate.delete(REDIS_PREFIX + categoryId);
        log.debug("[Cache EVICT] after updateCategory:{}", categoryId);

        return CategoryResponse.fromEntity(category);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "categories", key = "#categoryId"),
            @CacheEvict(cacheNames = "categories", allEntries = true)
    })
    public void deleteCategoryId(String categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));

        categoryRepository.delete(category);

        // Evict L2 Redis
        redisTemplate.delete(REDIS_PREFIX + categoryId);
        log.debug("[Cache EVICT] after deleteCategory:{}", categoryId);
    }
}
