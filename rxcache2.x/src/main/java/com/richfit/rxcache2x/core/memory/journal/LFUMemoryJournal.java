package com.richfit.rxcache2x.core.memory.journal;


import com.richfit.rxcache2x.core.CacheEntry;

/**
 * LFU缓存日志
 * @version monday 2016-07
 */
public class LFUMemoryJournal extends BasicMemoryJournal {

    @Override
    public String getLoseKey() {
        CacheEntry entry = null;
        for (CacheEntry item : getKeyValues().values()) {
            if (entry == null || entry.getUseCount() > item.getUseCount()) {
                entry = item;
            } else {
                if (entry.getUseCount() == item.getUseCount()
                    && entry.getLastUseTime() > item.getLastUseTime()) {
                    entry = item;
                }
            }
        }
        if (entry != null) {
            return entry.getKey();
        } else {
            return null;
        }
    }

}
