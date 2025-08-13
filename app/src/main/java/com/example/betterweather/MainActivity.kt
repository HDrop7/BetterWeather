package com.example.betterweather

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.core.content.ContextCompat
import com.example.betterweather.ui.theme.BetterWeatherTheme
import com.example.betterweather.BuildConfig
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

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
    var temperature by remember { mutableStateOf<Double?>(null) }

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
            locationText = "Permission denied"
        }
    }

    // Use a separate thread to get the temperature
    val coroutineScope = rememberCoroutineScope()

    fun fetchTemperature(lat: Double, lon: Double) {
        coroutineScope.launch {
            val apiKey = BuildConfig.OPENWEATHER_API_KEY

            val temp = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                WeatherUtil.getTemperature(lat, lon, apiKey)
            }
            if (temp != null) {
                temperature = temp
            } else {
                locationText = "Failed to get temperature"
            }
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
                if (loc != null) {
                    locationText = "Lat: ${loc.latitude}, Lng: ${loc.longitude}"
                    fetchTemperature(loc.latitude, loc.longitude)
                } else {
                    locationText = "Location unavailable"
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
            when {
                temperature != null -> Text("Temperature: ${temperature}°F")
                else -> Text(locationText)
            }
        }
    }
}
