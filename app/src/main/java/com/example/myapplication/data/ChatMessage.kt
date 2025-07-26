package com.example.myapplication.data

data class ChatMessage(
    val id: String = "",
    val content: String = "",
    val user: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
) {
    constructor() : this("", "", false, 0)
} 