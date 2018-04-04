package com.star.learningrxjavacascaderequest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "RxJava";

    private static final String BASE_URL = "https://api.github.com";
    public static final String PATH = "/repos/{owner}/{repo}/contributors";

    private static final String OWNER = "square";
    private static final String REPO = "retrofit";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create a very simple REST adapter which points the GitHub API.
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        // Create an instance of our GitHub API interface.
        GitHub github = retrofit.create(GitHub.class);

        // Create an observable instance for looking up Retrofit contributors.
        Observable<List<Contributor>> contributorsObservable =
                github.contributors(OWNER, REPO);

        // Create an observable instance for looking up Retrofit htmlUrls.
        Observable<List<HtmlUrl>> htmlUrlsObservable =
                github.htmlUrls(OWNER, REPO);

        contributorsObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(contributors -> {
                    for (Contributor contributor : contributors) {
                        Log.d(TAG, contributor.login + " (" + contributor.contributions + ")");
                    }
                })
                .observeOn(Schedulers.io())
                .flatMap((Function<List<Contributor>, ObservableSource<List<HtmlUrl>>>)
                        contributors -> htmlUrlsObservable)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(htmlUrls -> {
                    for (HtmlUrl htmlUrl : htmlUrls) {
                        Log.d(TAG, htmlUrl.id + " (" + htmlUrl.html_url + ")");
                    }
                });
    }
}
