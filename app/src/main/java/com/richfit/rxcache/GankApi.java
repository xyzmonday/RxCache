package com.richfit.rxcache;

import com.litesuits.orm.db.enums.Strategy;
import com.richfit.rxcache2x.CACHE;
import com.richfit.rxcache2x.netcache.ResultData;
import com.richfit.rxcache2x.netcache.strategy.CacheStrategy;

import io.reactivex.Flowable;
import retrofit2.http.GET;
import retrofit2.http.Path;


/**
 * Created by Chu on 2016/10/25.
 */

public interface GankApi {

    String BASE_URL = "http://gank.io/api/";

    @GET("history/content/3/{page}")
    Flowable<GankBean> getHistoryGank(@Path("page") int page);

    @GET("history/content/3/{page}")
    @CACHE(strategy = CacheStrategy.OnlyCache)
    Flowable<ResultData<GankBean>> getHistoryGankOnlyCache(@Path("page") int page);

    @GET("history/content/3/{page}")
    @CACHE(strategy = CacheStrategy.OnlyRemote)
    Flowable<ResultData<GankBean>> getHistoryGankOnlyRemote(@Path("page") int page);


    @GET("history/content/3/{page}")
    @CACHE(strategy = CacheStrategy.FirstCache)
    Flowable<ResultData<GankBean>> getHistoryGankFirstCache(@Path("page") int page);


    @GET("history/content/3/{page}")
    @CACHE(strategy = CacheStrategy.FirstRemote)
    Flowable<ResultData<GankBean>> getHistoryGankFirstRemote(@Path("page") int page);

    @GET("history/content/3/{page}")
    @CACHE(strategy = CacheStrategy.CacheAndRemote)
    Flowable<ResultData<GankBean>> getHistoryGankCacheAndRemote(@Path("page") int page);
}
