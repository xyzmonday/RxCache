package com.richfit.rxcache;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.richfit.rxcache.utils.MD5;
import com.richfit.rxcache2x.RxCache;
import com.richfit.rxcache2x.core.disk.converter.SerializableDiskConverter;
import com.richfit.rxcache2x.netcache.ResultData;
import com.richfit.rxcache2x.netcache.ResultFrom;
import com.richfit.rxcache2x.netcache.strategy.CacheStrategy;
import com.richfit.rxcache2x.netcache.strategy.ICacheStrategy;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.ResourceSubscriber;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //请求超时时间
    private final static int DEFUALT_TIME_OUT = 20;

    private GankApi gankApi;
    private RxCache rxCache;


    private Spinner spWithAnnotation, spCacheStrategy;
    private Button btnStartLoad;
    private TextView tvData;

    private CompositeDisposable mCompositeDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvData = (TextView) findViewById(R.id.tv_show_data);
        btnStartLoad = (Button) findViewById(R.id.btn_start_load);
        spWithAnnotation = (Spinner) findViewById(R.id.sp_with_annotation);
        spCacheStrategy = (Spinner) findViewById(R.id.sp_cache_strategy);
        btnStartLoad.setOnClickListener(this);
        mCompositeDisposable = new CompositeDisposable();
        initRetrofit();
        initData();
    }


    private void initRetrofit() {
        //打印拦截器
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
                Log.i("yff", message);
            }
        });
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);


        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)//添加打印拦截器
                .connectTimeout(DEFUALT_TIME_OUT, TimeUnit.SECONDS)//设置请求超时时间
                .readTimeout(DEFUALT_TIME_OUT, TimeUnit.SECONDS)
                .writeTimeout(DEFUALT_TIME_OUT, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)//设置出现错误进行重新连接。
                .build();

        gankApi = new Retrofit.Builder()
                .baseUrl(GankApi.BASE_URL)
                .client(httpClient)
                .addConverterFactory(RxCache.gsonConverter())
                .addCallAdapterFactory(RxCache.rxJava2CacheCallAdapter())
                .build()
                .create(GankApi.class);

        rxCache = new RxCache.Builder()
                .appVersion(1)//不设置，默认为1
                .diskCache(new File(getCacheDir().getPath() + File.separator + "data-cache"))
                .diskConverter(new SerializableDiskConverter())//目前只支持Serializable缓存
                .memoryMax(2 * 1024 * 1024)//不设置,默认为运行内存的8分之1
                .diskMax(20 * 1024 * 1024)//不设置， 默为认50MB
                .build();

    }

    private void initData() {
        List<String> list1 = new ArrayList<>();
        list1.add("不带注解");
        list1.add("带注解");
        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list1);
        spWithAnnotation.setAdapter(adapter1);

        List<String> list2 = new ArrayList<>();
        list2.add("OnlyCache");
        list2.add("OnlyRemote");
        list2.add("FirstRemote");
        list2.add("FirstCache");
        list2.add("CacheAndRemote");
        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list2);
        spCacheStrategy.setAdapter(adapter2);
    }

    @Override
    public void onClick(View view) {
        boolean isAnno = spWithAnnotation.getSelectedItemPosition() == 1;
        int strategyPos = spCacheStrategy.getSelectedItemPosition();
        CacheStrategy cacheStrategy = null;
        switch (strategyPos) {
            case 0:
                cacheStrategy = CacheStrategy.OnlyCache;
                break;
            case 1:
                cacheStrategy = CacheStrategy.OnlyRemote;
                break;
            case 2:
                cacheStrategy = CacheStrategy.FirstRemote;
                break;
            case 3:
                cacheStrategy = CacheStrategy.FirstCache;
                break;
            case 4:
                cacheStrategy = CacheStrategy.CacheAndRemote;
                break;
            default:
                cacheStrategy = CacheStrategy.OnlyRemote;
                break;
        }
        loadData(isAnno, cacheStrategy, strategyPos);
    }

    private void loadData(boolean isAnno, CacheStrategy strategy, int strategyPos) {

        tvData.setText("加载中...");
        Flowable<ResultData<GankBean>> flowable = null;
        if (!isAnno) {
            //不使用注解
            flowable = gankApi.getHistoryGank(1)
                    .compose(rxCache.<GankBean>transformer(MD5.getMessageDigest("custom_key"), strategy));
        } else {
            switch (strategyPos) {
                case 0:
                    flowable = gankApi.getHistoryGankOnlyCache(1);
                    break;
                case 1:
                    flowable = gankApi.getHistoryGankOnlyRemote(1);
                    break;
                case 2:
                    flowable = gankApi.getHistoryGankFirstCache(1);
                    break;
                case 3:
                    flowable = gankApi.getHistoryGankFirstRemote(1);
                    break;
                case 4:
                    flowable = gankApi.getHistoryGankCacheAndRemote(1);
                    break;
                default:
                    flowable = gankApi.getHistoryGankOnlyRemote(1);
                    break;
            }
        }

        ResourceSubscriber<ResultData<GankBean>> subscriber = flowable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new ResourceSubscriber<ResultData<GankBean>>() {
                    @Override
                    public void onNext(ResultData<GankBean> resultData) {
                        if (resultData.from == ResultFrom.Cache) {
                            tvData.setText("来自缓存：\n" + resultData.toString());
                        } else {
                            tvData.setText("来自网络：\n" + resultData.toString());
                        }

                    }


                    @Override
                    public void onError(Throwable t) {
                        Log.e("yff", "MainActivity onError = " + t.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
        mCompositeDisposable.add(subscriber);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCompositeDisposable.isDisposed()) {
            mCompositeDisposable.isDisposed();
        }
    }
}
