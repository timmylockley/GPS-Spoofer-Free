package com.example.osmspoofer

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.SystemClock
import android.preference.PreferenceManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

class MainActivity : AppCompatActivity() {

    private lateinit var map: MapView
    private var targetPoint: GeoPoint? = null
    private var isSpoofing = false
    private val locationManager by lazy { getSystemService(Context.LOCATION_SERVICE) as LocationManager }
    private val provider = LocationManager.GPS_PROVIDER

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load osmdroid configuration before setting the content view
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        setContentView(R.layout.activity_main)

        // Request permissions so the app doesn't crash on startup
        requestPermissions()

        // Initialize Map
        map = findViewById(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.controller.setZoom(15.0)
        // Default starting location (Brisbane)
        map.controller.setCenter(GeoPoint(-27.4705, 153.0260))

        setupMapEvents()

        // Button Listeners
        findViewById<Button>(R.id.btnStart).setOnClickListener { startSpoofing() }
        findViewById<Button>(R.id.btnStop).setOnClickListener { stopSpoofing() }
    }

    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        val listToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (listToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, listToRequest.toTypedArray(), 1)
        }
    }

    private fun setupMapEvents() {
        val marker = Marker(map)
        val eventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                targetPoint = p
                // Remove old marker and add new one
                map.overlays.removeAll { it is Marker }
                marker.position = p
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                marker.title = "Spoof Target"
                map.overlays.add(marker)
                map.invalidate() // Refresh map

                // Update UI text
                findViewById<TextView>(R.id.statusText).text =
                    "Target: ${String.format("%.5f", p.latitude)}, ${String.format("%.5f", p.longitude)}"
                return true
            }
            override fun longPressHelper(p: GeoPoint): Boolean = false
        }
        map.overlays.add(MapEventsOverlay(eventsReceiver))
    }

    private fun startSpoofing() {
        val p = targetPoint
        if (p == null) {
            Toast.makeText(this, "Please tap the map to select a target first!", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            // Clear existing provider to prevent "already exists" crash
            try { locationManager.removeTestProvider(provider) } catch (e: Exception) {}

            // Register as a test provider
            locationManager.addTestProvider(provider, false, false, false, false, true, true, true, 1, 1)
            locationManager.setTestProviderEnabled(provider, true)

            isSpoofing = true
            Toast.makeText(this, "Spoofing Started", Toast.LENGTH_SHORT).show()

            // Start a background thread to continuously broadcast the location
            Thread {
                while (isSpoofing) {
                    val mockLocation = Location(provider).apply {
                        latitude = p.latitude
                        longitude = p.longitude
                        altitude = 10.0
                        time = System.currentTimeMillis()
                        speed = 0.0f
                        accuracy = 1.0f
                        elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
                    }
                    locationManager.setTestProviderLocation(provider, mockLocation)
                    Thread.sleep(1000) // Broadcast every 1 second
                }
            }.start()

        } catch (e: SecurityException) {
            Toast.makeText(this, "Error: You must enable this app in Developer Options > Mock Location App", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun stopSpoofing() {
        isSpoofing = false
        try {
            locationManager.removeTestProvider(provider)
            Toast.makeText(this, "Spoofing Stopped", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            // Ignore if provider wasn't added
        }
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }
}