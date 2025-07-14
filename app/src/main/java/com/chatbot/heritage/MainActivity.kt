package com.chatbot.heritage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.chatbot.heritage.ui.theme.ChatScreen
import com.chatbot.heritage.ui.theme.HeritageTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HeritageTheme {
                ChatScreen()
            }
        }
    }
}
