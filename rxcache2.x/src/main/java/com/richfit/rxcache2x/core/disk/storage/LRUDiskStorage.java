package com.richfit.rxcache2x.core.disk.storage;

import com.google.gson.reflect.TypeToken;
import com.jakewharton.disklrucache.DiskLruCache;
import com.richfit.rxcache2x.core.disk.converter.IDiskConverter;
import com.richfit.rxcache2x.utils.CommonUtil;
import com.richfit.rxcache2x.utils.FileUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Created by monday on 2017/1/2.
 */

public class LRUDiskStorage implements IDiskStorage {

    private DiskLruCache mDiskLruCache;
    private final IDiskConverter mDiskConverter;

    public LRUDiskStorage(IDiskConverter diskConverter, File storageDir, int appVersion, long diskMaxSize) {
        this.mDiskConverter = diskConverter;
        try {
            this.mDiskLruCache = DiskLruCache.open(storageDir, appVersion, 1, diskMaxSize);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public <T> T load(String key) {
        if (CommonUtil.isEmpty(key)) {
            return null;
        }
        InputStream source = null;
        try {
            DiskLruCache.Snapshot snapShot = mDiskLruCache.get(key);
            if (snapShot != null) {
                source = snapShot.getInputStream(0);
                if (source != null) {
                    T value = (T) mDiskConverter.load(source,new TypeToken<T>() {}.getType());
                    return value;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            FileUtil.close(source);
        }
        return null;
    }

    @Override
    public <T> boolean save(String key, T value) {
        if (CommonUtil.isEmpty(key)) {
            return false;
        }
        if (value == null) {
            return remove(key);
        }
        OutputStream outputStream = null;
        try {
            DiskLruCache.Editor editor = mDiskLruCache.edit(key);
            if (editor == null) {
                return false;
            }
            outputStream = editor.newOutputStream(0);
            if (outputStream != null) {
                mDiskConverter.writer(outputStream, value);
                editor.commit();
                mDiskLruCache.flush();
                return true;
            }
            editor.abort();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            FileUtil.close(outputStream);
        }
        return false;
    }

    boolean containsKey(String key) {
        try {
            return mDiskLruCache.get(key) != null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void close() {
        if (!mDiskLruCache.isClosed()) {
            try {
                mDiskLruCache.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean remove(String key) {
        try {
            return mDiskLruCache.remove(key);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean clear() {
        try {
            mDiskLruCache.delete();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public long getTotalSize() {
        return mDiskLruCache.size();
    }

    @Override
    public long getTotalQuantity() {
        return mDiskLruCache.getDirectory().listFiles().length;
    }
}
