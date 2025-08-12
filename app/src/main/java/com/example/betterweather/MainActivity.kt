package com.example.betterweather

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.betterweather.ui.theme.BetterWeatherTheme
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BetterWeatherTheme {
                WeatherApp()
            }
        }
    }
}

@Composable
fun WeatherApp() {
    val context = LocalContext.current
    var locationText by remember { mutableStateOf("Waiting for location...")}

    val fusedLocationClient = remember {
        com.google.android.gms.location.LocationServices
            .getFusedLocationProviderClient(context)
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult (
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineGranted  || coarseGranted) {
            fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                locationText = if (loc != null) {
                    "Lat: ${loc.latitude}, Lng: ${loc.longitude}"
                } else {
                    "Location unavailable"
                }
            }
        } else {
            // permission denied
            locationText = "Permission denied"
        }
    }

    // Run once when the app launches
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                locationText = if (loc != null) {
                    "Lat: ${loc.latitude}, Lng: ${loc.longitude}"
                } else {
                    "Location unavailable"
                }
            }
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
        Box(modifier = Modifier
            .padding(padding)
            .padding(16.dp)
            ) {
            Text(locationText)
        }
    }
}
