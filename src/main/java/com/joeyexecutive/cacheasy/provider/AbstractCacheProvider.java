package com.joeyexecutive.cacheasy.provider;

import com.joeyexecutive.cacheasy.annotation.Cached;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@RequiredArgsConstructor
public abstract class AbstractCacheProvider<C> {

    private final String name;

    // Populated lazily from caller threads (via the aspect), so it must be concurrent.
    private final Map<Cached, C> cachedMap = new ConcurrentHashMap<>();

    public abstract C createCache(Cached cached);

    public abstract void put(C cache, String key, Object value);

    public abstract Object get(C cache, String key);

    public abstract void remove(C cache, String key);

    public abstract void clear(C cache);

    /**
     * Default membership check: present iff a non-expired value is cached. Backends with a
     * cheaper or more precise native check may override.
     */
    public boolean containsKey(C cache, String key) {
        return get(cache, key) != null;
    }

    public abstract long size(C cache);

    public void clear() {
        cachedMap.clear();
    }

    public final C getCache(Cached cached) {
        return cachedMap.computeIfAbsent(cached, this::createCache);
    }

}
