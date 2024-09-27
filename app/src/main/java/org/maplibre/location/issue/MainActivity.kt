package org.maplibre.location.issue

import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.engine.LocationEngine
import org.maplibre.android.location.engine.LocationEngineCallback
import org.maplibre.android.location.engine.LocationEngineRequest
import org.maplibre.android.location.engine.LocationEngineResult
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.location.issue.databinding.ActivityMainBinding

class MainActivity : Activity() {

    private lateinit var binding: ActivityMainBinding
    private var defaultLocationEngine: LocationEngine? = null

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(applicationContext);

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.mapView.getMapAsync { map ->
            map.setStyle("https://tiles.versatiles.org/assets/styles/colorful.json") { style ->
                map.locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(this@MainActivity, style)
                        .build()
                )

                map.locationComponent.renderMode = RenderMode.GPS
                map.locationComponent.isLocationComponentEnabled = true

                map.locationComponent.lastKnownLocation?.let { lastLocation ->
                    map.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                lastLocation.latitude,
                                lastLocation.longitude
                            ), 16.0
                        )
                    )
                }

                defaultLocationEngine = map.locationComponent.locationEngine
            }
        }

        binding.btnToggleLocationUpdates.setOnClickListener {
            binding.mapView.getMapAsync { map ->
                if (map.locationComponent.locationEngine is NonOpLocationEngine) {
                    map.locationComponent.locationEngine = defaultLocationEngine
                    Toast.makeText(this, "Location updates enabled", Toast.LENGTH_SHORT).show()
                } else {
                    map.locationComponent.locationEngine = NonOpLocationEngine
                    Toast.makeText(this, "Location updates disabled", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.mapView.onCreate(savedInstanceState)
    }

    object NonOpLocationEngine : LocationEngine {
        override fun getLastLocation(p0: LocationEngineCallback<LocationEngineResult>) {}

        override fun requestLocationUpdates(p0: LocationEngineRequest, p1: LocationEngineCallback<LocationEngineResult>, p2: Looper?) {}

        override fun requestLocationUpdates(p0: LocationEngineRequest, p1: PendingIntent?) {}

        override fun removeLocationUpdates(p0: LocationEngineCallback<LocationEngineResult>) {}

        override fun removeLocationUpdates(p0: PendingIntent?) {}
    }
}