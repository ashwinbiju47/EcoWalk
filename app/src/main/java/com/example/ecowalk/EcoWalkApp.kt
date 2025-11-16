package com.example.ecowalk

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.jakewharton.threetenabp.AndroidThreeTen

class EcoWalkApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        AndroidThreeTen.init(this)
        Log.d("Eco Walk", "Firebase initialized correctly")
    }
}