package com.richfit.rxcache2x.netcache.strategy;



import com.richfit.rxcache2x.netcache.ResultData;

import io.reactivex.Flowable;

/**
 * @version monday 2016-07
 */
public interface ICacheStrategy {

    <T> Flowable<ResultData<T>> execute(String key, Flowable<T> source);

}
