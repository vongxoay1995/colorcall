package com.colorcall.callerscreen.response;


import com.colorcall.callerscreen.BuildConfig;
import com.colorcall.callerscreen.constan.Constant;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class AppClient extends BaseClient {
    private static AppService appService;

    public static AppService getInstance() {
        if (appService == null) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(BuildConfig.DEBUG
                    ? HttpLoggingInterceptor.Level.BODY
                    : HttpLoggingInterceptor.Level.NONE);

            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();
            appService = createService(AppService.class, Constant.BASE_URL, client);
        }
        return appService;
    }

}
