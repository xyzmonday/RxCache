package com.richfit.rxcache2x.core.disk.journal;

import com.litesuits.orm.LiteOrm;

/**
 * Unlimited缓存日志
 * @version monday 2016-07
 */
public class UnlimitedDiskJournal extends BasicDiskJournal {

    public UnlimitedDiskJournal(LiteOrm liteOrm) {
        super(liteOrm);
    }

    // 永不清除有效的缓存（过期依旧会被清理）
    @Override
    public String getLoseKey() {
        return null;
    }

}
