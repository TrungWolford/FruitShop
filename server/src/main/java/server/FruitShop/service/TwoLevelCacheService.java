package server.FruitShop.service;

import java.time.Duration;

/**
 * Two-level cache abstraction (L1 = Caffeine, L2 = Redis).
 * Generic type T: kiểu dữ liệu cần cache.
 *
 * Flow:
 *   get(key) → L1 hit → return
 *              L1 miss → L2 hit → warm L1 → return
 *                        L2 miss → return null (caller tự fetch DB, rồi gọi put)
 *
 *   put(key, value) → L2.set(TTL) + L1.put
 *   evict(key)      → L2.delete + L1.invalidate
 */
public interface TwoLevelCacheService<T> {

    /**
     * Lấy dữ liệu theo thứ tự L1 → L2.
     * @return value hoặc null nếu cả hai đều miss
     */
    T get(String key);

    /**
     * Lưu vào cả L1 và L2 với TTL mặc định.
     */
    void put(String key, T value);

    /**
     * Lưu vào cả L1 và L2 với TTL tùy chỉnh.
     */
    void put(String key, T value, Duration ttl);

    /**
     * Xóa cache ở cả L1 và L2.
     */
    void evict(String key);
}
