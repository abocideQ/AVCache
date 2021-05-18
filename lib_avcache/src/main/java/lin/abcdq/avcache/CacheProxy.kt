package lin.abcdq.avcache

import android.content.Context
import android.net.Uri
import lin.abcdq.avcache.utils.CacheProxyUtils
import lin.abcdq.avcache.utils.SAFManager
import java.util.*

class CacheProxy(context: Context) {

    class Builder(private val context: Context) {

        private var maxDiskSize = 0L
        private var maxDiskCount = 0

        fun maxCacheSize(max: Long): Builder {
            maxDiskSize = max
            return this
        }

        fun maxCacheCount(max: Int): Builder {
            maxDiskCount = max
            return this
        }

        fun build(): CacheProxy {
            FileCacheLruManager.setMaxDiskSize(maxDiskSize)
            FileCacheLruManager.setMaxDiskCount(maxDiskCount)
            return CacheProxy(context)
        }
    }

    private val mCacheRoot = SAFManager.appStorageObbDir(context)
    private var mCacheProxyServer: CacheProxyServer? = null

    init {
        mCacheProxyServer = try {
            CacheProxyServer(mCacheRoot)
        } catch (e: Exception) {
            null
        }
    }

    fun proxyUrl(url: String): String {
        val cache = FileCache(mCacheRoot, url)
        return if (cache.completed()) Uri.fromFile(cache.file).toString()
        else {
            when {
                mCacheProxyServer == null -> url
                !PingManager.isAlive(CacheProxyServer.HOST, CacheProxyServer.PORT) -> url
                else -> localProxyUrl(url)
            }
        }
    }

    fun preload(url: String, percent: Int) {
        val cache = FileCache(mCacheRoot, url)
        if (cache.completed()) return
        CachePreload.preload(url, percent, cache)
    }

    private fun localProxyUrl(url: String): String {
        return String.format(
            Locale.US,
            "http://%s:%d/%s",
            CacheProxyServer.HOST,
            CacheProxyServer.PORT,
            CacheProxyUtils.encode(url)
        )
    }
}