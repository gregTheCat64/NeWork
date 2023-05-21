package ru.javacat.nework.ui.screens

import android.graphics.PointF
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import dagger.hilt.android.AndroidEntryPoint
import ru.javacat.nework.R
import ru.javacat.nework.databinding.FragmentMapsBinding
import ru.javacat.nework.ui.viewmodels.EventViewModel

@AndroidEntryPoint
class MapsFragment : Fragment(R.layout.fragment_maps), InputListener {
    private lateinit var mapView: MapView
    private lateinit var collection: MapObjectCollection
    private lateinit var mapKit: MapKit

    private val viewModel: EventViewModel by activityViewModels()

    private lateinit var binding: FragmentMapsBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        MapKitFactory.initialize(requireContext())
        super.onCreate(savedInstanceState)
        mapKit = MapKitFactory.getInstance()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentMapsBinding.bind(view)
        mapView = binding.mapview
        collection = mapView.map.mapObjects.addCollection()

        mapView.map.addInputListener(this)

        val userLocationLayer = mapKit.createUserLocationLayer(mapView.mapWindow)
        userLocationLayer.isVisible = true
        userLocationLayer.isHeadingEnabled = true

        val defaultLocation = arrayListOf(55.751574, 37.573856)
        val args = arguments
        val point = args?.getDoubleArray("POINT")

        if (point != null) {
            mapView.map.move(
                CameraPosition(Point(point[0], point[1]), 12.0F, 0.0F, 0.0F),
                Animation(Animation.Type.SMOOTH, 0F), null
            )
            collection.addPlacemark(Point(point[0], point[1]))
        }else {
            mapView.map.move(
                CameraPosition(Point(defaultLocation[0], defaultLocation[1]), 5.0F, 0.0F, 0.0F),
                Animation(Animation.Type.SMOOTH, 0F), null
            )
            Toast.makeText(requireContext(), "Выберите место", Toast.LENGTH_SHORT).show()
        }

        binding.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onMapTap(p0: Map, p1: Point) {
        Toast.makeText(requireContext(), "${p1.latitude}, ${p1.longitude}", Toast.LENGTH_SHORT)
            .show()
        println("$p1")
    }

    override fun onMapLongTap(p0: Map, p1: Point) {
        collection.clear()
        collection.addPlacemark(p1).setIcon(ImageProvider.fromResource(context, R.drawable.ic_baseline_location_on_24))
        val lat = p1.latitude.toString().take(7).toDouble()
        val long = p1.longitude.toString().take(7).toDouble()

        viewModel.setPoint(
            doubleArrayOf(lat,long)
        )
        Toast.makeText(requireContext(), "Точка добавлена", Toast.LENGTH_SHORT).show()
    }

    override fun onStart() {
        MapKitFactory.getInstance().onStart()
        super.onStart()
        (activity as AppCompatActivity).supportActionBar!!.hide()
    }

    override fun onStop() {
        binding.mapview.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
        (activity as AppCompatActivity).supportActionBar!!.show()
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar!!.hide()
    }
}
