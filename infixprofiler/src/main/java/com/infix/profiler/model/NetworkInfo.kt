package com.infix.profiler.model

data class NetworkInfo(
    val allowsVOIP: Boolean? = null,
    val carrierName: String? = null,
    val isConnected: Boolean? = null,
    val isoCountryCode: String? = null,
    val mobileCountryCode: String? = null,
    val mobileNetworkCode: String? = null,
    val networkType: String? = null,
    val publicIp: String? = null
)