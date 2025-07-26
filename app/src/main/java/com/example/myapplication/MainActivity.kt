package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.ChatMessage
import com.example.myapplication.ui.ChatViewModel
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.foundation.background
import com.example.myapplication.ui.LoginScreen
import com.example.myapplication.ui.LoginViewModel
import com.example.myapplication.ui.AuthState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

class MainActivity : ComponentActivity() {
    private val viewModel: ChatViewModel by viewModels()
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var isLoggedIn by remember { mutableStateOf(false) }
            val authState by loginViewModel.authState.collectAsState()
            var showProfileMenu by remember { mutableStateOf(false) }
            val userEmail = if (authState is AuthState.Success) (authState as AuthState.Success).userEmail else ""

            LaunchedEffect(authState) {
                isLoggedIn = authState is AuthState.Success
            }

            if (!isLoggedIn) {
                viewModel.clearChats()
                LoginScreen(
                    viewModel = loginViewModel,
                    onLoginSuccess = { isLoggedIn = true }
                )
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    ChatScreen(viewModel, onProfileClick = { showProfileMenu = true })
                    // Sağ üstte profil ikonu
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp, end = 12.dp),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        IconButton(onClick = { showProfileMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Profil",
                                tint = Color(0xFF1976D2),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = showProfileMenu,
                            onDismissRequest = { showProfileMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(userEmail.ifBlank { "Profil" }) },
                                onClick = {},
                                enabled = false
                            )
                            DropdownMenuItem(
                                text = { Text("Çıkış Yap", color = Color.Red) },
                                onClick = {
                                    loginViewModel.logout()
                                    showProfileMenu = false
                                    isLoggedIn = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatScreen(viewModel: ChatViewModel, onProfileClick: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    var showHistory by remember { mutableStateOf(false) }

    // Yeni mesaj geldiğinde otomatik scroll
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp)
        ) {
            // Modern AppBar
            Surface(
                tonalElevation = 4.dp,
                shadowElevation = 8.dp,
                color = Color.White,
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CHATGBT",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.Black,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onProfileClick) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profil",
                            tint = Color(0xFF1976D2),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
            // Modern butonlar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { viewModel.startNewChat() },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Yeni Sohbet", color = Color.White)
                }
                Button(
                    onClick = { showHistory = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Sohbet Geçmişi", color = Color.Black)
                }
            }

            // Sohbet geçmişi dialog
            if (showHistory) {
                AlertDialog(
                    onDismissRequest = { showHistory = false },
                    title = { Text("Sohbet Geçmişi", color = Color.Black) },
                    containerColor = Color.White,
                    text = {
                        if (uiState.chatIds.isEmpty()) {
                            Text("Kayıtlı sohbet yok.", color = Color.Black)
                        } else {
                            Column {
                                uiState.chatIds.forEach { chatId ->
                                    Button(
                                        onClick = {
                                            viewModel.switchToChat(chatId)
                                            showHistory = false
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp),
                                        shape = MaterialTheme.shapes.small,
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5F5F5))
                                    ) {
                                        Text(chatId.take(8) + "...", maxLines = 1, color = Color.Black)
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { showHistory = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0))
                        ) {
                            Text("Kapat", color = Color.Black)
                        }
                    }
                )
            }

            // Mesajlar
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.messages, key = { it.id }) { message ->
                    MessageBubbleModernWhite(message)
                }
                if (uiState.isLoading) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(16.dp),
                                color = Color.Black
                            )
                        }
                    }
                }
            }

            // Hata mesajı
            uiState.error?.let { error ->
                Text(
                    text = error,
                    color = Color.Red,
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
                )
            }

            // Giriş alanı
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .background(
                        color = Color(0xFFF5F5F5),
                        shape = MaterialTheme.shapes.medium
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp, end = 8.dp),
                    placeholder = { Text("Sorunuzu yazın...", color = Color(0xFF757575)) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Black,
                        unfocusedIndicatorColor = Color(0xFFB0B0B0),
                        disabledIndicatorColor = Color.Transparent,
                        cursorColor = Color.Black,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        disabledTextColor = Color.Gray
                    ),
                    enabled = !uiState.isLoading
                )
                Button(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            viewModel.sendMessage(messageText)
                            messageText = ""
                        }
                    },
                    enabled = messageText.isNotBlank() && !uiState.isLoading,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.padding(end = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0))
                ) {
                    Text("Gönder", color = Color.Black)
                }
            }
        }
    }
}

@Composable
fun MessageBubbleModernWhite(message: ChatMessage) {
    val isUser = message.user
    val bubbleColor = if (isUser) Color(0xFFF1F1F0) else Color(0xFFE0E0E0)
    val textColor = Color.Black
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    val shape = if (isUser) RoundedCornerShape(16.dp, 0.dp, 16.dp, 16.dp) else RoundedCornerShape(0.dp, 16.dp, 16.dp, 16.dp)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = bubbleColor,
            shape = shape,
            tonalElevation = 2.dp,
            modifier = Modifier
                .widthIn(max = 320.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp)
            ) {
                Text(
                    text = if (isUser) "Ben" else "AI",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isUser) Color(0xFF1976D2) else Color(0xFF388E3C)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = textColor
                )
            }
        }
    }
}