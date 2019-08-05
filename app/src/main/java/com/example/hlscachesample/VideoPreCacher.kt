package com.example.hlscachesample

import android.net.Uri

interface VideoPreCacher {
    fun preCache(uri: Uri, position: Long = 0L)
}
