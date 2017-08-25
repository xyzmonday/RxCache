/*
 * Copyright (C) 2015 Square, Inc.
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

import com.richfit.rxcache2x.netcache.ResultData;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import retrofit2.CallAdapter;
import retrofit2.HttpException;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.Result;


public final class RxJava2CallAdapterFactory extends CallAdapter.Factory {
    /**
     * Returns an instance which creates synchronous observables that do not operate on any scheduler
     * by default.
     */
    public static RxJava2CallAdapterFactory create() {
        return new RxJava2CallAdapterFactory(null, false);
    }

    /**
     * Returns an instance which creates asynchronous observables. Applying
     * {@link Observable#subscribeOn} has no effect on stream types created by this factory.
     */
    public static RxJava2CallAdapterFactory createAsync() {
        return new RxJava2CallAdapterFactory(null, true);
    }

    /**
     * Returns an instance which creates synchronous observables that
     * {@linkplain Observable#subscribeOn(Scheduler) subscribe on} {@code scheduler} by default.
     */
    @SuppressWarnings("ConstantConditions") // Guarding public API nullability.
    public static RxJava2CallAdapterFactory createWithScheduler(Scheduler scheduler) {
        if (scheduler == null) throw new NullPointerException("scheduler == null");
        return new RxJava2CallAdapterFactory(scheduler, false);
    }

    private final
    @Nullable
    Scheduler scheduler;
    private final boolean isAsync;

    private RxJava2CallAdapterFactory(@Nullable Scheduler scheduler, boolean isAsync) {
        this.scheduler = scheduler;
        this.isAsync = isAsync;
    }

    @Override
    public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {

        //获取返回的数据类型可能是Observable,Flowable,Completable,Single,Maybe等
        Class<?> rawType = getRawType(returnType);

        //处理Completable
        if (rawType == Completable.class) {
            return new RxJava2CallAdapter(Void.class, scheduler, isAsync, false, true, false, false,
                    false, true, false, null);
        }

        boolean isFlowable = rawType == Flowable.class;
        boolean isSingle = rawType == Single.class;
        boolean isMaybe = rawType == Maybe.class;

        //如果不是正常的RxJava操作符，那么返回为空。表示不能处理
        if (rawType != Observable.class && !isFlowable && !isSingle && !isMaybe) {
            return null;
        }

        //处理响应返回的数据类型
        boolean isResult = false;
        boolean isBody = false;

        //自己添加的响应类型
        boolean isCache = false;
        CacheInfo info = null;

        Type responseType;
        //如果返回的类型没有参数化
        if (!(returnType instanceof ParameterizedType)) {
            String name = isFlowable ? "Flowable"
                    : isSingle ? "Single"
                    : isMaybe ? "Maybe" : "Observable";
            throw new IllegalStateException(name + " return type must be parameterized"
                    + " as " + name + "<Foo> or " + name + "<? extends Foo>");
        }

        //获取参数化类型
        Type observableType = getParameterUpperBound(0, (ParameterizedType) returnType);
        Class<?> rawObservableType = getRawType(observableType);


        if (rawObservableType == Response.class) {
            //如果返回的Response类型，该类是Retrofit里面的
            if (!(observableType instanceof ParameterizedType)) {
                throw new IllegalStateException("Response must be parameterized"
                        + " as Response<Foo> or Response<? extends Foo>");
            }
            responseType = getParameterUpperBound(0, (ParameterizedType) observableType);

        } else if (rawObservableType == Result.class) {
            //如果返回的Retrofit的是Result数据类型
            if (!(observableType instanceof ParameterizedType)) {
                throw new IllegalStateException("Result must be parameterized"
                        + " as Result<Foo> or Result<? extends Foo>");
            }
            responseType = getParameterUpperBound(0, (ParameterizedType) observableType);
            isResult = true;
        } else if (rawObservableType == ResultData.class) {
            //如果返回的是我们自己定义的缓存数据类型
            responseType = observableType;
            isBody = true;
            isCache = true;
            info = CacheInfo.get(annotations);
            //拿到缓存注解
            if (info == null) {
                responseType = observableType;
                isCache = false;
            }
        } else {
            responseType = observableType;
            isBody = true;
        }

        return new RxJava2CallAdapter(responseType, scheduler, isAsync, isResult, isBody, isFlowable,
                isSingle, isMaybe, false, isCache, info);
    }
}
