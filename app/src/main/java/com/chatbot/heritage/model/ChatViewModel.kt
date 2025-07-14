package com.chatbot.heritage.model

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private val _typedMessages = mutableStateMapOf<String, String>()
    val typedMessages: Map<String, String> get() = _typedMessages

    fun startTypingEffect(message: String, onUpdate: (String) -> Unit) {
        if (_typedMessages.containsKey(message)) {
            onUpdate(_typedMessages[message]!!) // Use stored message if it exists
            return
        }

        viewModelScope.launch {
            var displayedText = ""
            message.forEachIndexed { index, _ ->
                delay(30) // Typing speed
                displayedText = message.substring(0, index + 1)
                onUpdate(displayedText)
            }
            _typedMessages[message] = displayedText // Store completed message
        }
    }
}
