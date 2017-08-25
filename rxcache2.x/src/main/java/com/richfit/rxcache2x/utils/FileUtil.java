package com.richfit.rxcache2x.utils;

/**
 * Created by monday on 2016/1/26.
 */

import android.content.Context;
import android.os.Environment;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;


public class FileUtil {



    /**
     * 判断SD是否存在
     */
    public static boolean isAvailable() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) ||
                !Environment.isExternalStorageRemovable()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 删除某目录下的图片
     *
     * @param imageDir
     * @param fileName
     * @return
     */
    public static boolean deleteImage(String imageDir, String fileName) {
        boolean isSuccess = false;
        try {
            File file = new File(imageDir + File.separator + fileName);
            if (!file.exists()) {
                return false;
            }
            isSuccess = file.delete();
        } catch (Exception e) {
            e.printStackTrace();
            return isSuccess;
        }
        return isSuccess;
    }


    /**
     * 删除空目录
     *
     * @param dir 将要删除的目录路径
     */
    public static void doDeleteEmptyDir(String dir) {
        boolean success = (new File(dir)).delete();
        if (success) {
            System.out.println("删除空目录成功: " + dir);
        } else {
            System.out.println("删除空目录失败" + dir);
        }
    }

    /**
     * 递归删除目录下的所有文件及子目录下所有文件
     *
     * @param dir 将要删除的文件目录
     * @return 返回true说明删除成功。
     */
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            //递归删除目录中的子目录下
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }

    public static void close(Closeable close) {
        if (close != null) {
            try {
                closeThrowException(close);
            } catch (IOException ignored) {
            }
        }
    }

    public static void closeThrowException(Closeable close) throws IOException {
        if (close != null) {
            close.close();
        }
    }

}