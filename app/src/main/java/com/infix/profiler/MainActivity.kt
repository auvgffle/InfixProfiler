package com.infix.profiler

import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import org.json.JSONException
import org.json.JSONObject

import com.infix.profiler.InfixProfiler

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


//                InfixProfiler.init(
//            appContext = this,
//            appId = "TEST_APP_ID",
//            contact = mapOf("email" to "test@example.com"),
//            options = InfixProfiler.Options(
//                enableDeviceInfo = true,
//                enableNetworkInfo = true,
//                enableLocation = true,
//                enableAdId = true
//            )
//        )


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

    private fun prettyPrintJson(map: Map<String, Any?>?): String {
        return try {
            val json = JSONObject(map)
            json.toString(2)
        } catch (e: JSONException) {
            map?.toString() ?: "No data"
        }
    }
}