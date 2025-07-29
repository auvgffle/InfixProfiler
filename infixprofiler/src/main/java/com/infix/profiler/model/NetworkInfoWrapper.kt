package com.infix.profiler.model

data class NetworkInfoWrapper(
    val android_network_info: NetworkInfo? = null,
    val ios_network_info: NetworkInfo? = null,
    val network_info: NetworkInfo? = null
)