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

    public abstract void put(C cache, String key, Object value);

    public abstract Object get(C cache, String key);

    public abstract void remove(C cache, String key);

    public abstract void clear(C cache);

    public abstract boolean containsKey(C cache, String key);

    public abstract long size(C cache);

    public void clear() {
        cachedMap.clear();
    }

    public final C getCache(Cached cached) {
        return cachedMap.computeIfAbsent(cached, this::createCache);
    }

}
