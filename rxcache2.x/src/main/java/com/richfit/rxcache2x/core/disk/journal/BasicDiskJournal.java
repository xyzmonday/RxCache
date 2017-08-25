package com.richfit.rxcache2x.core.disk.journal;

import android.util.Log;

import com.litesuits.orm.LiteOrm;
import com.litesuits.orm.db.assit.WhereBuilder;
import com.richfit.rxcache2x.core.CacheEntry;
import com.richfit.rxcache2x.exceptions.NullException;
import com.richfit.rxcache2x.utils.CommonUtil;

import java.io.IOException;
import java.util.Collection;

/**
 * 缓存日志-基类
 * @version monday 2016-07
 */
public abstract class BasicDiskJournal implements IDiskJournal {

    private final LiteOrm mLiteOrm;

    public BasicDiskJournal(LiteOrm liteOrm) {
        this.mLiteOrm = liteOrm;
    }

    final LiteOrm getDb() {
        return mLiteOrm;
    }

    @Override
    public CacheEntry get(String key) {
        if (CommonUtil.isEmpty(key)) {
            throw new NullException("key == null");
        }

        CacheEntry entry = null;
        try {
            entry = mLiteOrm.queryById(key, CacheEntry.class);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("yff","查询出错 = " + e.getMessage());
        }
        if (entry != null) {
            // 有效期内，才记录最后使用时间
            if (!entry.isExpiry()) {
                entry.setLastUseTime(System.currentTimeMillis());
                entry.setUseCount(entry.getUseCount() + 1);
                mLiteOrm.update(entry);
            }
            return entry;
        } else {
            return null;
        }
    }

    @Override
    public void put(String key, CacheEntry entry) {
        if (CommonUtil.isEmpty(key) || entry == null) {
            throw new NullException("key == null || value == null");
        }
        if (!entry.isExpiry()) {
            entry.setLastUseTime(System.currentTimeMillis());
            entry.setUseCount(entry.getUseCount() + 1);
            mLiteOrm.save(entry);
        } else {
            remove(key);
        }
    }

    @Override
    public final boolean containsKey(String key) {
        CacheEntry entry = get(key);
        return entry != null;
    }

    @Override
    public abstract String getLoseKey();

    @Override
    public final boolean remove(String key) {
        int result = mLiteOrm.delete(new WhereBuilder(CacheEntry.class)
                .where(CacheEntry.COL_KEY + " = ?", new Object[]{"%"+key+"%"}));
        return result > 0;
    }

    @Override
    public final boolean clear() {
        int result = mLiteOrm.deleteAll(CacheEntry.class);
        return result >= 0;
    }

    @Override
    public final Collection<CacheEntry> snapshot() {
        return mLiteOrm.query(CacheEntry.class);
    }

    @Override
    public void close() throws IOException {
        // TODO Nothing
        //mLiteOrm.close();
    }

}
