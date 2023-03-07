package ru.javacat.nework.application

import android.app.Application
import ru.javacat.nework.auth.AppAuth

class NeworkApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        AppAuth.initApp(this)
    }
}