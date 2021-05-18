package lin.abcdq.avcache

import lin.abcdq.avcache.utils.Logger
import java.io.InputStream
import java.util.concurrent.Executors
import okhttp3.Call
import okhttp3.Request
import okhttp3.Response

internal class CachePreload(
    private val originUrl: String,
    private val percent: Int,
    private val cache: FileCache
) {

    companion object {

        private val mPreThread = Executors.newCachedThreadPool()
        private val preMap = HashMap<String, CachePreload>()
        fun preloading(originUrl: String): Boolean {
            if (preMap[originUrl] != null) return true
            return false
        }

        fun preload(originUrl: String, percent: Int, cache: FileCache) {
            mPreThread.execute {
                try {
                    if (preloading(originUrl)) return@execute
                    preMap[originUrl] = CachePreload(originUrl, percent, cache)
                    preMap[originUrl]?.run()
                } catch (e: Exception) {
                    Logger.e("CachePreload", "$e")
                } finally {
                    preMap[originUrl]?.interrupt()
                    preMap.remove(originUrl)
                }
            }
        }

        fun interrupt(originUrl: String) {
            preMap[originUrl]?.interrupt()
        }
    }

    fun interrupt() {
        try {
            call?.cancel()
            response?.close()
            inStream?.close()
        } catch (e: Exception) {
            Logger.e("CachePreload", "$e")
        } finally {
            try {
                cache.close()
                interrupt = true
                Thread.currentThread().interrupt()
            } catch (e: Exception) {
                Logger.e("CachePreload", "$e")
            }
        }
    }

    private fun interrupted(): Boolean {
        return interrupt || Thread.currentThread().isInterrupted
    }

    private var interrupt = false

    private var call: Call? = null
    private var response: Response? = null
    private var inStream: InputStream? = null

    fun run() {
        try {
            if (interrupted()) return
            val infoSource = RequestManager.infoSource(originUrl)
            val length = infoSource.length * (percent / 100.0)
            if (interrupted()) return
            val request = Request.Builder().url(originUrl).build()
            call = RequestClient.instanceClient().newCall(request)
            response = call!!.execute()
            if (interrupted()) return
            if (!response!!.isSuccessful) return
            inStream = response!!.body!!.byteStream()
            val bytes = ByteArray(1024)
            var offset = 0L
            while (!interrupted()) {
                val len = inStream!!.read(bytes)
                if (len == -1) break
                if (offset >= length) break
                if (interrupted()) break
                cache.write(bytes, offset, len)
                offset += len

            }
        } catch (e: Exception) {
            throw e
        }
    }
}