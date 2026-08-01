package com.sila.messaging

import android.app.Application
import com.google.firebase.FirebaseApp

class SilaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
