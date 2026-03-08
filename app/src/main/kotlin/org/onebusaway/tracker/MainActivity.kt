package org.onebusaway.tracker

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import org.onebusaway.tracker.data.SessionEvent
import org.onebusaway.tracker.data.SessionManager
import org.onebusaway.tracker.location.LocationService
import org.onebusaway.tracker.ui.TrackerScreen
import org.onebusaway.tracker.ui.login.LoginScreen
import org.onebusaway.tracker.ui.login.LoginViewModel
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var isLoggedIn by remember { mutableStateOf(sessionManager.isLoggedIn()) }

                    LaunchedEffect(Unit) {
                        sessionManager.sessionEvents.collect { event ->
                            if (event is SessionEvent.SessionExpired) {
                                isLoggedIn = false
                            }
                        }
                    }

                    if (isLoggedIn) {
                        TrackerScreen(
                            onStart = { vId, tId -> startLocationService(vId, tId) },
                            onStop = { stopLocationService() },
                            onLogout = {
                                sessionManager.clearSession()
                                isLoggedIn = false
                            }
                        )
                    } else {
                        LoginScreen(
                            viewModel = loginViewModel,
                            onLoginSuccess = { isLoggedIn = true }
                        )
                    }
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
