package com.richfit.rxcache2x.core;


import com.richfit.rxcache2x.utils.CommonUtil;

import java.util.Collection;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 缓存接口
 */
public abstract class ACache {

    protected final long mMaxSize;
    protected final long mMaxQuantity;
    private final ReadWriteLock mLock = new ReentrantReadWriteLock();

    public ACache(long maxSize, long maxQuantity) {
        this.mMaxSize = maxSize;
        this.mMaxQuantity = maxQuantity;
    }


    /**
     * 读取
     * @param key
     * @param <T>
     * @return
     */
    public final <T> T load(String key) {
        CommonUtil.checkNotNull(key);

        if (!containsKey(key)) {
            return null;
        }

        // 过期自动清理
        if (isExpiry(key)) {
            remove(key);
            return null;
        }

        mLock.readLock().lock();
        try {
            // 读取缓存
            return doLoad(key);
        } finally {
            mLock.readLock().unlock();
        }
    }

    /**
     * 读取
     * @param key
     * @param <T>
     * @return
     */
    protected abstract <T> T doLoad(String key);

    /**
     * 保存
     * @param maxAge 最大有效期时长（单位：秒）
     */
    public final <T> boolean save(String key, T value, int maxAge, CacheTarget target) {
        CommonUtil.checkNotNull(key);
        // 如果为空，那么意味者需要保存null，所以删除已经存在的就是保存当期的缓存
        if (value == null) {
            return remove(key);
        }

        boolean status = false;
        mLock.writeLock().lock();
        try {
            // 写入缓存
            status = doSave(key, value, maxAge, target);
        } finally {
            mLock.writeLock().unlock();
        }

        // 清理无用数据
        clearUnused();
        return status;
    }

    /**
     * 保存
     * @param maxAge 最长有效期时长（单位：毫秒）
     */
    protected abstract <T> boolean doSave(String key, T value, int maxAge, CacheTarget target);


    /**
     * 是否过期
     */
    protected abstract boolean isExpiry(String key);

    /**
     * 是否包含
     * @param key
     * @return
     */
    public final boolean containsKey(String key) {
        mLock.readLock().lock();
        try {
            return doContainsKey(key);
        } finally {
            mLock.readLock().unlock();
        }
    }

    /**
     * 删除缓存
     * @param key
     */
    public final boolean remove(String key) {
        mLock.writeLock().lock();
        try {
            return doRemove(key);
        } finally {
            mLock.writeLock().unlock();
        }
    }

    /**
     * 清空缓存
     */
    public final boolean clear() {
        mLock.writeLock().lock();
        try {
            return doClear();
        } finally {
            mLock.writeLock().unlock();
        }
    }

    /**
     * 是否包含
     * @param key
     * @return
     */
    protected abstract boolean doContainsKey(String key);

    /**
     * 删除缓存
     * @param key
     */
    protected abstract boolean doRemove(String key);

    /**
     * 清空缓存
     */
    protected abstract boolean doClear();


    /**
     * 日志快照
     */
    public abstract Collection<CacheEntry> snapshot();


    /**
     * 获取准备丢弃的Key
     * @return 准备丢弃的Key（如存储空间不足时，需要清理）
     */
    public abstract String getLoseKey();

    /**
     * 缓存大小
     * @return 单位:byte
     */
    public abstract long getTotalSize();

    /**
     * 缓存个数
     * @return 单位:个数
     */
    public abstract long getTotalQuantity();

    /**
     * 清理无用缓存
     */
    public void clearUnused() {
        // 清理过期
        for (CacheEntry entry : snapshot()) {
            if (entry.isExpiry()) {
                remove(entry.getKey());
            }
        }

        // 清理超出缓存
        if (mMaxSize != 0) {
            while (mMaxSize < getTotalSize()) {
                remove(getLoseKey());
            }
        }
        if (mMaxQuantity != 0) {
            while (mMaxQuantity < getTotalQuantity()) {
                remove(getLoseKey());
            }
        }
    }

}
