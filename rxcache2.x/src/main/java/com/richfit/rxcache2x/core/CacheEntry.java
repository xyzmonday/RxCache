package com.richfit.rxcache2x.core;

import com.litesuits.orm.db.annotation.Column;
import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.enums.AssignType;

import java.io.Serializable;

/**
 * 日志项
 *
 * @version monday 2016-04
 */
public class CacheEntry implements Serializable, Cloneable {
    public static final String COL_KEY = "key";
    public static final String COL_CREATE_TIME = "create_time";
    public static final String COL_LAST_USE_TIME = "last_use_time";
    public static final String COL_USE_COUNT = "use_count";
    public static final String COL_EXPIRY_TIME = "expiry_time";
    public static final String COL_TARGET = "expiry_target";

    /**
     * KEY
     */
    @PrimaryKey(AssignType.BY_MYSELF)
    @Column(COL_KEY)
    private String key;
    /**
     * 创建时间
     */
    @Column(COL_CREATE_TIME)
    private long createTime;
    /**
     * 最后使用时间
     */
    @Column(COL_LAST_USE_TIME)
    private long lastUseTime;
    /**
     * 总使用次数
     */
    @Column(COL_USE_COUNT)
    private long useCount;
    /**
     * 过期时间
     */
    @Column(COL_EXPIRY_TIME)
    private long expiryTime;
    /**
     * 缓存目标
     */
    @Column(COL_TARGET)
    private CacheTarget target;



    public CacheEntry() {

    }

    public CacheEntry(String key, long maxAge, CacheTarget target) {
        long currentTime = System.currentTimeMillis();
        this.key = key;
        this.createTime = currentTime;
        this.lastUseTime = currentTime;
        this.expiryTime = currentTime + maxAge;
        this.target = target;
        this.useCount = 1;
    }

    public static CacheEntry cloneCacheEntry(CacheEntry entry) {
        CacheEntry cacheEntry = new CacheEntry();
        if (entry != null) {
            cacheEntry.key = entry.key;
            cacheEntry.createTime = entry.createTime;
            cacheEntry.lastUseTime = entry.lastUseTime;
            cacheEntry.expiryTime = entry.expiryTime;
            cacheEntry.target = entry.target;
            cacheEntry.useCount = entry.useCount;
        }
        return cacheEntry;
    }


    /**
     * 是否过期
     */
    public boolean isExpiry() {
        return System.currentTimeMillis() > expiryTime;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CacheEntry entry = (CacheEntry) o;

        if (createTime != entry.createTime) return false;
        if (lastUseTime != entry.lastUseTime) return false;
        if (useCount != entry.useCount) return false;
        if (expiryTime != entry.expiryTime) return false;
        if (key != null ? !key.equals(entry.key) : entry.key != null) return false;
        return target == entry.target;
    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (int) (createTime ^ (createTime >>> 32));
        result = 31 * result + (int) (lastUseTime ^ (lastUseTime >>> 32));
        result = 31 * result + (int) (useCount ^ (useCount >>> 32));
        result = 31 * result + (int) (expiryTime ^ (expiryTime >>> 32));
        result = 31 * result + (target != null ? target.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "{" +
                "key='" + key + '\'' +
                ", createTime=" + createTime +
                ", lastUseTime=" + lastUseTime +
                ", useCount=" + useCount +
                ", expiryTime=" + expiryTime +
                ", target=" + target +
                '}';
    }


    public String getKey() {
        return key;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getLastUseTime() {
        return lastUseTime;
    }

    public void setLastUseTime(long lastUseTime) {
        this.lastUseTime = lastUseTime;
    }

    public long getUseCount() {
        return useCount;
    }

    public void setUseCount(long useCount) {
        this.useCount = useCount;
    }

    public long getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(long expiryTime) {
        this.expiryTime = expiryTime;
    }

    public CacheTarget getTarget() {
        return target;
    }

    public void setTarget(CacheTarget target) {
        this.target = target;
    }
}
