package ru.javacat.nework.ui.screens


import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import ru.javacat.nework.R
import ru.javacat.nework.databinding.ActivityMapBinding


class MapActivity: Activity() {

    private lateinit var binding: ActivityMapBinding

    override fun onStop() {
        binding.mapview.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        binding.mapview.onStart()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MapKitFactory.initialize(this)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val defaultLocation = arrayListOf(55.751574, 37.573856)

        val yandexMap = binding.mapview.map
        val bundle = intent.extras
        val location = bundle?.getDoubleArray("LOCATION")
        println(location.toString())

        if (location != null){
            yandexMap.move(
                CameraPosition(
                    Point(location[0],location[1]),15.0F, 0.0F, 0.0F),
                Animation(Animation.Type.SMOOTH, 0F), null
            )
        } else Toast.makeText(this, "Координаты не верные", Toast.LENGTH_SHORT).show()


    }
}