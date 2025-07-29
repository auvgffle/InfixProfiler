package com.infix.profiler.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import com.infix.profiler.model.DeviceInfo

interface SdkApiService {
    @POST("get-token")
    suspend fun getToken(@Body payload: Map<String, Any?>): Response<Map<String, Any?>>
    @POST("events")
    suspend fun sendDeviceData(@Body payload: DeviceInfo): Response<Map<String, Any?>>
    @POST("events")
    suspend fun sendDeviceDataMap(@Body payload: Map<String, @JvmSuppressWildcards Any?>): Response<Map<String, Any?>>
}