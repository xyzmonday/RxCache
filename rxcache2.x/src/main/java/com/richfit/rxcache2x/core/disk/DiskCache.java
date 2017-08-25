package com.richfit.rxcache2x.core.disk;


import com.richfit.rxcache2x.core.ACache;
import com.richfit.rxcache2x.core.CacheEntry;
import com.richfit.rxcache2x.core.CacheTarget;
import com.richfit.rxcache2x.core.disk.journal.IDiskJournal;
import com.richfit.rxcache2x.core.disk.storage.IDiskStorage;
import com.richfit.rxcache2x.utils.CommonUtil;

import java.util.Collection;


/**
 * 磁盘缓存
 * @version monday 2016-04
 */
public final class DiskCache extends ACache {

    private final IDiskStorage mStorage;
    private final IDiskJournal mJournal;

    public DiskCache(IDiskStorage storage,
                     IDiskJournal journal,
                     long maxSize,
                     long maxQuantity) {
        super(maxSize, maxQuantity);
        this.mStorage = storage;
        this.mJournal = journal;
    }

    /**
     * 读取
     * @param key
     * @param <T>
     * @return
     */

    @Override
    protected <T> T doLoad(String key) {
        return  mStorage.load(key);
    }

    /**
     * 保存
     * @param maxAge 最大有效期时长（单位：毫秒）
     */
    @Override
    protected <T> boolean doSave(String key, T value, int maxAge, CacheTarget target) {
        if (target == null || target == CacheTarget.NONE || target == CacheTarget.Memory) {
            return true;
        }
       if(mStorage.save(key,value)) {
           mJournal.put(key, new CacheEntry(key, maxAge, target));
           return true;
       }
        return false;
    }

    @Override
    protected boolean isExpiry(String key) {
        CacheEntry entry = mJournal.get(key);
        return entry == null || entry.isExpiry();
    }

    @Override
    protected boolean doContainsKey(String key) {
        return mJournal.containsKey(key);
    }

    @Override
    protected boolean doRemove(String key) {
        return mStorage.remove(key) && mJournal.remove(key);
    }

    @Override
    protected boolean doClear() {
        return mStorage.clear() && mJournal.clear();
    }

    @Override
    public Collection<CacheEntry> snapshot() {
        return mJournal.snapshot();
    }

    @Override
    public String getLoseKey() {
        return mJournal.getLoseKey();
    }

    @Override
    public long getTotalSize() {
        long size = mStorage.getTotalSize();
        CommonUtil.checkNotLessThanZero(size);
        return size;
    }

    @Override
    public long getTotalQuantity() {
        long quantity = mStorage.getTotalQuantity();
        CommonUtil.checkNotLessThanZero(quantity);
        return quantity;
    }

}
