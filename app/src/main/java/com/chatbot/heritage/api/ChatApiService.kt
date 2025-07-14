package com.chatbot.heritage.api

import com.chatbot.heritage.model.ChatRequest
import com.chatbot.heritage.model.ChatResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ChatApiService {
    @POST("/chat")  // API endpoint
    fun sendMessage(@Body request: ChatRequest): Call<ChatResponse>
}
