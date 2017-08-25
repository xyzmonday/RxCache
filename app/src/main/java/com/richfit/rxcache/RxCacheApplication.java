package com.richfit.rxcache;

import android.app.Application;

import com.richfit.rxcache2x.RxCache;

/**
 * Created by monday on 2017/8/25.
 */

public class RxCacheApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        RxCache.init(this);
    }
}
