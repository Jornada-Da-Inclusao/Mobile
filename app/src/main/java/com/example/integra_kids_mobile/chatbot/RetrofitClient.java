package com.example.integra_kids_mobile.chatbot;

// RetrofitClient.java
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "https://chatbot-service-yu32.onrender.com/";
    private static ChatApiService instance;

    public static ChatApiService getInstance() {
        if (instance == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            instance = retrofit.create(ChatApiService.class);
        }
        return instance;
    }
}
