package com.richfit.rxcache2x.core.disk.storage;

import java.io.Closeable;

/**
 * 磁盘存储
 *
 * @version monday 2016-04
 */
public interface IDiskStorage extends Closeable {

    /**
     * 加载数据源
     *
     * @param key
     * @return
     */
    <T> T load(String key);

    /**
     * 保存数据
     *
     * @param key
     */
    <T> boolean save(String key, T value);

    /**
     * 关闭
     */
    @Override
    void close();

    /**
     * 删除缓存
     *
     * @param key
     */
    boolean remove(String key);

    /**
     * 清空缓存
     */
    boolean clear();

    /**
     * 缓存总大小
     *
     * @return 单位:byte
     */
    long getTotalSize();

    /**
     * 缓存总数目
     *
     * @return 单位:缓存个数
     */
    long getTotalQuantity();

}
