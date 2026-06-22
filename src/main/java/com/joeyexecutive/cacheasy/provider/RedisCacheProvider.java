package com.joeyexecutive.cacheasy.provider;

import com.joeyexecutive.cacheasy.annotation.CacheExpiryType;
import com.joeyexecutive.cacheasy.annotation.Cached;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.params.SetParams;
import redis.clients.jedis.resps.ScanResult;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Redis backend via Jedis — distributed/shared across processes. Each {@code @Cached} gets its
 * own key namespace ("cacheasy:{n}:") so clear()/size() can be scoped to it. Values are
 * Java-serialized and Base64-encoded, so cached return types must implement
 * {@link java.io.Serializable}.
 *
 * <p>Defaults to localhost:6379. For another endpoint, register a configured instance:
 * {@code Cacheasy.registerCacheProvider(redis.clients.jedis.Jedis.class, new RedisCacheProvider(host, port));}
 */
public class RedisCacheProvider extends AbstractCacheProvider<RedisCacheProvider.RedisCache> {

    private final String host;
    private final int port;
    private final AtomicLong namespaceCounter = new AtomicLong();
    private volatile JedisPool pool;

    public RedisCacheProvider() {
        this("localhost", 6379);
    }

    public RedisCacheProvider(String host, int port) {
        super("Redis");
        this.host = host;
        this.port = port;
    }

    @Override
    public RedisCache createCache(Cached cached) {
        long ttlMillis = Math.max(1L, cached.expiresTimeUnit().toMillis(cached.expiresAfter()));
        boolean refreshOnAccess = cached.expiryType() == CacheExpiryType.EXPIRES_AFTER_ACCESS;
        String prefix = "cacheasy:" + namespaceCounter.incrementAndGet() + ":";
        return new RedisCache(prefix, ttlMillis, refreshOnAccess);
    }

    @Override
    public void put(RedisCache cache, String key, Object value) {
        try (Jedis jedis = pool().getResource()) {
            jedis.set(cache.prefix + key, serialize(value), SetParams.setParams().px(cache.ttlMillis));
        }
    }

    @Override
    public Object get(RedisCache cache, String key) {
        try (Jedis jedis = pool().getResource()) {
            String stored = jedis.get(cache.prefix + key);
            if (stored == null) {
                return null;
            }
            if (cache.refreshOnAccess) {
                jedis.pexpire(cache.prefix + key, cache.ttlMillis);
            }
            return deserialize(stored);
        }
    }

    @Override
    public void remove(RedisCache cache, String key) {
        try (Jedis jedis = pool().getResource()) {
            jedis.del(cache.prefix + key);
        }
    }

    @Override
    public void clear(RedisCache cache) {
        try (Jedis jedis = pool().getResource()) {
            scanNamespace(jedis, cache.prefix, keys -> jedis.del(keys.toArray(new String[0])));
        }
    }

    @Override
    public boolean containsKey(RedisCache cache, String key) {
        try (Jedis jedis = pool().getResource()) {
            return jedis.exists(cache.prefix + key);
        }
    }

    @Override
    public long size(RedisCache cache) {
        try (Jedis jedis = pool().getResource()) {
            return scanNamespace(jedis, cache.prefix, null);
        }
    }

    /** Iterates every key in {@code prefix*}, optionally acting on each batch; returns the count. */
    private static long scanNamespace(Jedis jedis, String prefix, Consumer<List<String>> batchAction) {
        long count = 0;
        ScanParams params = new ScanParams().match(prefix + "*").count(100);
        String cursor = ScanParams.SCAN_POINTER_START;
        do {
            ScanResult<String> result = jedis.scan(cursor, params);
            List<String> keys = result.getResult();
            count += keys.size();
            if (batchAction != null && !keys.isEmpty()) {
                batchAction.accept(keys);
            }
            cursor = result.getCursor();
        } while (!ScanParams.SCAN_POINTER_START.equals(cursor));
        return count;
    }

    private JedisPool pool() {
        JedisPool current = pool;
        if (current == null) {
            synchronized (this) {
                current = pool;
                if (current == null) {
                    current = new JedisPool(host, port);
                    pool = current;
                }
            }
        }
        return current;
    }

    private static String serialize(Object value) {
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
             ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(value);
            out.flush();
            return Base64.getEncoder().encodeToString(bytes.toByteArray());
        } catch (IOException e) {
            throw new IllegalArgumentException("Cached value must be Serializable to use the Redis backend", e);
        }
    }

    private static Object deserialize(String encoded) {
        byte[] decoded = Base64.getDecoder().decode(encoded);
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(decoded))) {
            return in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException("Failed to deserialize cached value from Redis", e);
        }
    }

    /** A key namespace plus the expiry settings derived from one {@code @Cached}. */
    public static final class RedisCache {
        final String prefix;
        final long ttlMillis;
        final boolean refreshOnAccess;

        RedisCache(String prefix, long ttlMillis, boolean refreshOnAccess) {
            this.prefix = prefix;
            this.ttlMillis = ttlMillis;
            this.refreshOnAccess = refreshOnAccess;
        }
    }

}
