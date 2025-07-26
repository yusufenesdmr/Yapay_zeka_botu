package com.example.myapplication.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit
) {
    val authState by viewModel.authState.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoginMode by remember { mutableStateOf(true) }
    var showRegisterSuccess by remember { mutableStateOf(false) }
    var showResetSent by remember { mutableStateOf(false) }
    var resetSentEmail by remember { mutableStateOf("") }

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onLoginSuccess()
        }
        if (authState is AuthState.Registered) {
            showRegisterSuccess = true
            isLoginMode = true
            password = ""
        }
        if (authState is AuthState.PasswordResetSent) {
            showResetSent = true
            resetSentEmail = (authState as AuthState.PasswordResetSent).email
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F6FA)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .shadow(16.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo/Icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0xFF1976D2), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = if (isLoginMode) "Giriş Yap" else "Kayıt Ol",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFF222222),
                    fontSize = 26.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                ModernTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "E-posta",
                    icon = Icons.Default.MailOutline,
                    isPassword = false
                )
                Spacer(modifier = Modifier.height(8.dp))
                ModernTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Şifre",
                    icon = Icons.Default.Lock,
                    isPassword = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { isLoginMode = !isLoginMode }) {
                    Text(
                        if (isLoginMode) "Hesabınız yok mu? Kayıt Olun" else "Zaten hesabınız var mı? Giriş Yapın",
                        color = Color(0xFF1976D2)
                    )
                }
                if (isLoginMode) {
                    TextButton(
                        onClick = { viewModel.sendPasswordReset(email) },
                        enabled = email.isNotBlank()
                    ) {
                        Text("Şifremi Unuttum", color = Color(0xFF1976D2))
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {
                        if (isLoginMode) {
                            viewModel.login(email, password)
                        } else {
                            viewModel.register(email, password)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                    enabled = email.isNotBlank() && password.isNotBlank() && authState != AuthState.Loading
                ) {
                    Text(if (isLoginMode) "Giriş Yap" else "Kayıt Ol", color = Color.White, fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.height(10.dp))
                if (showRegisterSuccess) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFF388E3C),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Kayıt başarılı! Şimdi giriş yapabilirsiniz.", color = Color(0xFF388E3C))
                    }
                }
                if (showResetSent) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MailOutline,
                            contentDescription = null,
                            tint = Color(0xFF388E3C),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Şifre sıfırlama e-postası gönderildi: $resetSentEmail", color = Color(0xFF388E3C))
                    }
                }
                if (authState is AuthState.Error) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text((authState as AuthState.Error).message, color = Color.Red)
                    }
                }
                if (authState is AuthState.Loading) {
                    Spacer(modifier = Modifier.height(8.dp))
                    CircularProgressIndicator(color = Color(0xFF1976D2))
                }
            }
        }
    }
}

@Composable
fun ModernTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    isPassword: Boolean
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        leadingIcon = {
            Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF1976D2))
        },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedIndicatorColor = Color(0xFF1976D2),
            unfocusedIndicatorColor = Color(0xFFB0B0B0),
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black
        )
    )
} 