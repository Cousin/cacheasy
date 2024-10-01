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

    private final Map<Cached, C> cachedMap = new HashMap<>();

    public abstract C createCache(Cached cached);

    public abstract void put(Cached cached, String key, Object value);

    public abstract Object get(Cached cached, String key);

    public abstract void remove(Cached cached, String key);

    public abstract void clear(Cached cached);

    public abstract boolean containsKey(Cached cached, String key);

    public abstract long size(Cached cached);

    public void clear() {
        cachedMap.clear();
    }

    public C getCache(Cached cachedd) {
        return cachedMap.computeIfAbsent(cachedd, this::createCache);
    }

}
