package ru.javacat.nework.ui.screens


import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.InputDevice
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.GeoObjectTapListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObjectCollection
import ru.javacat.nework.R
import ru.javacat.nework.databinding.ActivityMapBinding


class MapActivity: Activity(), InputListener {

    private lateinit var binding: ActivityMapBinding
    private lateinit var collection:MapObjectCollection

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
        collection = yandexMap.mapObjects.addCollection()
        val bundle = intent.extras
        val location = bundle?.getDoubleArray("LOCATION")
        yandexMap.addInputListener(this)
        println(location.toString())

        if (location != null){
            yandexMap.move(
                CameraPosition(
                    Point(location[0],location[1]),15.0F, 0.0F, 0.0F),
                Animation(Animation.Type.SMOOTH, 0F), null
            )
            //collection.addPlacemark(location)
        } else Toast.makeText(this, "Координаты не верные", Toast.LENGTH_SHORT).show()


    }

    override fun onMapTap(p0: Map, p1: Point) {
        Toast.makeText(this, "${p1.latitude}, ${p1.longitude}", Toast.LENGTH_SHORT).show()
        println("$p1")
    }

    override fun onMapLongTap(p0: Map, p1: Point) {
        Toast.makeText(this, "$p1", Toast.LENGTH_SHORT).show()
        collection.clear()
        collection.addPlacemark(p1)
    }
}