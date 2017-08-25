package com.richfit.rxcache2x.core;

/**
 * 缓存目标
 *
 * @version monday 2016-04
 */
public enum CacheTarget {

    NONE,
    Memory,
    Disk,
    MemoryAndDisk;

    public boolean supportMemory() {
        return this == Memory || this == MemoryAndDisk;
    }

    public boolean supportDisk() {
        return this == Disk || this == MemoryAndDisk;
    }

}
