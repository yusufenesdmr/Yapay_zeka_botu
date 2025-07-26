package com.example.myapplication.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.ChatMessage
import com.example.myapplication.data.ChatRepository
import com.example.myapplication.data.GeminiRestService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.UUID

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val chatIds: List<String> = emptyList(),
    val activeChatId: String = ""
)

class ChatViewModel : ViewModel() {
    private val geminiService = GeminiRestService("AIzaSyDbEIZPHldn6amGAZOpl2ASNEBEsR21mrQ")
    private val chatRepository = ChatRepository()

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        // Varsayılan yeni sohbet aç
        startNewChat()
        // Sohbet geçmişini dinle
        viewModelScope.launch {
            chatRepository.getAllChats().collect { chatIds ->
                _uiState.value = _uiState.value.copy(chatIds = chatIds)
            }
        }
    }

    private fun observeMessages(chatId: String) {
        viewModelScope.launch {
            chatRepository.getMessages(chatId)
                .catch { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Bir hata oluştu"
                    )
                }
                .collect { messages ->
                    _uiState.value = _uiState.value.copy(
                        messages = messages,
                        error = null
                    )
                }
        }
    }

    fun startNewChat() {
        val newChatId = UUID.randomUUID().toString()
        _uiState.value = _uiState.value.copy(
            activeChatId = newChatId,
            messages = emptyList(),
            error = null
        )
        observeMessages(newChatId)
    }

    fun switchToChat(chatId: String) {
        _uiState.value = _uiState.value.copy(
            activeChatId = chatId,
            error = null
        )
        observeMessages(chatId)
    }

    fun sendMessage(message: String) {
        if (message.isBlank()) return
        val chatId = _uiState.value.activeChatId
        if (chatId.isBlank()) return
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                // Kullanıcı mesajını kaydet
                chatRepository.addMessage(chatId, message, user = true)
                // Gemini'den yanıt al (REST ile)
                val response = geminiService.generateResponse(message)
                // AI yanıtını kaydet
                chatRepository.addMessage(chatId, response, user = false)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Bir hata oluştu"
                )
            }
        }
    }

    fun clearChats() {
        _uiState.value = ChatUiState()
    }
} 