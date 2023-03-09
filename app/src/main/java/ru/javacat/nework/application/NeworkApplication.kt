package ru.javacat.nework.application

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import ru.javacat.nework.auth.AppAuth

@HiltAndroidApp
class NeworkApplication: Application()