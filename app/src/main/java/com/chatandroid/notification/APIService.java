package com.chatandroid.notification;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type: application/json",
                    "Authorization:key=AAAA-fSY97k:APA91bEGGF6ZQ0N76yJg1Jw3HwCs1FP6b_9SWHrmKKvdOnGZKZypTNqePmerY_5bF7sIIIaT1xAipxZgGFhOn8jARFzPToZ3DR_8H1fi-SyeZiW40PaWXl5CUg7MOvZknlABqlc08bz2"
            }
    )
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
