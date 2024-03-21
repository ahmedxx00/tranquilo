package com.vegas.tranquilo.api;

import com.google.gson.JsonObject;
import com.vegas.tranquilo.models.GiftResponse;
import com.vegas.tranquilo.models.MainResponse;
import com.vegas.tranquilo.models.SimpleResponse;

import java.util.Date;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface API {

//    @GET("check/talkToServer")
//    Call<MainResponse> talkToServer(
//            @Query("phone") String phone,
//            @Query("nowTime") Date nowTime,
//            @Query("APP_VERSION") String APP_VERSION
//    );


    @GET("check/ifRegistrationCodeValid")
    Call<SimpleResponse> checkIfRegistrationCodeValid(
            @Query("code") String code
    );

    @POST("register/user")
    @FormUrlEncoded
    Call<SimpleResponse> registerUser(
            @Field("phone") String phone,
            @Field("password") String password,
            @Field("code") String code
    );

    @POST("login/user")
    @FormUrlEncoded
    Call<SimpleResponse> loginUser(
            @Field("phone") String phone,
            @Field("password") String password
    );

    //---------------------------------------------------------------


    @GET("check/lastOrder")
    Call<MainResponse> checkLastOrder(
            @Query("order_phone") String order_phone,
            @Query("login_phone") String login_phone,
            @Query("app_name") String app_name,
            @Query("APP_VERSION") String APP_VERSION
    );


    @GET("check/ifPointLiesWithinPolygon")
    Call<MainResponse> checkPointLiesInArea(
            @Query("lat") double lat,
            @Query("lng") double lng,
            @Query("nowTime") Date nowTime
    );

    @PUT("update/canceled_by_user")
    @FormUrlEncoded
    Call<SimpleResponse> canceledByUser(
            @Field("_id") String _id
    );


    @POST("order/create")
    @FormUrlEncoded
    Call<SimpleResponse> sendOrder(
            @Field("sentOrder") JsonObject sentOrder
    );

    @POST("add/userFeedback")
    @FormUrlEncoded
    Call<SimpleResponse> sendFeedback(
            @Field("phone") String phone,
            @Field("feedback") String feedback
    );

    @GET("getsingle/userGift")
    Call<GiftResponse> getUserGift(
            @Query("phone") String phone // login_phone
    );

    @PUT("update/userGiftAddWalletNumber")
    @FormUrlEncoded
    Call<SimpleResponse> userGiftAddWalletNumber(
            @Field("giftId") String giftId,
            @Field("wallet_number") String wallet_number
    );



}
