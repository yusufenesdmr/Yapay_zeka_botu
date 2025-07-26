package com.example.myapplication

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase

class DersApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        
        // Firebase veritabanı URL'sini ayarla
        FirebaseDatabase.getInstance("https://staj-c0855-default-rtdb.firebaseio.com")
    }
} 