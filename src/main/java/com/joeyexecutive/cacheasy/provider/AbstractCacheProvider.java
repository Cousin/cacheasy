package com.joeyexecutive.cacheasy.provider;

import com.joeyexecutive.cacheasy.annotation.Cached;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public abstract class AbstractCacheProvider<C> {

    private final String name;

    private final Map<Cached, C> cacheMap = new HashMap<>();

    public abstract C createCache(Cached ms);

    public abstract void put(Cached cache, String key, Object value);

    public abstract Object get(Cached cache, String key);

    public abstract void remove(Cached cache, String key);

    public abstract void clear(Cached cache);

    public abstract boolean containsKey(Cached cache, String key);

    public abstract long size(Cached cache);

    public void clear() {
        cacheMap.clear();
    }

    public C getCache(Cached ms) {
        return cacheMap.computeIfAbsent(ms, this::createCache);
    }

}
