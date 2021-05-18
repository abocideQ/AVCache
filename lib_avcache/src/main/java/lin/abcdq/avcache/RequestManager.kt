package lin.abcdq.avcache

import okhttp3.Call
import okhttp3.Request
import java.net.Socket

internal class RequestManager {

    companion object {

        private val mRequestInfoMap = HashMap<String, InfoSource>()
        fun proxy(url: String, infoRequest: InfoRequest, socket: Socket, cache: FileCache) {
            try {
                if (mRequestInfoMap[url] == null) mRequestInfoMap[url] = infoSource(url)
                val infoSource = mRequestInfoMap[url]!!
                if (useCache(cache, infoRequest, infoSource)) {
                    cacheProxy(url, infoRequest, infoSource, socket, cache)
                } else {
                    dataProxy(url, infoRequest, infoSource, socket)
                }
            } catch (e: Exception) {
                throw e
            }
        }

        @Synchronized
        fun infoSource(url: String): InfoSource {
            return RequestManager().infoSource(url)
        }

        @Synchronized
        fun newHeaders(range: Long, length: Long, mime: String): String {
            return RequestManager().newHeader(range, length, mime)
        }

        @Synchronized
        fun newCall(url: String, range: Long, length: Long): Call {
            return RequestManager().newCall(url, range, length)
        }

        private val mCacheRunnableMap = HashMap<String, RequestRunnable.CacheRunnable>()
        private fun cacheProxy(
            url: String,
            infoRequest: InfoRequest,
            infoSource: InfoSource,
            socket: Socket,
            cache: FileCache
        ) {
            try {
                mCacheRunnableMap[url] = RequestRunnable.CacheRunnable()
                mCacheRunnableMap[url]?.set(infoSource)
                mCacheRunnableMap[url]?.run(url, infoRequest, socket, cache)
            } catch (e: Exception) {
                throw e
            }
        }

        private val mDataRunnableMap = HashMap<String, RequestRunnable.DataRunnable>()
        private fun dataProxy(
            url: String,
            infoRequest: InfoRequest,
            infoSource: InfoSource,
            socket: Socket
        ) {
            try {
                mDataRunnableMap[url] = RequestRunnable.DataRunnable()
                mCacheRunnableMap[url]?.set(infoSource)
                mDataRunnableMap[url]?.run(url, infoRequest, socket)
            } catch (e: Exception) {
                throw e
            }
        }

        private fun useCache(
            cache: FileCache,
            infoRequest: InfoRequest,
            infoSource: InfoSource
        ): Boolean {
            val sourceLength: Long = infoSource.length
            val cacheLength: Long = cache.length()
            val range = infoRequest.range
            val sourceKnown = sourceLength > 0
            val rangeKnown = range in 0 until sourceLength
            val seek2far = range > cacheLength + sourceLength * .2f
            return sourceKnown && rangeKnown && !seek2far
        }
    }

    private fun newHeader(range: Long, length: Long, mime: String): String {
        return StringBuilder().append(if (range > 0) "HTTP/1.1 206 PARTIAL CONTENT\n" else "HTTP/1.1 200 OK\n")
            .append("Accept-Ranges: bytes\n")
            .append("Content-Range: bytes $range-${length - 1}/$length")
            .append("Content-Length: ${length - range}\n")
            .append("Content-Type: $mime\n")
            .append("\n")
            .toString()
    }

    private fun newCall(url: String, range: Long, length: Long): Call {
        val okHttpRequest = Request.Builder().url("" + url)
            .addHeader("Range", "bytes=$range-$length").build()
        return RequestClient.instanceClient().newCall(request = okHttpRequest)
    }

    private fun infoSource(url: String): InfoSource {
        try {
            if (url.isEmpty()) throw Exception("Request URL Wrong")
            val request = Request.Builder()
                .addHeader("Accept-Encoding", "identity")
                .url(url)
                .build()
            val call = RequestClient.instanceClient().newCall(request = request)
            val response = call.execute()
            if (!response.isSuccessful) throw Exception("NetConnectError: ${response.code}")
            val length = response.body!!.contentLength()
            val contentType = response.body!!.contentType()!!.type
            response.close()
            return InfoSource(url, length, contentType)
        } catch (e: Exception) {
            throw e
        }
    }
}