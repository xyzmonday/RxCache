package com.richfit.rxcache2x.core.memory.storage;

import android.graphics.Bitmap;

import com.richfit.rxcache2x.core.CalculateMemorySize;
import com.richfit.rxcache2x.utils.CommonUtil;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 简单的内存存储
 * @version monday 2016-06
 */
public class SimpleMemoryStorage implements IMemoryStorage {

    private Map<String, Object> mStorageMap;

    public SimpleMemoryStorage() {
        this.mStorageMap = new HashMap<>();
    }

    @Override
    public Object load(String key) {
        if (CommonUtil.isEmpty(key)) {
            return null;
        }
        return mStorageMap.get(key);
    }

    @Override
    public boolean save(String key, Object value) {
        if (CommonUtil.isEmpty(key)) {
            //如果key为空，那么不允许缓存
            return false;
        }
        boolean contains = mStorageMap.containsKey(key);
        Object object = mStorageMap.put(key, value);
        if(contains && object == null) {
            return false;//说明没有覆盖掉老的value
        }
        return true;
    }

    @Override
    public void close() {
        // TODO Nothing
    }

    @Override
    public boolean remove(String key) {
        if (CommonUtil.isEmpty(key)) {
            return true;
        }
        return mStorageMap.remove(key) != null;
    }

    @Override
    public boolean clear() {
        mStorageMap.clear();
        return true;
    }

    @Override
    public long getTotalSize() {
        long totalSize = 0;
        for (Object value : mStorageMap.values()) {
            totalSize += countSize(value);
        }
        return totalSize;
    }

    private static long countSize(Object value) {
        if (value == null) {
            return 0;
        }

        // FIXME 更优良的内存大小算法
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
        return mStorageMap.size();
    }

}
