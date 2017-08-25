/*
 * Copyright (C) 2016 Jake Wharton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.richfit.rxcache2x.netcache.retrofit;


import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.richfit.rxcache2x.RxCache;
import com.richfit.rxcache2x.netcache.strategy.CacheStrategy;
import com.richfit.rxcache2x.utils.CommonUtil;

import org.w3c.dom.Text;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Observable;
import io.reactivex.Scheduler;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okio.ByteString;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Response;

final class RxJava2CallAdapter<R> implements CallAdapter<R, Object> {
    private final Type responseType;
    private final
    @Nullable
    Scheduler scheduler;
    private final boolean isAsync;
    private final boolean isResult;
    private final boolean isBody;
    private final boolean isFlowable;
    private final boolean isSingle;
    private final boolean isMaybe;
    private final boolean isCompletable;
    private final boolean isCache;
    private final CacheInfo cacheInfo;

    RxJava2CallAdapter(Type responseType, @Nullable Scheduler scheduler, boolean isAsync,
                       boolean isResult, boolean isBody, boolean isFlowable, boolean isSingle, boolean isMaybe,
                       boolean isCompletable, boolean isCache, CacheInfo cacheInfo) {
        this.responseType = responseType;
        this.scheduler = scheduler;
        this.isAsync = isAsync;
        this.isResult = isResult;
        this.isBody = isBody;
        this.isFlowable = isFlowable;
        this.isSingle = isSingle;
        this.isMaybe = isMaybe;
        this.isCompletable = isCompletable;
        this.isCache = isCache;
        this.cacheInfo = cacheInfo;
    }

    @Override
    public Type responseType() {
        return responseType;
    }

    @Override
    public Object adapt(Call<R> call) {

        Observable<Response<R>> responseObservable = isAsync
                ? new CallEnqueueObservable<>(call)
                : new CallExecuteObservable<>(call);

        Observable<?> observable;
        if (isResult) {
            observable = new ResultObservable<>(responseObservable);
        } else if (isBody) {
            observable = new BodyObservable<>(responseObservable);
        } else {
            observable = responseObservable;
        }

        //处理订阅线程
        if (scheduler != null) {
            observable = observable.subscribeOn(scheduler);
        }

        //处理Flowable的缓存
        if (isFlowable) {
            if (isCache) {
                if (cacheInfo != null) {
                    CacheInfo info = getCacheInfo(call,cacheInfo);
                    return observable.toFlowable(BackpressureStrategy.LATEST)
                            .compose(RxCache.transformer(info.getKey(), info.getStrategy()));
                }
            }
            return observable.toFlowable(BackpressureStrategy.LATEST);
        }
        if (isSingle) {
            return observable.singleOrError();
        }
        if (isMaybe) {
            return observable.singleElement();
        }
        if (isCompletable) {
            return observable.ignoreElements();
        }
        //如理Observable的缓存
        if (isCache) {
            if (cacheInfo != null) {
                CacheInfo info = getCacheInfo(call,cacheInfo);
                return observable.toFlowable(BackpressureStrategy.LATEST)
                        .compose(RxCache.transformer(info.getKey(), info.getStrategy()))
                        .toObservable();
            }
        }
        return observable;
    }

    private <R> CacheInfo getCacheInfo(final Call<R> call,CacheInfo info) {
        if (info == null) {
            return null;
        }
        //获取开启缓存的注解
        if (info != null && info.isEnable()) {
            // 处理缓存
            if (CommonUtil.isEmpty(info.getKey())) {
                // 生成Key
                String key = CommonUtil.getHash(call.request());
                info.setKey(ByteString.of(key.getBytes()).md5().hex());
            }
            //处理缓存策略
            if (info.getStrategy() == null) {
                info.setStrategy(CacheStrategy.OnlyRemote);
            }
        }
        return info;
    }
}
