package com.finalproject;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public class APIService {

    public static final String BASE_URL = "http://192.168.100.9:5000";
    private static API api = null;

    public static API getAPIService() {
        if (api == null) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();

            api = retrofit.create(API.class);
        }

        return api;
    }

    public interface API {
        @Multipart
        @POST("api/auth/register")
        Call<AuthResponse> register(
                @Part("username") RequestBody username,
                @Part("gender") RequestBody gender,
                @Part List<MultipartBody.Part> voices
        );

        @Multipart
        @POST("api/auth/login")
        Call<AuthResponse> login(
                @Part("username") RequestBody username,
                @Part MultipartBody.Part voice
        );
    }
}
