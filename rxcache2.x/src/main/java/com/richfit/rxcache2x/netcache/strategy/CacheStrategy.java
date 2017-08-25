package com.richfit.rxcache2x.netcache.strategy;


import android.util.Log;

import com.richfit.rxcache2x.RxCache;
import com.richfit.rxcache2x.core.CacheTarget;
import com.richfit.rxcache2x.netcache.ResultData;
import com.richfit.rxcache2x.netcache.ResultFrom;

import org.reactivestreams.Publisher;

import java.util.NoSuchElementException;

import io.reactivex.Flowable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;


/**
 * 使用枚举实现缓存策略，由于我们在枚举中加入了抽闲方法，那么每一个枚举值必须
 * 实现该方法。
 * 这里将实现该文章提出的几种需求
 * http://blog.csdn.net/qq_35064774/article/details/53449795
 * 1. 数据实时性高
 * 伪代码如下:
 * 如果 (存在缓存) {
 *   读取缓存并显示
 * }
 *   请求网络
 *   写入缓存
 *   显示网络数据
 * ============================================================
 * 2.数据定期更新或不频繁变化
 * 伪代码如下:
 * 如果 (存在缓存 且 缓存未过期) {
 *   读取缓存并显示
 *   返回
 *}
 *  请求网络
 *  更新缓存
 *  显示最新数据
 *  注意这里与方案一不同的地方在于:方案2如果读取到缓存那么直接返回了。所以方案一
 *  实现采用了concat+switchIfEmpty操作符；方案二使用concat+firstOrError
 *  操作符实现。那么我们将整合这两个思路，当然你也可以扩展缓存策略。
 * @version monday 2016-07
 */
public enum CacheStrategy {
    /**
     * 仅缓存。如果没有获取到缓存信息，那么直接走onError方法会调，将错误信息提示给用户。
     */
    OnlyCache {
        @Override
        public <T> Flowable<ResultData<T>> execute(String key, Flowable<T> source) {
            return loadCache(key);
        }
    },
    /**
     * 仅网络
     */
    OnlyRemote {
        @Override
        public <T> Flowable<ResultData<T>> execute(String key, Flowable<T> source) {
            return loadRemote(key, source);
        }
    },

    /**
     * 优选远程数据，只有remote无数据才走cache。
     */
    FirstRemote {
        @Override
        public <T> Flowable<ResultData<T>> execute(String key, Flowable<T> source) {
            Flowable<ResultData<T>> cache = loadCache(key);
            Flowable<ResultData<T>> remote = loadRemote(key, source);
            return Flowable.concat(remote, cache)
                    .onBackpressureBuffer()
                    .filter(result -> result != null && result.data != null)
                    .firstOrError()
                    .toFlowable();
        }
    },

    /**
     * 优先缓存。这里应该拦截掉cache未获取到的错误信息，这样才有机会加载remote数据
     */
    FirstCache {
        @Override
        public <T> Flowable<ResultData<T>> execute(String key, Flowable<T> source) {
            Flowable<ResultData<T>> cache = loadCache(key);
            Flowable<ResultData<T>> remote = loadRemote(key, source);
            return Flowable.concat(cache.onErrorResumeNext(throwable -> {
                return Flowable.empty();
            }), remote)
                    .onBackpressureBuffer()
                    .filter(result -> result != null && result.data != null)
                    //这里离使用firstOrError操作符，该操作符的意思是只要cache或者remote
                    //有数据那么直接返回，也就是实现了方案2.如果两个数据源都没有数据那么返回错误
                    .firstOrError().toFlowable();
        }
    },
    /**
     * 先缓存，后网络。注意这里先通从缓存得到数据，不管存在与否都会请求远程数据
     */
    CacheAndRemote {
        @Override
        public <T> Flowable<ResultData<T>> execute(String key, Flowable<T> source) {
            Flowable<ResultData<T>> cache = loadCache(key);
            Flowable<ResultData<T>> remote = loadRemote(key, source);
            return Flowable.concat(cache.onErrorResumeNext(throwable -> {
                return Flowable.empty();
            }), remote.onErrorResumeNext(throwable -> {
                return Flowable.empty();
            }))
                    .onBackpressureBuffer()
                    .filter(result -> result.data != null)
                    //switchIfEmpty表明缓存和远程数据如果都存在，那么都将显示到界面
                    //也就是方案1伪代码的需求
                    .switchIfEmpty(s -> s.onError(new NoSuchElementException()));
        }
    };

    /**
     * 加载缓存
     * @param key
     * @param <T>
     * @return
     */
    <T> Flowable<ResultData<T>> loadCache(final String key) {
        return RxCache.manager().load(key)
                .filter(value -> value != null)
                .map(value -> new ResultData<>(ResultFrom.Cache, key, (T) value));

    }

    /**
     * 保存远程数据到缓存，注意这里我们没有拦击掉onError的情况，因为我们需要将错误信息返回给用户。
     *
     * @param key
     * @param source
     * @param <T>
     * @return
     */
    <T> Flowable<ResultData<T>> loadRemote(final String key, Flowable<T> source) {
        return source.map(value -> {
            RxCache.manager().save(key, value).subscribe(status -> Log.d("yff","CacheStrategy loadRemote => 保存远程数据" + (status ? "成功" : "失败")));
            return new ResultData<>(ResultFrom.Remote, key, value);
        });
    }

    /**
     * TODO:扩展通过注解实现将远程数据缓存的目标
     * @param key
     * @param source
     * @param target
     * @param <T>
     * @return
     */
    <T> Flowable<ResultData<T>> loadRemote(final String key, Flowable<T> source, final CacheTarget target) {
        return source.map(value -> {
            RxCache.manager().save(key, value, target);
            return new ResultData<>(ResultFrom.Remote, key, value);
        });
    }

    public abstract <T> Flowable<ResultData<T>> execute(String key, Flowable<T> source);

}
