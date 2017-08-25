package com.richfit.rxcache2x.core.disk.converter;



import com.richfit.rxcache2x.utils.FileUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

/**
 * 序列化-数据转换器
 * @version monday 2016-07
 */
public class SerializableDiskConverter implements IDiskConverter {

    @Override
    public Object load(InputStream source, Type type) {
        Object value = null;
        ObjectInputStream oin = null;
        try {
            oin = new ObjectInputStream(source);
            value = oin.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            FileUtil.close(oin);
        }
        return value;
    }

    @Override
    public boolean writer(OutputStream sink, Object data) {
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(sink);
            oos.writeObject(data);
            oos.flush(); //缓冲流
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            FileUtil.close(oos);
        }
    }

}
