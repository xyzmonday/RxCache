package com.richfit.rxcache2x.core.memory.journal;

/**
 * LRU缓存日志
 * @version monday 2016-07
 */
public class LRUMemoryJournal extends BasicMemoryJournal {

    @Override
    public String getLoseKey() {
        return getKeyValues().entrySet().iterator().next().getKey();
    }

}
