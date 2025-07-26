package com.example.myapplication.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val userEmail: String = "") : AuthState()
    data class Error(val message: String) : AuthState()
    object Registered : AuthState() // Kayıt başarılı ama giriş yapılmadı
    data class PasswordResetSent(val email: String) : AuthState()
}

class LoginViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private fun firebaseErrorToTurkish(message: String?): String {
        return when {
            message?.contains("The email address is badly formatted", true) == true -> "Geçersiz e-posta adresi."
            message?.contains("There is no user record", true) == true -> "Bu e-posta ile kayıtlı kullanıcı bulunamadı."
            message?.contains("The password is invalid", true) == true -> "Şifre yanlış."
            message?.contains("The email address is already in use", true) == true -> "Bu e-posta adresi zaten kullanılıyor."
            message?.contains("Password should be at least", true) == true -> "Şifre en az 6 karakter olmalı."
            message?.contains("A network error", true) == true -> "Ağ bağlantı hatası."
            else -> message ?: "Bir hata oluştu."
        }
    }

    fun login(email: String, password: String) {
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Success(email)
                } else {
                    _authState.value = AuthState.Error(firebaseErrorToTurkish(task.exception?.localizedMessage))
                }
            }
    }

    fun register(email: String, password: String) {
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Registered
                } else {
                    _authState.value = AuthState.Error(firebaseErrorToTurkish(task.exception?.localizedMessage))
                }
            }
    }

    fun logout() {
        auth.signOut()
        _authState.value = AuthState.Idle
    }

    fun checkCurrentUser() {
        val user = auth.currentUser
        if (user != null) {
            _authState.value = AuthState.Success(user.email ?: "")
        } else {
            _authState.value = AuthState.Idle
        }
    }

    fun sendPasswordReset(email: String) {
        _authState.value = AuthState.Loading
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.PasswordResetSent(email)
                } else {
                    _authState.value = AuthState.Error(firebaseErrorToTurkish(task.exception?.localizedMessage))
                }
            }
    }
} 