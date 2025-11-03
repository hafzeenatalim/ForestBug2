package com.games.forestbug.vkhgm

import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class LauncherActivity : AppCompatActivity() {
    
    private companion object {
        const val PREFS_NAME = "ForestBugPrefs"
        const val TOKEN_KEY = "token"
        const val LINK_KEY = "link"
        const val BASE_ADDRESS = "https://wallen-eatery.space/a-vdm-2/server.php"
        const val PARAM_P = "Jh675eYuunk85"
        const val TAG = "LauncherActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val token = prefs.getString(TOKEN_KEY, null)
        
        if (token != null) {
            val link = prefs.getString(LINK_KEY, "")
            openForestScreen(link ?: "")
        } else {
            makeServerRequest()
        }
    }
    
    private fun makeServerRequest() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val addressString = buildAddressWithParams()
                Log.d(TAG, "Making request to: $addressString")
                
                val address = URL(addressString)
                val connection = address.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                
                val responseCode = connection.responseCode
                Log.d(TAG, "Response code: $responseCode")
                
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = reader.use { it.readText() }
                    Log.d(TAG, "Response: $response")
                    
                    withContext(Dispatchers.Main) {
                        handleResponse(response)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        openMenuActivity()
                    }
                }
                
                connection.disconnect()
            } catch (e: Exception) {
                Log.e(TAG, "Error making request", e)
                withContext(Dispatchers.Main) {
                    openMenuActivity()
                }
            }
        }
    }
    
    private fun buildAddressWithParams(): String {
        val os = "Android ${Build.VERSION.RELEASE}"
        val language = Locale.getDefault().language
        val region = Locale.getDefault().country
        val deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}"
        
        val batteryIntent = registerReceiver(null, android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryLevel = if (level >= 0 && scale > 0) {
            level.toFloat() / scale.toFloat()
        } else {
            0.0f
        }
        
        val statusInt = batteryIntent?.getIntExtra(android.os.BatteryManager.EXTRA_STATUS, -1) ?: -1
        val batteryStatus = when (statusInt) {
            android.os.BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
            android.os.BatteryManager.BATTERY_STATUS_FULL -> "Full"
            android.os.BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            android.os.BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "NotCharging"
            else -> "Unknown"
        }
        
        return "$BASE_ADDRESS?p=$PARAM_P&os=$os&lng=$language&loc=$region&devicemodel=$deviceModel&bs=$batteryStatus&bl=$batteryLevel"
    }
    
    private fun handleResponse(response: String) {
        if (response.contains("#")) {
            val parts = response.split("#", limit = 2)
            val token = parts[0]
            val link = if (parts.size > 1) parts[1] else ""
            
            val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            prefs.edit().apply {
                putString(TOKEN_KEY, token)
                putString(LINK_KEY, link)
                apply()
            }
            
            Log.d(TAG, "Token saved: $token")
            Log.d(TAG, "Link saved: $link")
            
            openForestScreen(link)
        } else {
            openMenuActivity()
        }
    }
    
    private fun openForestScreen(link: String) {
        val intent = Intent(this, ForestActivity::class.java)
        intent.putExtra("LINK", link)
        startActivity(intent)
        finish()
    }
    
    private fun openMenuActivity() {
        val intent = Intent(this, MenuActivity::class.java)
        startActivity(intent)
        finish()
    }
}

