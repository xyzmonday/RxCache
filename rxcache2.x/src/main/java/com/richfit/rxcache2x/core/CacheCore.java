package com.richfit.rxcache2x.core;


import android.util.Log;

import com.richfit.rxcache2x.core.disk.DiskCache;
import com.richfit.rxcache2x.core.disk.journal.IDiskJournal;
import com.richfit.rxcache2x.core.disk.storage.IDiskStorage;
import com.richfit.rxcache2x.core.memory.MemoryCache;
import com.richfit.rxcache2x.core.memory.journal.IMemoryJournal;
import com.richfit.rxcache2x.core.memory.storage.IMemoryStorage;
import com.richfit.rxcache2x.utils.CommonUtil;

/**
 * 缓存核心。该类持有内存和磁盘缓存器，
 * 通过它能够操作内存和磁盘缓存。
 *
 * @version monday 2016-04
 */
public class CacheCore {

    private MemoryCache memory;
    private DiskCache disk;

    private CacheCore(MemoryCache memory, DiskCache disk) {
        this.memory = CommonUtil.checkNotNull(memory);
        this.disk = CommonUtil.checkNotNull(disk);
    }


    /**
     * 读取
     */
    public <T> T load(String key) {
        if (memory != null) {
            T result =  memory.load(key);
            if (result != null) {
                Log.d("yff","内存缓存 = " + result);
                return result;
            }
        }

        if (disk != null) {
            T result = disk.load(key);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * 保存
     *
     * @param expires 有效期（单位：毫秒）
     */
    public <T> boolean save(String key, T value, int expires, CacheTarget target) {

        if (value == null) {
            return memory.remove(key) && disk.remove(key);
        }

        if (target.supportMemory() && memory != null) {
            memory.save(key, value, expires, target);
        }
        if (target.supportDisk() && disk != null) {
            return disk.save(key, value, expires, target);
        }

        return false;
    }

    /**
     * 是否包含
     *
     * @param key
     * @return
     */
    public boolean containsKey(String key) {
        if (memory != null) {
            if (memory.containsKey(key)) {
                return true;
            }
        }
        if (disk != null) {
            if (disk.containsKey(key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 删除缓存
     *
     * @param key
     */
    public void remove(String key) {
        if (memory != null) {
            memory.remove(key);
        }
        if (disk != null) {
            disk.remove(key);
        }
    }

    /**
     * 清空缓存
     */
    public void clear() {
        if (memory != null) {
            memory.clear();
        }
        if (disk != null) {
            disk.clear();
        }
    }


    /**
     * 构造器
     */
    public static class Builder {
        private IMemoryStorage memory;
        private IMemoryJournal memoryJournal;
        private long memoryMaxSize;
        private long memoryMaxQuantity;

        private IDiskStorage disk;
        private IDiskJournal diskJournal;
        private long diskMaxSize;
        private long diskMaxQuantity;

        public Builder() {
        }

        public Builder memory(IMemoryStorage memory) {
            this.memory = CommonUtil.checkNotNull(memory);
            return this;
        }

        public Builder memoryJournal(IMemoryJournal journal) {
            this.memoryJournal = CommonUtil.checkNotNull(journal);
            return this;
        }

        public Builder memoryMax(long memoryMaxSize, long memoryMaxQuantity) {
            this.memoryMaxSize = memoryMaxSize;
            this.memoryMaxQuantity = memoryMaxQuantity;
            return this;
        }

        public Builder memoryMaxSize(int memoryMaxSize) {
            this.memoryMaxSize = memoryMaxSize;
            return this;
        }

        public Builder memoryMaxQuantity(long memoryMaxQuantity) {
            this.memoryMaxQuantity = memoryMaxQuantity;
            return this;
        }


        public Builder disk(IDiskStorage disk) {
            this.disk = CommonUtil.checkNotNull(disk);
            return this;
        }

        public Builder diskJournal(IDiskJournal journal) {
            this.diskJournal = CommonUtil.checkNotNull(journal);
            return this;
        }

        public Builder diskMax(long diskMaxSize, long diskMaxQuantity) {
            this.diskMaxSize = diskMaxSize;
            this.diskMaxQuantity = diskMaxQuantity;
            return this;
        }


        public Builder diskMaxSize(long diskMaxSize) {
            this.diskMaxSize = diskMaxSize;
            return this;
        }

        public Builder diskMaxQuantity(long diskMaxQuantity) {
            this.diskMaxQuantity = diskMaxQuantity;
            return this;
        }

        public CacheCore create() {
            return new CacheCore(new MemoryCache(memory, memoryJournal, memoryMaxSize, memoryMaxQuantity),
                    new DiskCache(disk, diskJournal, diskMaxSize, diskMaxQuantity));
        }
    }

}
