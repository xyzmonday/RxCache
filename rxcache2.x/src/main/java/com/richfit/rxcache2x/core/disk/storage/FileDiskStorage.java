package com.richfit.rxcache2x.core.disk.storage;


import com.google.gson.reflect.TypeToken;
import com.richfit.rxcache2x.core.disk.converter.IDiskConverter;
import com.richfit.rxcache2x.exceptions.ArgumentException;
import com.richfit.rxcache2x.exceptions.NotFoundException;
import com.richfit.rxcache2x.utils.CommonUtil;
import com.richfit.rxcache2x.utils.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 文件形式的磁盘存储
 *
 * @version monday 2016-06
 */
public class FileDiskStorage implements IDiskStorage {
    private final File mStorageDir;
    private final IDiskConverter mDiskConverter;

    /**
     * @param storageDir 磁盘存储根目录
     */
    public FileDiskStorage(IDiskConverter diskConverter, File storageDir) {
        if (storageDir == null || !storageDir.isDirectory()) {
            throw new NotFoundException("‘storageDir’ not found.");
        }
        if (diskConverter == null) {
            throw new ArgumentException("disConverter is null");
        }
        this.mStorageDir = storageDir;
        this.mDiskConverter = diskConverter;
    }

    @Override
    public <T> T load(String key) {
        if (CommonUtil.isEmpty(key)) {
            return null;
        }
        File file = new File(mStorageDir, key);
        if (!file.exists() || !file.isFile()) {
            return null;
        }
        FileInputStream source = null;
        try {
            T value = null;
            source = new FileInputStream(file);
            if (source != null) {
                value = (T) mDiskConverter.load(source, new TypeToken<T>() {
                }.getType());
                return value;
            }
            return null;
        } catch (FileNotFoundException e) {
            throw new NotFoundException(e);
        } finally {
            FileUtil.close(source);
        }
    }

    @Override
    public <T> boolean save(String key, T value) {
        if (CommonUtil.isEmpty(key)) {
            return false;
        }
        if (value == null) {
            return remove(key);
        }
        File file = new File(mStorageDir, key);
        if (!exists(file) || file.isDirectory()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            if (outputStream != null) {
                mDiskConverter.writer(outputStream, value);
                return true;
            }
            return false;
        } catch (Exception e) {
            throw new NotFoundException(e);
        }
    }


    @Override
    public void close() {
        // TODO Nothing
    }

    @Override
    public boolean remove(String key) {
        return !CommonUtil.isEmpty(key) && delete(new File(mStorageDir, key));
    }

    @Override
    public boolean clear() {
        try {
            deleteContents(mStorageDir);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public long getTotalSize() {
        return countSize(mStorageDir);
    }

    @Override
    public long getTotalQuantity() {
        return mStorageDir.list().length;
    }


    public boolean exists(File file) {
        return file != null && file.exists();
    }

    private long countSize(File file) {
        return file.length();
    }

    public boolean delete(File file) {
        if (file == null) {
            return false;
        }
        // If delete() fails, make sure it's because the file didn't exist!
        return file.delete() || !file.exists();

    }

    private void deleteContents(File directory) throws IOException {
        File[] files = directory.listFiles();
        if (files == null) {
            throw new IOException("not a readable directory: " + directory);
        }
        for (File file : files) {
            if (file.isDirectory()) {
                deleteContents(file);
            }
            if (!file.delete()) {
                throw new IOException("failed to delete " + file);
            }
        }
    }
}
