package lin.abcdq.avcache

import lin.abcdq.avcache.utils.Logger
import okhttp3.Call
import okhttp3.Response
import java.io.*
import java.net.Socket

internal class RequestRunnable {

    class CacheRunnable {

        private val mCacheWriteLock = Any()
        private lateinit var mCache: FileCache

        private var url = ""
        private var length = 0L
        private var mime = ""

        fun set(infoSource: InfoSource) {
            if (length != 0L || mime.isNotEmpty()) return
            length = infoSource.length
            mime = infoSource.mime
        }

        fun run(url: String, infoRequest: InfoRequest, socket: Socket, fileCache: FileCache) {
            try {
                this.url = url
                val outStream = socket.getOutputStream()
                val range = infoRequest.range
                if (length == 0L && mime.isEmpty()) {
                    val infoSource = RequestManager.infoSource(url)
                    length = infoSource.length
                    mime = infoSource.mime
                }
                val headers: String = RequestManager.newHeaders(range, length, mime)
                outStream.write(headers.toByteArray(charset("UTF-8")))
                mCache = fileCache
                read(outStream, range)
            } catch (e: Exception) {
                throw e
            } finally {
                interrupt()
            }
        }

        private fun read(outStream: OutputStream, range: Long) {
            var offset = range
            val bufferRead = ByteArray(FileCache.BUFFER_SIZE)
            while (true) {
                if (!mCache.completed() && mCache.length() < offset + bufferRead.size && !isInterrupt()) {
                    synchronized(this) {
                        writeAsync()
                    }
                    continue
                }
                val len = mCache.read(bufferRead, offset, bufferRead.size)
                if (len == -1) break
                outStream.write(bufferRead, 0, len)
                offset += len
            }
        }

        private fun writeAsync() {
            val processing = mWriteThread != null && mWriteThread?.state != Thread.State.TERMINATED
            if (!processing && !mCache.completed()) {
                mWriteThread = Thread {
                    var call: Call? = null
                    var response: Response? = null
                    var inStream: InputStream? = null
                    try {
                        var offset = mCache.length()
                        call = RequestManager.newCall(url, offset, length)
                        response = call.execute()
                        if (!response.isSuccessful) throw Exception("Connect Error : ${response.code}")
                        inStream = response.body!!.byteStream()
                        val bufferWrite = ByteArray(FileCache.BUFFER_SIZE)
                        while (true) {
                            val len = inStream.read(bufferWrite)
                            if (len == -1) break
                            synchronized(mCacheWriteLock) {
                                if (isInterrupt()) return@Thread
                                mCache.write(bufferWrite, offset, len)
                            }
                            offset += len
                        }
                        synchronized(mCacheWriteLock) {
                            if (isInterrupt()) return@Thread
                            if (mCache.length() >= length) mCache.complete()
                        }
                    } catch (e: Exception) {
                        Logger.e("Cache Write", "$e")
                    } finally {
                        interrupt(call, response, inStream)
                    }
                }
                mWriteThread?.start()
            }
        }

        @Volatile
        private var mWriteThread: Thread? = null

        @Volatile
        private var interrupt = false

        private fun isInterrupt(): Boolean {
            return interrupt || Thread.currentThread().isInterrupted
        }

        private fun interrupt() {
            synchronized(mCacheWriteLock) {
                interrupt = true
                mWriteThread?.interrupt()
                mCache.close()
            }
        }

        private fun interrupt(call: Call?, response: Response?, inputStream: InputStream?) {
            call?.cancel()
            response?.close()
            inputStream?.close()
        }
    }


    class DataRunnable {

        private var url = ""
        private var length = 0L
        private var mime = ""

        fun set(infoSource: InfoSource) {
            if (length != 0L || mime.isNotEmpty()) return
            length = infoSource.length
            mime = infoSource.mime
        }

        fun run(url: String, infoRequest: InfoRequest, socket: Socket) {
            try {
                this.url = url
                val outStream = socket.getOutputStream()
                val range = infoRequest.range
                if (length == 0L && mime.isEmpty()) {
                    val infSource = RequestManager.infoSource(url)
                    length = infSource.length
                    mime = infSource.mime
                }
                val headers: String = RequestManager.newHeaders(range, length, mime)
                outStream.write(headers.toByteArray(charset("UTF-8")))
                proxy(outStream, range)
            } catch (e: Exception) {
                throw e
            }
        }

        private fun proxy(outStream: OutputStream, offset: Long) {
            var call: Call? = null
            var response: Response? = null
            var inStream: InputStream? = null
            try {
                call = RequestManager.newCall(url, offset, length)
                response = call.execute()
                if (!response.isSuccessful) throw IOException("Net Connect Error: ${response.code}")
                inStream = response.body!!.byteStream()
                val bytes = ByteArray(FileCache.BUFFER_SIZE)
                while (true) {
                    val len = inStream.read(bytes)
                    if (len == -1) response.close()
                    if (len == -1) break
                    outStream.write(bytes, 0, len)
                }
            } catch (e: Exception) {
                throw e
            } finally {
                interrupt(call, response, inStream)
            }
        }

        private fun interrupt(call: Call?, response: Response?, inputStream: InputStream?) {
            call?.cancel()
            response?.close()
            inputStream?.close()
        }
    }
}