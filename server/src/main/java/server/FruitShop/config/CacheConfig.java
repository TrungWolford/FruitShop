package server.FruitShop.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import server.FruitShop.dto.response.Product.ProductResponse;

import java.time.Duration;
import java.util.List;

/**
 * CacheConfig — khai báo Caffeine cache beans.
 *
 * Hai nhóm bean:
 *
 * 1. Raw Caffeine Cache<K,V>  → inject thủ công vào TwoLevelCacheService
 *    - productLocalCache       (TTL 5 phút, max 10.000)
 *
 * 2. CacheManager (Primary)   → dùng bởi @Cacheable / @CacheEvict
 *    - "products"    TTL 5  phút  max 10.000  (product detail + top-products)
 *    - "top-products" TTL 5 phút  max 1.000
 *    - "categories"  TTL 10 phút  max 500
 *    - "user-profile" TTL 10 phút max 5.000
 */
@Configuration
public class CacheConfig {

    // =========================================================================
    // Raw Caffeine bean — inject vào ProductCacheService (L1 thủ công)
    // =========================================================================

    /**
     * L1 Cache cho product detail.
     * key = "fruitshop:product:{id}" | TTL = 5 phút | max = 10.000 entries
     */
    @Bean("productLocalCache")
    public Cache<String, ProductResponse> productLocalCache() {
        return Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterWrite(Duration.ofMinutes(5))
                .recordStats()
                .build();
    }

    // =========================================================================
    // CacheManager — dùng cho @Cacheable / @CacheEvict
    // =========================================================================

    /**
     * Primary CacheManager với per-cache TTL & size độc lập.
     * Dùng SimpleCacheManager để đăng ký từng CaffeineCache riêng.
     */
    @Bean
    @Primary
    public CacheManager caffeineCacheManager() {
        SimpleCacheManager manager = new SimpleCacheManager();

        manager.setCaches(List.of(
                // Product detail — TTL 5 phút
                buildCache("products",     Duration.ofMinutes(5),  10_000),

                // Top sold products — TTL 5 phút (list thay đổi theo stock)
                buildCache("top-products", Duration.ofMinutes(5),   1_000),

                // Category — thay đổi ít → TTL 10 phút
                buildCache("categories",   Duration.ofMinutes(10),    500),

                // User profile — thay đổi ít → TTL 10 phút
                buildCache("user-profile", Duration.ofMinutes(10),  5_000)
        ));

        return manager;
    }

    // =========================================================================
    // HELPER
    // =========================================================================

    private CaffeineCache buildCache(String name, Duration ttl, long maxSize) {
        return new CaffeineCache(name,
                Caffeine.newBuilder()
                        .maximumSize(maxSize)
                        .expireAfterWrite(ttl)
                        .recordStats()
                        .build());
    }
}
