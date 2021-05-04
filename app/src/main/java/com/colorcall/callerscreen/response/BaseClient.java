package com.colorcall.callerscreen.response;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BaseClient {
    private static HttpLoggingInterceptor sLogging =
            new HttpLoggingInterceptor()
                    .setLevel(HttpLoggingInterceptor.Level.BODY);

    private static OkHttpClient.Builder sHttpClient =
            new OkHttpClient.Builder();

    static <S> S createService(Class<S> serviceClass, String baseUrl, OkHttpClient okHttpClient) {
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient) //The Htttp client to be used for requests
                .addConverterFactory(GsonConverterFactory.create());
        Retrofit retrofit = builder.build();
        if (!sHttpClient.interceptors().contains(sLogging)) {
            sHttpClient.addInterceptor(sLogging);
            builder.client(sHttpClient.build());
            retrofit = builder.build();
        }
        return retrofit.create(serviceClass);
    }
}
