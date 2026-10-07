package server.FruitShop.service.Impl;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import server.FruitShop.dto.response.Product.ProductResponse;
import server.FruitShop.service.TwoLevelCacheService;

import java.time.Duration;

/**
 * Two-level cache cho Product Detail.
 *   L1 = Caffeine  (in-process, siêu nhanh, TTL 5 phút)
 *   L2 = Redis     (distributed, TTL 10 phút)
 *
 * Flow GET:
 *   L1 hit → return
 *   L1 miss → L2 hit → warm L1 → return
 *             L2 miss → return null (caller fetch DB rồi gọi put)
 *
 * FIX: thêm @Service, dùng RedisTemplate<String, Object> thay vì
 *      RedisTemplate<String, ProductResponse> để match đúng bean.
 */
@Slf4j
@Service("productCacheService")
public class ProductCacheService implements TwoLevelCacheService<ProductResponse> {

    private static final String PREFIX = "fruitshop:product:";
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(10);

    /** Raw Caffeine cache — inject by bean name để tránh nhầm lẫn. */
    private final Cache<String, ProductResponse> localCache;

    /** Dùng RedisTemplate<String, Object> — đây là bean đã khai báo trong RedisConfig. */
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public ProductCacheService(
            @Qualifier("productLocalCache") Cache<String, ProductResponse> localCache,
            RedisTemplate<String, Object> redisTemplate) {
        this.localCache = localCache;
        this.redisTemplate = redisTemplate;
    }

    // =========================================================================
    // TwoLevelCacheService<ProductResponse>
    // =========================================================================

    @Override
    public ProductResponse get(String productId) {
        String key = PREFIX + productId;

        // L1: Caffeine
        ProductResponse local = localCache.getIfPresent(key);
        if (local != null) {
            log.debug("[Cache L1 HIT] product:{}", productId);
            return local;
        }

        // L2: Redis
        Object raw = redisTemplate.opsForValue().get(key);
        if (raw instanceof ProductResponse redis) {
            log.debug("[Cache L2 HIT] product:{} → warm L1", productId);
            localCache.put(key, redis);   // nạp ngược lại L1
            return redis;
        }

        log.debug("[Cache MISS] product:{}", productId);
        return null;
    }

    @Override
    public void put(String productId, ProductResponse value) {
        put(productId, value, DEFAULT_TTL);
    }

    @Override
    public void put(String productId, ProductResponse value, Duration ttl) {
        String key = PREFIX + productId;

        // L2 trước (distributed)
        redisTemplate.opsForValue().set(key, value, ttl);

        // L1
        localCache.put(key, value);

        log.debug("[Cache PUT] product:{} ttl={}", productId, ttl);
    }

    @Override
    public void evict(String productId) {
        String key = PREFIX + productId;
        redisTemplate.delete(key);
        localCache.invalidate(key);
        log.debug("[Cache EVICT] product:{}", productId);
    }
}
