package com.example.mediapembelajaran

import android.app.Application

class MediaPembelajaranApp : Application() {
    override fun onCreate() {
        super.onCreate()
        DatabaseHelper.init(this)
    }
}
