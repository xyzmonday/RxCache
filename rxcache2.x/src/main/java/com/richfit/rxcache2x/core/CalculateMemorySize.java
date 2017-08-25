package com.richfit.rxcache2x.core;

import android.graphics.Bitmap;


import com.richfit.rxcache2x.exceptions.NotImplementException;
import com.richfit.rxcache2x.utils.FileUtil;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public final class CalculateMemorySize {

    public interface SizeOf {
        long sizeOf();
    }

    private CalculateMemorySize() {
    }

    /**
     * 计算大小
     */
    public static long sizeOf(SizeOf obj) {
        if (obj == null) {
            return 0;
        }

        return obj.sizeOf();
    }

    /**
     * 计算大小
     */
    public static long sizeOf(Serializable serial)  {
        if (serial == null) {
            return 0;
        }
        long size = -1;
        ByteArrayOutputStream baos = null;
        ObjectOutputStream oos = null;
        try {
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(serial);
            oos.flush();  //缓冲流
            size = baos.size();
        } catch (FileNotFoundException e) {
            throw new NotImplementException(e.getMessage());
        } catch (NotSerializableException e) {
            throw new NotImplementException(e.getMessage() + " does not implement the MemorySizeOf.SizeOf.");
        } catch (IOException e) {
        } finally {
            FileUtil.close(oos);
            FileUtil.close(baos);
        }
        return size;
    }

    /**
     * 计算大小
     */
    public static long sizeOf(Bitmap bitmap) {
        if (bitmap == null) {
            return 0;
        }

        long size = -1;
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            size = baos.size();
        } finally {
            FileUtil.close(baos);
        }
        return size;
    }

}