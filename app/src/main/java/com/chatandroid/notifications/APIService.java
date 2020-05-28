package com.chatandroid.notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type: application/json",
                    "Authorization:key=AAAAvY54mFY:APA91bGxkEHO0sASJO7RY33yuDDwffRZBYQlL22EqVEzO-NZkS5YgTAyvxQeq4-XjWJNutX4XRz2LQb6hCuEf8_kbaRlcQpIDTsDYA1d97_s2bFUHBlXMDTEAJwCMsWdbbSfymwLxpn4"
            }
    )
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
