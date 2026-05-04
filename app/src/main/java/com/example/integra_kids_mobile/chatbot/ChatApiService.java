package com.example.integra_kids_mobile.chatbot;

// ChatApiService.java
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ChatApiService {

    @GET("api/chat/start")
    Call<ChatStartResponse> startSession();

    @POST("api/chat/message")
    Call<BotMessage> sendMessage(@Body ChatMessageRequest request);
}
