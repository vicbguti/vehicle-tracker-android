package org.onebusaway.tracker

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dagger.hilt.android.AndroidEntryPoint
import org.onebusaway.tracker.location.LocationService

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TrackerScreen(
                        onStart = { vId, tId -> startLocationService(vId, tId) },
                        onStop = { stopLocationService() }
                    )
                }
            }
        }
    }

    private fun startLocationService(vehicleId: String, tripId: String) {
        val intent = Intent(this, LocationService::class.java).apply {
            putExtra("vehicle_id", vehicleId)
            putExtra("trip_id", tripId)
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopLocationService() {
        val intent = Intent(this, LocationService::class.java)
        stopService(intent)
    }
}

@Composable
fun TrackerScreen(onStart: (String, String) -> Unit, onStop: () -> Unit) {
    var vehicleId by remember { mutableStateOf("vehicle-001") }
    var tripId by remember { mutableStateOf("route_1_1200") }
    
    // Permission Handling
    val context = androidx.compose.ui.platform.LocalContext.current
    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            onStart(vehicleId, tripId)
        }
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Text(text = "OneBusAway Tracker", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = vehicleId,
            onValueChange = { vehicleId = it },
            label = { Text("Vehicle ID") },
            modifier = Modifier.fillMaxWidth()
        )
        
        OutlinedTextField(
            value = tripId,
            onValueChange = { tripId = it },
            label = { Text("Trip ID") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = {
                launcher.launch(arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ))
            }, 
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start Tracking")
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Button(
            onClick = onStop,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Stop Tracking")
        }
    }
}
