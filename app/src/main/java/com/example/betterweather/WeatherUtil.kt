package com.example.betterweather

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

object WeatherUtil {
    private val client = OkHttpClient()

    fun getTemperature(lat: Double, lon: Double, apiKey: String): Double? {
        val url = "https://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$lon&units=imperial&appid=$apiKey"
        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            val body = response.body?.string() ?: return null
            val json = JSONObject(body)
            return json.getJSONObject("main").getDouble("temp")
        }
    }
}
