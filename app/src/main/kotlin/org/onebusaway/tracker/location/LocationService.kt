package org.onebusaway.tracker.location

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import org.onebusaway.tracker.api.ApiService
import org.onebusaway.tracker.utils.ConnectivityObserver
import org.onebusaway.tracker.R
import javax.inject.Inject

@AndroidEntryPoint
class LocationService : Service() {

    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var connectivityObserver: ConnectivityObserver

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private var currentStatus: ConnectivityObserver.Status = ConnectivityObserver.Status.Unavailable

    // Hardcoded for skeleton - will be replaced by UI/Auth data
    private var vehicleId = "vehicle-001"
    private var tripId = "route_1_1200"

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        
        serviceScope.launch {
            connectivityObserver.observe().collect { status ->
                currentStatus = status
                android.util.Log.d("LocationService", "Network Status Changed: $status")
            }
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.locations.lastOrNull()?.let { location ->
                    sendLocationToServer(location)
                }
            }
        }
    }

    private fun sendLocationToServer(location: android.location.Location) {
        if (currentStatus != ConnectivityObserver.Status.Available) {
            android.util.Log.w("LocationService", "Skipping report: Network is $currentStatus")
            return
        }

        serviceScope.launch {
            try {
                val report = org.onebusaway.tracker.api.LocationReport(
                    vehicle_id = vehicleId,
                    trip_id = tripId,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    bearing = location.bearing,
                    speed = location.speed,
                    accuracy = location.accuracy,
                    timestamp = System.currentTimeMillis() / 1000
                )
                val response = apiService.postLocation(report)
                if (response.isSuccessful) {
                    android.util.Log.d("LocationService", "Report sent successfully")
                } else {
                    android.util.Log.e("LocationService", "Failed to send report: ${response.code()}")
                }
            } catch (e: Exception) {
                android.util.Log.e("LocationService", "Error sending report", e)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Update IDs from intent if provided
        intent?.getStringExtra("vehicle_id")?.let { vehicleId = it }
        intent?.getStringExtra("trip_id")?.let { tripId = it }

        val notification = NotificationCompat.Builder(this, "location")
            .setContentTitle("OBA Tracker Active")
            .setContentText("Tracking $vehicleId")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .build()
        
        startForeground(1, notification)
        startLocationUpdates()
        
        return START_STICKY
    }

    private fun startLocationUpdates() {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000L).build()
        try {
            fusedLocationClient.requestLocationUpdates(request, locationCallback, null)
        } catch (e: SecurityException) {
            // Handle lack of permissions
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
