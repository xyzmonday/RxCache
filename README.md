# 1 RxCache
这是一个基于RxJava2的三级缓存框架。2016年RxJava2发布后，查阅了大量的资料，总结了RxJava2的基本用法（可以见鄙人的RxJava2Demo项目）。那为什么要写这么一个项目了。
因为在学习Android基础的时候，刚开始接触的是Bitmap的缓存，然后是一些缓存框架。我推进一个我系统研究过的缓存框架:<br/>

> https://github.com/Trinea/android-common <br/>

以及一些基于RxJava1.x或者RxJava2.x的缓存框架:<br/>
> https://github.com/LittleFriendsGroup/KakaCache-RxJava  <br/>
> https://github.com/VictorAlbertos/RxCache <br/>

上面两个缓存框架，阅读起来比较困难，我只是阅读了他们的关键代码。那么一直在构想一套比较简单的基于RxJava2的框架，首先列出查阅的文章:

> http://www.jianshu.com/p/ab70e9286b8b <br/>
> http://blog.csdn.net/aishang5wpj/article/details/51692824 <br/>
> http://blog.csdn.net/qq_35064774/article/details/53449795 <br/>
> http://www.jianshu.com/p/1e9e8f4213f3 <br/>
> http://blog.csdn.net/dd864140130/article/details/52714272 <br/>


## 2 RxCache设计图

 ![image](https://github.com/xyzmonday/RxCache/raw/master/screenshots/pic1.png)
 
 以上就是RxCache的基本设计。CacheCore类将同时管理MemoryCache和DiskCache，并且通过CacheTarget中的枚举值可以方便的控制是否支持内存和磁盘缓存。RxCache的基本思路是实现一个RxJava的Transform，通过该Transform将网络流拦截掉，然后根据不同的缓存配置，加载和缓存该网络流。如下面的代码所示:

```Java

 flowable = gankApi.getHistoryGank(1)
                    .compose(rxCache.<GankBean>transformer(MD5.getMessageDigest("custom_key"), strategy));

```
而Transform的实现方式如下
```Java

 private static class CacheTransformer<T> implements FlowableTransformer<T, ResultData<T>> {
        private String key;
        private CacheStrategy cacheStrategy;

        public CacheTransformer(String key, CacheStrategy cacheStrategy) {
            this.key = CommonUtil.hashKeyForDisk(key);
            this.cacheStrategy = cacheStrategy;
        }

        @Override
        public Publisher<ResultData<T>> apply(Flowable<T> upstream) {
            return cacheStrategy.execute(key, upstream);
        }
    }
```
显然该Transform最后将缓存的逻辑交给了CacheStrategy去去管理，实际上CacheStrategy是一个枚举值，里面添加了一个execute抽象方法，通过不同的枚举值，执行不同的缓存策略。接下来我以FirstCache和CacheAndRemote策略为例说明原理:

* 2.1 FirstCache
FirstCache是先加载缓存，如果缓存加载成功，那么直接返回给用户。如果未获取到缓存，那么将获取Remote端的数据，具体代码如下:
```Java

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

```
注意这里的firstOrError操作符，该操作符的意思是如果cache有数据那么直接返回，如果cache和remote都没有数据那么回调onError方法。另外，我们对cache流添加了一个onErrorResumeNext操作符，这是因为如果缓存中未获取的任何数据，loadCache方法会抛出一个运行时异常，如果不处理该异常，那么在没有缓存的情况下直接值onError回调，从而导致remote流不能够执行。

* 2.2 FirstAndCache
FirstAndCache的实现原理和FirstCache区别不大，代码如下:

```Java
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
   
 ```
 注意这不在使用firstOrError，而是使用switchIfEmpty。因为这加载完缓存之后，需要通过remote加载最新的远程数据。如果cache和remote都为获取到数据，那么将抛出NoSuchElementException异常。
 
 * 注解使用<br/>
为了更加方便，设计了一个用于缓存的注解。该注解主要就是配置缓存的策略，那么RxCache怎么拿到注解，然后结合已经实现的Transform，从而实现缓存。我们知道，Retrofit实际上为我们实现了4个adapter，那么我们找到RxJavaCallAdapter接口，将call拦截住，然后将call返回的流根据注解信息给Transform。主要代码如下:
 
 ```Java
 （RxJava2CallAdapter）
 
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
 
  ```
 
 ## 效果展示
 
 
 
 
