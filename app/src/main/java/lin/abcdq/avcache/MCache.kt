package lin.abcdq.avcache

import android.content.Context

class MCache {

    companion object {
        @Volatile
        private var mCacheProxy: CacheProxy? = null

        fun init(context: Context) {
            if (mCacheProxy == null) {
                synchronized(this) {
                    if (mCacheProxy == null) {
                        mCacheProxy = CacheProxy.Builder(context)
                            .maxCacheSize(1024 * 1024 * 512)
                            .maxCacheCount(50)
                            .build()
                    }
                }
            }
        }

        fun proxy(URL: String): String? {
            return mCacheProxy?.proxyUrl(URL)
        }

        fun preProxy(URL: String) {
            mCacheProxy?.preload(URL, 10)
        }
    }
}