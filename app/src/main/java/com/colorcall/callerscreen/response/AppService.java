package com.colorcall.callerscreen.response;

import retrofit2.Call;
import retrofit2.http.GET;

public interface AppService {
    @GET("/api/getTheme")
    Call<AppData> getTheme();
}
