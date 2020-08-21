package com.example.localdelivery.Interface;

import com.example.localdelivery.model.CancelOrderData;
import com.example.localdelivery.model.FavData;
import com.example.localdelivery.model.FavResponse;
import com.example.localdelivery.model.FeedbackData;
import com.example.localdelivery.model.LoginData;
import com.example.localdelivery.model.LoginResponse;
import com.example.localdelivery.model.NearbyShopsData;
import com.example.localdelivery.model.NearbyShopsResponse;
import com.example.localdelivery.model.OrdersResponse;
import com.example.localdelivery.model.OtpData;
import com.example.localdelivery.model.OtpResponse;
import com.example.localdelivery.model.PayTmCheckSumData;
import com.example.localdelivery.model.PayTmCheckSumResponse;
import com.example.localdelivery.model.PlaceOrderData;
import com.example.localdelivery.model.ResendOtpResponse;
import com.example.localdelivery.model.ReviewData;
import com.example.localdelivery.model.SignUpData;
import com.example.localdelivery.model.SignUpResponse;

import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface JsonApiHolder {

    @POST("user/signUp")
    Single<SignUpResponse> signUp(@Body SignUpData signUpData);

    @POST("user/otpVerify/{userId}")
    Single<OtpResponse> verifyOtp(@Path("userId") String id, @Body OtpData otpData);

    @POST("user/login")
    Single<LoginResponse> login(@Body LoginData loginData);

    @POST("user/otpResend/{userId}")
    Single<ResendOtpResponse> resendOtp(@Path("userId") String id);

    @PUT("shopAction/getNearbyShops")
    Single<NearbyShopsResponse> getNearbyShops(@Body NearbyShopsData nearbyShopsData);

    @POST("customerAction/placeOrder")
    Single<ResponseBody> placeOrder(@Body PlaceOrderData placeOrderData);

    @POST("customerAction/postReview")
    Single<ResponseBody> postReview(@Body ReviewData reviewData);

    @POST("customerAction/postFeedback")
    Single<ResponseBody> postFeedback(@Body FeedbackData feedbackData);

    @GET("customerAction/getOrders")
    Single<OrdersResponse> getOrders();

    @POST("customerAction/paisa")
    Single<PayTmCheckSumResponse> getPayTmCheckSum(@Body PayTmCheckSumData payTmCheckSumData);

    @PUT("customerAction/fav")
    Single<FavResponse> favourite(@Body FavData favData);

    @PUT("customerAction/changeStatus")
    Single<ResponseBody> cancelOrder(@Body CancelOrderData cancelOrderData);
}
