package com.vegas.tranquilo.api;

import android.util.Base64;

import androidx.annotation.NonNull;

import com.vegas.tranquilo.utils.CONSTANTS;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.vegas.tranquilo.utils.CONSTANTS.BASIC_AUTH_PASS;
import static com.vegas.tranquilo.utils.CONSTANTS.BASIC_AUTH_USERNAME;

public class RetrofitClient {

    private static final String AUTH = "Basic " + Base64.encodeToString((BASIC_AUTH_USERNAME + ":" + BASIC_AUTH_PASS).getBytes(), Base64.NO_WRAP);

    private static RetrofitClient mInstance;
    private Retrofit retrofit;

    /*  before java 8 syntax

    private RetrofitClient() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(
                        new Interceptor() {
                            @NonNull
                            @Override
                            public Response intercept(@NonNull Chain chain) throws IOException {
                                Request original = chain.request();
                                Request.Builder requestBuilder = original.newBuilder()
                                        .addHeader("Authorization", AUTH)
                                        .method(original.method(), original.body());

                                Request request = requestBuilder.build();
                                return chain.proceed(request);
                            }
                        }
                ).build();
        retrofit = new Retrofit.Builder()
                .baseUrl(CONSTANTS.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();
    }*/

    private RetrofitClient() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(
                        new Interceptor() {
                            @NonNull
                            @Override
                            public Response intercept(@NonNull Chain chain) throws IOException {
                                Request original = chain.request();
                                Request.Builder requestBuilder = original.newBuilder()
                                        .addHeader("Authorization", AUTH)
                                        .method(original.method(), original.body());

                                Request request = requestBuilder.build();
                                return chain.proceed(request);
                            }
                        }

                ).build();
        retrofit = new Retrofit.Builder()
                .baseUrl(CONSTANTS.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();
    }

    public static synchronized RetrofitClient getInstance() {

        if (mInstance == null) {
            mInstance = new RetrofitClient();
        }
        return mInstance;
    }

    public API getApi() {
        return retrofit.create(API.class);
    }

}
