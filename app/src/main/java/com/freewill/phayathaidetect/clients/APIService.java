package com.freewill.phayathaidetect.clients;

import fwg.mdc.btc.nursetrackingtest.model.UpdateListResponse;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;


public interface APIService {

    //object body
//    @Headers("Content-Type: application/json")
//    @POST("detect.php")
//    Call<ResponseBody> APIUpdateList(@Body UpdateListResponse updatelist);

    @Headers("Content-Type: application/json")
    @POST("v2detect.php")
    Call<ResponseBody> APIUpdateList(@Body UpdateListResponse updatelist);

    //body
    @POST("/phayathai/api/detect.php")
    Call<ResponseBody> APIUpdateList(@Body RequestBody data);

    //string
    @POST("/phayathai/api/detect.php")
    Call<ResponseBody> APIUpdateList(@Body String raw);
}
