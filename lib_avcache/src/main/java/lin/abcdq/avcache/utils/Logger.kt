package lin.abcdq.avcache.utils

import android.util.Log

internal object Logger {

    fun e(tag: String, e: String) {
        Log.e("CacheProxy", "$tag : $e")
    }

}