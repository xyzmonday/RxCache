package com.richfit.rxcache2x.core.memory.storage;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.richfit.rxcache2x.core.CalculateMemorySize;
import com.richfit.rxcache2x.utils.CommonUtil;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;

/**
 * 基于LRU算法的内存缓存
 * Created by monday on 2017/1/2.
 */

public class LRUMemoryStorage implements IMemoryStorage {

    private final LruCache<String, Object> mStorage;
    private final HashSet<String> mKeySet;

    public LRUMemoryStorage(final int cacheSize) {
        mKeySet = new HashSet<>();
        this.mStorage = new LruCache<String, Object>(cacheSize) {
            @Override
            protected int sizeOf(String key, Object value) {
                return super.sizeOf(key, value);
            }
        };
    }

    @Override
    public Object load(String key) {
        if (CommonUtil.isEmpty(key)) {
            return null;
        }
        return mStorage.get(key);
    }

    @Override
    public boolean save(String key, Object value) {
        if (CommonUtil.isEmpty(key)) {
            return false;
        }
        boolean contains = mKeySet.contains(key);
        mKeySet.add(key);
        Object object = mStorage.put(key, value);
        if(contains && object == null) {
            return false;//说明没有覆盖掉老的value
        }
        return true;
    }

    @Override
    public void close() {

    }

    @Override
    public boolean remove(String key) {
        if (CommonUtil.isEmpty(key)) {
            return false;
        }
        mKeySet.remove(key);
        return mStorage.remove(key) != null;
    }

    public boolean containsKey(String key) {
        return mKeySet.contains(key);
    }

    @Override
    public boolean clear() {
        mKeySet.clear();
        mStorage.evictAll();
        return true;
    }

    @Override
    public long getTotalSize() {
        Map<String, Object> snapshot = mStorage.snapshot();
        long totalSize = 0;
        for (Object value : snapshot.values()) {
            totalSize += countSize(value);
        }
        return totalSize;
    }

    private static long countSize(Object value) {
        if (value == null) {
            return 0;
        }

        long size = 1;
        if (value instanceof CalculateMemorySize.SizeOf) {

            size = CalculateMemorySize.sizeOf((CalculateMemorySize.SizeOf) value);
        } else if (value instanceof Bitmap) {
            size = CalculateMemorySize.sizeOf((Bitmap) value);
        } else if (value instanceof Iterable) {
            for (Object item : ((Iterable) value)) {
                size += countSize(item);
            }
        } else if (value instanceof Serializable) {
            size = CalculateMemorySize.sizeOf((Serializable) value);
        }
        return size;
    }


    @Override
    public long getTotalQuantity() {
        return mStorage.snapshot().size();
    }
}
