package com.bioscankit.android.design

data class AppVersion(
    val name: String,
    val code: Long,
) {
    val displayText: String
        get() = "$name ($code)"
}
