package com.richfit.rxcache2x.core.disk.journal;



import com.richfit.rxcache2x.core.CacheEntry;

import java.io.Closeable;
import java.util.Collection;

/**
 * 磁盘缓存日志
 * @version monday 2016-04
 */
public interface IDiskJournal extends Closeable {

    CacheEntry get(String key);

    void put(String key, CacheEntry entry);

    boolean containsKey(String key);

    /**
     * 获取准备丢弃的Key
     * @return 准备丢弃的Key（如存储空间不足时，需要清理）
     */
    String getLoseKey();

    boolean remove(String key);

    boolean clear();

    Collection<CacheEntry> snapshot();

}
