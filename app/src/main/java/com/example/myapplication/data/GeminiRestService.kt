package com.example.myapplication.data

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// Yanıt veri modeli

data class GeminiContent(
    @SerializedName("parts") val parts: List<GeminiPart>
)
data class GeminiPart(
    @SerializedName("text") val text: String
)
data class GeminiRequest(
    @SerializedName("contents") val contents: List<GeminiContent>
)
data class GeminiCandidate(
    @SerializedName("content") val content: GeminiContent?
)
data class GeminiResponse(
    @SerializedName("candidates") val candidates: List<GeminiCandidate>?
)

interface GeminiApi {
    @POST("v1/models/gemini-1.5-pro:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body body: GeminiRequest
    ): Response<GeminiResponse>
}

class GeminiRestService(private val apiKey: String) {
    private val retrofit: Retrofit
    private val api: GeminiApi

    init {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
        retrofit = Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
        api = retrofit.create(GeminiApi::class.java)
    }

    suspend fun generateResponse(prompt: String): String {
        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(parts = listOf(GeminiPart(text = prompt)))
            )
        )
        val response = api.generateContent(apiKey, request)
        if (response.isSuccessful) {
            val body = response.body()
            val text = body?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            return text ?: "Yanıt alınamadı."
        } else {
            val error = response.errorBody()?.string() ?: "Bilinmeyen hata"
            return "Hata: $error"
        }
    }
} 