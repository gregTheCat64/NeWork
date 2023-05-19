package ru.javacat.nework.application

import android.app.Application
import com.yandex.mapkit.MapKitFactory
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.javacat.nework.MapsApiKey.MAPS_API_KEY
import ru.javacat.nework.data.auth.AppAuth
import javax.inject.Inject

@HiltAndroidApp
class NeworkApplication: Application(){
    private val appScope = CoroutineScope(Dispatchers.Default)

    @Inject
    lateinit var auth: AppAuth

    override fun onCreate() {
        super.onCreate()
        MapKitFactory.setApiKey(MAPS_API_KEY)
        //setupAuth()

    }


}