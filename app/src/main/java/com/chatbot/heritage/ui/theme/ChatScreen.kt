package com.chatbot.heritage.ui.theme

import ChatMessage
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.chatbot.heritage.R
import com.chatbot.heritage.api.RetrofitClient
import com.chatbot.heritage.model.ChatRequest
import com.chatbot.heritage.model.ChatResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlinx.coroutines.delay


@Composable
fun ChatScreen() {
    var messages by remember { mutableStateOf(listOf(
        ChatMessage("Hello.👋 I'm your new friend, Heritage Bot. Please describe the kind of experience you want.", isUser = false)
    )) }

    var inputText by remember { mutableStateOf(TextFieldValue("")) }
    val listState = rememberLazyListState()
    var showQueryGrid by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .windowInsetsPadding(WindowInsets.ime)
    ) {
        ChatHeader()

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .imePadding(),
            state = listState
        ) {
            items(messages) { message ->
                TypingChatBubble(message.text, message.isUser)
                Spacer(modifier = Modifier.height(6.dp))

                if (message.link != null) {
                    LinkChatBubble(message.link)
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }

        if (showQueryGrid) {
            QueryGrid(onQuerySelected = { selectedQuery ->
                showQueryGrid = false
                messages = messages + ChatMessage(selectedQuery, isUser = true)
                sendMessageToApi(selectedQuery) { botReply, botLink ->
                    messages = messages + ChatMessage(botReply, isUser = false, link = botLink)
                }
            })
        }

        ChatInputField(
            inputText = inputText,
            onTextChanged = { inputText = it
                if (it.text.isNotBlank()) showQueryGrid = false},
            onSend = {
                if (inputText.text.isNotBlank()) {
                    val userMessage = inputText.text
                    messages = messages + ChatMessage(userMessage, isUser = true)
                    inputText = TextFieldValue("")

                    sendMessageToApi(userMessage) { botReply, botLink ->
                        messages = messages + ChatMessage(botReply, isUser = false, link = botLink)
                    }
                }
            }
        )
    }

    LaunchedEffect(messages) {
        delay(100)
        listState.animateScrollToItem(index = messages.size - 1)
    }
}
@Composable
fun QueryGrid(onQuerySelected: (String) -> Unit) {
    val queries = listOf(
        "Book Tickets For me",
        "What are the top exhibits?",
        "What are the museum's opening hours?",
        "Do you have guided tours?"
    )

    Box(
        contentAlignment = Alignment.Center
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(queries) { query ->
                QueryBubble(query, onClick = { onQuerySelected(query) })
            }
        }
    }
}

@Composable
fun QueryBubble(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(10.dp,100.dp)
            .background(Color(0xFF808080), RoundedCornerShape(40.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = text,
            fontSize = 16.sp,
            color = Color.White,
            textAlign = TextAlign.Center)
    }
}



@Composable
fun ChatHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            painter = painterResource(id = R.drawable.ic_logo),
            contentDescription = "App Logo",
            tint = Color.Green,
            modifier = Modifier.size(30.dp)
        )

        Spacer(modifier = Modifier.width(8.dp).padding(40.dp))

        Text(
            text = "Heritage",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun LinkChatBubble(link: String) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        ClickableText(
            text = AnnotatedString(link),
            style = TextStyle(fontSize = 16.sp, color = Color.Cyan, fontWeight = FontWeight.Bold),
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                context.startActivity(intent)
            },
            modifier = Modifier
                .background(Color(0xFF2A2A2A), RoundedCornerShape(15.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}


@Composable
fun TypingChatBubble(message: String, isUser: Boolean) {
    val context = LocalContext.current

    var displayedText by rememberSaveable(message) { mutableStateOf(if (isUser) message else "") }
    var currentIndex by rememberSaveable(message) { mutableIntStateOf(0) }
    var animationCompleted by rememberSaveable(message) { mutableStateOf(isUser) }

    val linkRegex = """(https?://[\w\-.]+\.[a-z]{2,3}(?:/\S*)?)""".toRegex()
    val links = linkRegex.findAll(message).map { it.value }.toList()

    LaunchedEffect(message) {
        if (!isUser && !animationCompleted) {
            displayedText = ""
            currentIndex = 0

            message.forEachIndexed { index, _ ->
                delay(10)
                currentIndex = index + 1
                displayedText = message.substring(0, currentIndex)
            }

            animationCompleted = true
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        SelectionContainer {
            Text(
                text = displayedText,
                fontSize = 16.sp,
                color = Color.White,
                modifier = Modifier
                    .background(
                        color = if (isUser) Color(0xFF4A5EEB) else Color(0xFF2A2A2A),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .animateContentSize()
            )
        }

        links.forEach { link ->
            ClickableText(
                text = AnnotatedString(link, spanStyle = SpanStyle(color = Color.Cyan, fontWeight = FontWeight.Bold)),
                style = TextStyle(fontSize = 16.sp),
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .padding(top = 4.dp)
                    .background(Color(0xFF2A2A2A), RoundedCornerShape(15.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            )
        }
    }
}


@Composable
fun ChatInputField(inputText: TextFieldValue, onTextChanged: (TextFieldValue) -> Unit, onSend: () -> Unit) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color(0xFF2E2E2E), RoundedCornerShape(25.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(8.dp))

        BasicTextField(
            value = inputText,
            onValueChange = { onTextChanged(it) },
            modifier = Modifier
                .weight(1f)
                .background(Color.Gray, RoundedCornerShape(20.dp))
                .padding(12.dp),
            textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Send
            ),
            keyboardActions = KeyboardActions(
                onSend = {
                    if (inputText.text.isNotBlank()) {
                        onSend()
                    }
                }
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            painter = painterResource(id = R.drawable.ic_send_button),
            contentDescription = "Send",
            tint = Color.White,
            modifier = Modifier
                .size(24.dp)
                .clickable {
                    if (inputText.text.isNotBlank()) {
                        onSend()
                    }
                }
        )
    }

    LaunchedEffect(inputText) {
        if (inputText.text.isBlank()) {
            delay(100)
            keyboardController?.hide()
        }
    }
}

fun sendMessageToApi(userMessage: String, onResponse: (String, String?) -> Unit) {
    val call = RetrofitClient.instance.sendMessage(ChatRequest(userMessage))

    call.enqueue(object : Callback<ChatResponse> {
        override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
            if (response.isSuccessful) {
                val botReply = response.body()?.response ?: "Sorry, I didn't understand."
                val botLink = response.body()?.link  // Get link if available

                Log.d("ChatBotResponse", "Bot reply: $botReply, Link: $botLink")
                onResponse(botReply, botLink)

            } else {
                onResponse("Error: ${response.code()}", null)
            }
        }

        override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
            onResponse("Failed to connect. Try again.", null)
        }
    })
}

@Preview(name="Pixel 8",showBackground = true)
@Composable
fun PreviewChatScreen() {
    ChatScreen()
}

