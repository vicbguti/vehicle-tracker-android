package org.onebusaway.tracker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackerScreen(
    onStart: (String, String) -> Unit,
    onStop: () -> Unit,
    onLogout: () -> Unit
) {
    var vehicleId by remember { mutableStateOf("vehicle-001") }
    var tripId by remember { mutableStateOf("route_1_1200") }
    
    // Permission Handling
    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            onStart(vehicleId, tripId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vehicle Tracker") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(text = "Reporting Location", style = MaterialTheme.typography.titleMedium)
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
}
