package com.credenceai.app.presentation.ui.screens.crence_chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.RequestOptions
import com.google.ai.client.generativeai.type.content
import com.credenceai.app.BuildConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class CrenceChatUiState(
    val messages: List<ChatMessage> = listOf(
        ChatMessage(
            text = "Hi! I'm Crence, your AI assistant. How can I help you?",
            isUser = false
        )
    ),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val suggestedQuestions: List<String> = listOf(
        "What can you help me with?",
        "Give me some budgeting tips",
        "What is a mutual fund?"
    )
)

@HiltViewModel
class CrenceChatViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(CrenceChatUiState())
    val uiState: StateFlow<CrenceChatUiState> = _uiState.asStateFlow()

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        requestOptions = RequestOptions(apiVersion = "v1beta")
    )

    private val chat = generativeModel.startChat(
        history = listOf(
            content(role = "user") { text("You are Crence, a helpful AI financial assistant for the Credence AI app. Keep your answers concise and friendly.") },
            content(role = "model") { text("Understood. I am Crence, your Credence AI financial assistant. How can I help you today?") }
        )
    )

    fun onInputTextChange(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun onSendMessage(text: String = _uiState.value.inputText) {
        if (text.isBlank()) return

        val userMessage = ChatMessage(text = text, isUser = true)
        _uiState.update { 
            it.copy(
                messages = it.messages + userMessage,
                inputText = "",
                isLoading = true
            )
        }

        viewModelScope.launch {
            try {
                val response = chat.sendMessage(text)
                val aiMessage = ChatMessage(
                    text = response.text ?: "Sorry, I couldn't understand that.",
                    isUser = false
                )
                _uiState.update { 
                    it.copy(
                        messages = it.messages + aiMessage,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                val errorMessage = ChatMessage(
                    text = "Error: ${e.localizedMessage ?: "Failed to get response"}",
                    isUser = false
                )
                _uiState.update { 
                    it.copy(
                        messages = it.messages + errorMessage,
                        isLoading = false
                    )
                }
            }
        }
    }
}
