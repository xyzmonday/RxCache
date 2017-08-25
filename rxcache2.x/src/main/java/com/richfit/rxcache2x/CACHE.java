package com.richfit.rxcache2x;



import com.richfit.rxcache2x.netcache.strategy.CacheStrategy;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 缓存注解，该注解使用在Retrofit的api上面，结合callAdapter可以自动开启缓存。
 */
@Documented
@Target(METHOD)
@Retention(RUNTIME)
public @interface CACHE {

    /**
     * 缓存的KEY
     * @return 空表示由系统自动生成
     */
    String value() default "";

    /**
     * 是否启用缓存
     * @return 默认启用
     */
    boolean enable() default true;

    /**
     * 缓存策略
     * @return
     */
    CacheStrategy strategy() default CacheStrategy.FirstRemote;

}
