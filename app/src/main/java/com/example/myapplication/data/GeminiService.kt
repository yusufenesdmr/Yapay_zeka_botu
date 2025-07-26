package com.example.myapplication.data

import com.example.myapplication.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiService {
    private val model = GenerativeModel(
        modelName = "gemini-1.0-pro", // Güncel model adı
        apiKey = "AIzaSyBxpHFIWEweLcwPqmBXE3e1CzBx488aBl0" // Yeni anahtar
    )

    suspend fun generateResponse(prompt: String): String = withContext(Dispatchers.IO) {
        try {
            val response = model.generateContent(prompt)
            response.text ?: "Üzgünüm, bir cevap üretemiyorum."
        } catch (e: Exception) {
            "Bir hata oluştu: ${e.localizedMessage}"
        }
    }
} 