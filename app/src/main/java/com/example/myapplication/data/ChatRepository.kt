package com.example.myapplication.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.UUID

class ChatRepository {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance("https://staj-c0855-default-rtdb.firebaseio.com").apply {
        setPersistenceEnabled(true)
    }
    private fun userChatsRef(): com.google.firebase.database.DatabaseReference {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "public"
        return database.getReference("user_chats").child(userId)
    }

    fun getAllChats(): Flow<List<String>> = callbackFlow {
        val ref = userChatsRef()
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val chatIds = snapshot.children.mapNotNull { it.key }
                trySend(chatIds)
            }
            override fun onCancelled(error: DatabaseError) {
                trySend(emptyList())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun getMessages(chatId: String): Flow<List<ChatMessage>> = callbackFlow {
        val messagesRef = userChatsRef().child(chatId).child("messages")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = snapshot.children.mapNotNull {
                    it.getValue(ChatMessage::class.java)
                }.sortedBy { it.timestamp }
                trySend(messages)
            }
            override fun onCancelled(error: DatabaseError) {
                trySend(emptyList())
            }
        }
        messagesRef.addValueEventListener(listener)
        awaitClose { messagesRef.removeEventListener(listener) }
    }

    suspend fun addMessage(chatId: String, content: String, user: Boolean) {
        val messagesRef = userChatsRef().child(chatId).child("messages")
        val message = ChatMessage(
            id = UUID.randomUUID().toString(),
            content = content,
            user = user,
            timestamp = System.currentTimeMillis()
        )
        messagesRef.child(message.id).setValue(message)
    }
} 