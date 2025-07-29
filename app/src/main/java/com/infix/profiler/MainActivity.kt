//package com.infix.profiler
//
//import android.os.Bundle
//import android.widget.ScrollView
//import android.widget.TextView
//import androidx.appcompat.app.AppCompatActivity
//import kotlinx.coroutines.*
//import org.json.JSONObject
//import org.json.JSONException
//import android.Manifest
//import android.content.pm.PackageManager
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import android.widget.Button
//import android.widget.LinearLayout
//
//class MainActivity : AppCompatActivity() {
//    private val mainScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
//    private lateinit var textView: TextView
//    private lateinit var errorLogView: TextView
//    private lateinit var responseLogView: TextView
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        textView = TextView(this)
//        textView.text = "Loading payload..."
//        textView.setPadding(24, 24, 24, 24)
//        textView.textSize = 14f
//        val scrollView = ScrollView(this)
//        scrollView.addView(textView)
//
//        errorLogView = TextView(this)
//        errorLogView.text = ""
//        errorLogView.setPadding(24, 0, 24, 24)
//        errorLogView.textSize = 12f
//        errorLogView.setTextColor(0xFFFF4444.toInt())
//
//        responseLogView = TextView(this)
//        responseLogView.text = ""
//        responseLogView.setPadding(24, 0, 24, 24)
//        responseLogView.textSize = 12f
//        responseLogView.setTextColor(0xFF33B5E5.toInt())
//
//        val sendButton = Button(this)
//        sendButton.text = "Send Data Now"
//        sendButton.setOnClickListener {
//            mainScope.launch {
//                try {
//                    val response = withContext(Dispatchers.IO) { InfixProfiler.sendData(); InfixProfiler.lastApiResponse }
//                    val payload = withContext(Dispatchers.IO) { InfixProfiler.getCurrentPayload() }
//
//                    textView.text = prettyPrintJson(payload)
//                    val health = InfixProfiler.healthCheck()
//                    val error = health["lastError"]?.toString() ?: "No errors"
//                    errorLogView.text = "Last Error: $error"
//                    responseLogView.text = "API Response: ${response ?: "No response"}"
//                } catch (e: Exception) {
//                    errorLogView.text = "Exception: ${e.localizedMessage}"
//                    responseLogView.text = "API Response: Exception: ${e.localizedMessage}"
//                }
//            }
//        }
//
//        val mainLayout = LinearLayout(this)
//        mainLayout.orientation = LinearLayout.VERTICAL
//        mainLayout.addView(sendButton)
//        mainLayout.addView(scrollView)
//        mainLayout.addView(errorLogView)
//        mainLayout.addView(responseLogView)
//        setContentView(mainLayout)
//
//        requestLocationPermissionIfNeeded()
//
//        mainScope.launch {
//            val payload = withContext(Dispatchers.IO) { InfixProfiler.getCurrentPayload() }
//            textView.text = prettyPrintJson(payload)
//            val health = InfixProfiler.healthCheck()
//            val error = health["lastError"]?.toString() ?: "No errors"
//            errorLogView.text = "Last Error: $error"
//        }
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        mainScope.cancel()
//    }
//
//    private fun requestLocationPermissionIfNeeded() {
//        val permissions = arrayOf(
//            Manifest.permission.ACCESS_FINE_LOCATION,
//            Manifest.permission.ACCESS_COARSE_LOCATION
//        )
//        val missing = permissions.filter {
//            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
//        }
//        if (missing.isNotEmpty()) {
//            ActivityCompat.requestPermissions(this, missing.toTypedArray(), 1001)
//        }
//    }
//
//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == 1001) {
//            // Refresh payload after permission is granted
//            mainScope.launch {
//                val payload = withContext(Dispatchers.IO) { InfixProfiler.getCurrentPayload() }
//                textView.text = prettyPrintJson(payload)
//            }
//        }
//    }
//
//    private fun prettyPrintJson(map: Map<String, Any?>?): String {
//        return try {
//            val json = JSONObject(map ?: emptyMap<String, Any?>())
//            json.toString(2)
//        } catch (e: JSONException) {
//            map?.toString() ?: "No data"
//        }
//    }
//}




package com.infix.profiler

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import org.json.JSONException
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private val mainScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var textView: TextView
    private lateinit var errorLogView: TextView
    private lateinit var responseLogView: TextView

    companion object {
        const val TAG = "InfixProfiler"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // UI Components
        textView = TextView(this).apply {
            text = "Loading payload..."
            setPadding(24, 24, 24, 24)
            textSize = 14f
        }

        val scrollView = ScrollView(this).apply {
            addView(textView)
        }

        errorLogView = TextView(this).apply {
            text = ""
            setPadding(24, 0, 24, 24)
            textSize = 12f
            setTextColor(0xFFFF4444.toInt()) // Red
        }

        responseLogView = TextView(this).apply {
            text = ""
            setPadding(24, 0, 24, 24)
            textSize = 12f
            setTextColor(0xFF33B5E5.toInt()) // Blue
        }

        val sendButton = Button(this).apply {
            text = "Send Data Now"
            setOnClickListener {
                mainScope.launch {
                    try {
                        val payload = withContext(Dispatchers.IO) { InfixProfiler.getCurrentPayload() }
                        Log.d(TAG, "Payload: ${JSONObject(payload).toString(2)}")

                        val response = withContext(Dispatchers.IO) {
                            InfixProfiler.sendData()
                            InfixProfiler.lastApiResponse
                        }

                        Log.d(TAG, "API Response: $response")

                        val health = InfixProfiler.healthCheck()
                        val error = health["lastError"]?.toString() ?: "No errors"

                        // Display on UI
                        textView.text = "📦 Payload:\n${prettyPrintJson(payload)}\n\n✅ API Response:\n$response"
                        errorLogView.text = "Last Error: $error"
                        responseLogView.text = "API Response: ${response ?: "No response"}"

                    } catch (e: Exception) {
                        Log.e(TAG, "Exception while sending data", e)
                        errorLogView.text = "Exception: ${e.localizedMessage}"
                        responseLogView.text = "API Response: Exception: ${e.localizedMessage}"
                    }
                }
            }
        }

        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(sendButton)
            addView(scrollView)
            addView(errorLogView)
            addView(responseLogView)
        }

        setContentView(mainLayout)

        requestLocationPermissionIfNeeded()

        mainScope.launch {
            val payload = withContext(Dispatchers.IO) { InfixProfiler.getCurrentPayload() }
            Log.d(TAG, "Initial Payload: ${JSONObject(payload).toString(2)}")
            textView.text = "📦 Payload:\n${prettyPrintJson(payload)}"

            val health = InfixProfiler.healthCheck()
            val error = health["lastError"]?.toString() ?: "No errors"
            errorLogView.text = "Last Error: $error"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mainScope.cancel()
    }

    private fun requestLocationPermissionIfNeeded() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        val missing = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missing.toTypedArray(), 1001)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            mainScope.launch {
                val payload = withContext(Dispatchers.IO) { InfixProfiler.getCurrentPayload() }
                Log.d(TAG, "Updated Payload after permission grant: ${JSONObject(payload).toString(2)}")
                textView.text = "📦 Payload:\n${prettyPrintJson(payload)}"
            }
        }
    }

    private fun prettyPrintJson(map: Map<String, Any?>?): String {
        return try {
            val json = JSONObject(map)
            json.toString(2)
        } catch (e: JSONException) {
            map?.toString() ?: "No data"
        }
    }
}