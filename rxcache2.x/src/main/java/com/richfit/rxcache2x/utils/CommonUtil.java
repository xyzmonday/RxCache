package com.richfit.rxcache2x.utils;

import com.richfit.rxcache2x.exceptions.ArgumentException;
import com.richfit.rxcache2x.exceptions.InstanceException;
import com.richfit.rxcache2x.exceptions.NullException;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.UUID;

import okhttp3.Request;
import okio.Buffer;
import okio.ByteString;

/**
 * Created by monday on 2016/12/30.
 */

public class CommonUtil {
    private CommonUtil() {

    }

    /**
     * 不为空
     */
    public static <T> T checkNotNull(T obj) {
        if (obj == null) {
            throw new NullException("Can not be empty.");
        }
        return obj;
    }

    /**
     * 不小于0
     */
    public static long checkNotLessThanZero(long number) {
        if (number < 0) {
            throw new ArgumentException("Can not be < 0.");
        }

        return number;
    }

    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    private static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    public static void checkOffsetAndCount(int arrayLength, int offset, int count) {
        if ((offset | count) < 0 || offset > arrayLength || arrayLength - offset < count) {
            throw new ArrayIndexOutOfBoundsException("length=" + arrayLength
                    + "; regionStart=" + offset
                    + "; regionLength=" + count);
        }
    }

    public static Class<?> getRawType(Type type) {
        if (type == null) throw new NullPointerException("type == null");

        if (type instanceof Class<?>) {
            // Type is a normal class.
            return (Class<?>) type;
        }
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;

            // I'm not exactly sure why getRawType() returns Type instead of Class. Neal isn't either but
            // suspects some pathological case related to nested classes exists.
            Type rawType = parameterizedType.getRawType();
            if (!(rawType instanceof Class)) throw new IllegalArgumentException();
            return (Class<?>) rawType;
        }
        if (type instanceof GenericArrayType) {
            Type componentType = ((GenericArrayType) type).getGenericComponentType();
            return Array.newInstance(getRawType(componentType), 0).getClass();
        }
        if (type instanceof TypeVariable) {
            // We could use the variable's bounds, but that won't work if there are multiple. Having a raw
            // type that's more general than necessary is okay.
            return Object.class;
        }
        if (type instanceof WildcardType) {
            return getRawType(((WildcardType) type).getUpperBounds()[0]);
        }

        throw new IllegalArgumentException("Expected a Class, ParameterizedType, or "
                + "GenericArrayType, but <" + type + "> is of type " + type.getClass().getName());
    }

    public static <T> T newInstance(Class<T> clazz) throws InstanceException {
        try {
            return clazz.newInstance();
        } catch (InstantiationException e) {
            throw new InstanceException(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new InstanceException(e.getMessage());
        }
    }

    /**
     * 根据Request生成哈希值
     *
     * @param request
     * @return
     */
    public static String getHash(Request request) {
        if(request == null)
            return UUID.randomUUID().toString();
        StringBuilder str = new StringBuilder();
        str.append('[');
        str.append(request.method());
        str.append(']');
        str.append('[');
        str.append(request.url());
        str.append(']');

        try {
            if(request.body() != null) {
                Buffer buffer = new Buffer();
                request.body().writeTo(buffer);
                str.append(buffer.readByteString().sha1().hex());
            }
        } catch (IOException e) {
            return "";
        }

        str.append('-');
        str.append(ByteString.of(request.headers().toString().getBytes()).sha1().hex());

        return str.toString();
    }

}
