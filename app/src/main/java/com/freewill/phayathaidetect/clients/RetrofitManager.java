package com.freewill.phayathaidetect.clients;

import com.freewill.phayathaidetect.listener.OnSessionExpiredListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class RetrofitManager {
    private static RetrofitManager instance;

    public static RetrofitManager getInstance() {
        if (instance == null)
            instance = new RetrofitManager();
        return instance;
    }


    public OnSessionExpiredListener onSessionExpiredListener = null;
    private  OkHttpClient.Builder okHttpClient;

//    private static final String BASE_URL = "http://freewillmdc.loginto.me:56870/phayathaiv2/api/";
    private static final String BASE_URL = "http://10.32.10.71/phayathai_nurse_tracking_backend/api/";

    private Retrofit retrofit = null;
    private APIService service;

    public static String getBASE_URL() {
        return BASE_URL;
    }


    public RetrofitManager() {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        if (retrofit == null) {
            //OkHttpClient.Builder client = new OkHttpClient.Builder();
//            OkHttpClient.Builder okHttpClient = new OkHttpClient().newBuilder()
//                    .connectTimeout(30, TimeUnit.SECONDS)
//                    .readTimeout(30, TimeUnit.SECONDS)
//                    .writeTimeout(30, TimeUnit.SECONDS);
//            okHttpClient.interceptors().add(new AddCookiesInterceptor());
//            okHttpClient.interceptors().add(new ReceivedCookiesInterceptor());

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(Unsafe_CA_SSL.ConfigureSSLSocket())
//                    .client(okHttpClient.build())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        service = retrofit.create(APIService.class);
    }

//    public  Retrofit getRetrofit() {
//        return retrofit;
//    }

//    private RetrofitManager() {
//
////        service = getRetrofit().create(APIService.class);
//    }

    public APIService getAPIService() {
        return service;
    }

}
