package com.example.grandchroniclerapp

import android.app.Application
import com.example.grandchroniclerapp.repository.AppContainer
import com.example.grandchroniclerapp.repository.DefaultAppContainer

class GrandChroniclerApp : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        // Kirim 'this' sebagai context ke DefaultAppContainer
        container = DefaultAppContainer(this)
    }
}