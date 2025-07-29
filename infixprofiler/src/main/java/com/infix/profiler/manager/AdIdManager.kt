package com.infix.profiler.manager

import android.content.Context
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import java.util.concurrent.Executors

class AdIdManager(private val context: Context) {

    interface Callback {
        fun onSuccess(adId: String)
        fun onFailure(errorMessage: String)
    }

    fun fetchAdId(callback: Callback) {
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            try {
                val adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context)
                val adId = adInfo.id
                if (!adId.isNullOrEmpty()) {
                    callback.onSuccess(adId)
                } else {
                    callback.onFailure("Ad ID is null or empty")
                }
            } catch (e: Exception) {
                callback.onFailure("Error retrieving Ad ID: ${e.message}")
            }
        }
    }
}