package com.richfit.rxcache2x.core.disk.journal;

import com.litesuits.orm.LiteOrm;
import com.litesuits.orm.db.assit.QueryBuilder;
import com.richfit.rxcache2x.core.CacheEntry;

import java.util.List;

/**
 * FIFO缓存日志
 * @version monday 2016-07
 */
public class FIFODiskJournal extends BasicDiskJournal {

    public FIFODiskJournal(LiteOrm liteOrm) {
        super(liteOrm);
    }

    @Override
    public String getLoseKey() {
        QueryBuilder query = new QueryBuilder(CacheEntry.class);
        query.orderBy(CacheEntry.COL_CREATE_TIME);
        query.limit(0, 1);
        List<CacheEntry> list = getDb().query(query);
        if (list != null && list.size() >0) {
            return list.get(0).getKey();
        } else {
            return null;
        }
    }

}
