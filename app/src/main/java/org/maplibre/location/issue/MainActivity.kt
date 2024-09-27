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
import org.maplibre.android.location.permissions.PermissionsListener
import org.maplibre.android.location.permissions.PermissionsManager
import org.maplibre.location.issue.databinding.ActivityMainBinding

class MainActivity : Activity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var permissionsManager: PermissionsManager
    private var defaultLocationEngine: LocationEngine? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(applicationContext);

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        permissionsManager = PermissionsManager(object: PermissionsListener {
            override fun onExplanationNeeded(p0: MutableList<String>?) {}

            override fun onPermissionResult(granted: Boolean) {
                if (granted) {
                    init()
                }
            }
        })

        if (!PermissionsManager.areLocationPermissionsGranted(this)) {
            permissionsManager.requestLocationPermissions(this)
        } else {
            init()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    @SuppressLint("MissingPermission")
    private fun init() {
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
    }

    object NonOpLocationEngine : LocationEngine {
        override fun getLastLocation(p0: LocationEngineCallback<LocationEngineResult>) {}

        override fun requestLocationUpdates(p0: LocationEngineRequest, p1: LocationEngineCallback<LocationEngineResult>, p2: Looper?) {}

        override fun requestLocationUpdates(p0: LocationEngineRequest, p1: PendingIntent?) {}

        override fun removeLocationUpdates(p0: LocationEngineCallback<LocationEngineResult>) {}

        override fun removeLocationUpdates(p0: PendingIntent?) {}
    }
}