package com.richfit.rxcache2x.core.disk.converter;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

/**
 * 通用转换器
 *
 * @version monday 2016-04
 */
public interface IDiskConverter {

    /**
     * 读取
     *
     * @param source
     * @return
     */
    Object load(InputStream source, Type type);

    /**
     * 写入
     *
     * @param sink
     * @param data
     * @return
     */
    boolean writer(OutputStream sink, Object data);

}
