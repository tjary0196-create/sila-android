package com.sila.messaging

import android.app.Application
import com.sila.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

class SilaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        // App Check: attests that requests hitting Firestore/Functions really come from
        // this app's genuine, unmodified binary on a real device — not a script, an
        // emulator farm, or a rebuilt/patched APK replaying captured traffic. This is
        // independent of (and complements) the per-user authorization enforced by
        // Firestore Rules.
        val appCheck = FirebaseAppCheck.getInstance()
        if (BuildConfig.DEBUG) {
            // Debug builds/emulators can't pass real Play Integrity attestation, so they
            // use the debug provider instead (each debug install gets a token you register
            // once in the Firebase console — see:
            // https://firebase.google.com/docs/app-check/android/debug-provider).
            appCheck.installAppCheckProviderFactory(DebugAppCheckProviderFactory.getInstance())
        } else {
            appCheck.installAppCheckProviderFactory(PlayIntegrityAppCheckProviderFactory.getInstance())
        }
    }
}
