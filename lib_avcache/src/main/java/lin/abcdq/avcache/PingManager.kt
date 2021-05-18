package lin.abcdq.avcache

import lin.abcdq.avcache.utils.Logger
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.OutputStream
import java.net.Socket
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

internal class PingManager {

    companion object {

        private const val PING = "ping"

        private val mClient by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            OkHttpClient.Builder().connectTimeout(300L, TimeUnit.MILLISECONDS)
                .readTimeout(300L, TimeUnit.MILLISECONDS)
                .writeTimeout(300L, TimeUnit.MILLISECONDS)
                .build()
        }

        fun instanceClient(): OkHttpClient {
            return mClient
        }

        private var mSingleThread = Executors.newSingleThreadExecutor()

        fun isAlive(host: String, port: Int): Boolean {
            val sub = mSingleThread.submit(Callable {
                return@Callable PingManager().ping(host, port)
            })
            return sub.get()
        }

        fun isPingRequest(url: String, socket: Socket): Boolean {
            val ping = url == PING
            if (ping) {
                val out: OutputStream = socket.getOutputStream()
                out.write("HTTP/1.1 200 OK\n\n".toByteArray())
                out.write(PING.toByteArray())
                out.close()
            }
            return ping
        }
    }

    private fun ping(host: String, port: Int): Boolean {
        var ping = false
        try {
            if (host.isEmpty()) throw Exception("Request URL Wrong")
            val url = String.format(Locale.US, "http://%s:%d/%s", host, port, PING)
            val request = Request.Builder().url("" + url).build()
            val call = instanceClient().newCall(request = request)
            val response = call.execute()
            call.cancel()
            response.close()
            ping = response.isSuccessful
        } catch (e: Exception) {
            Logger.e("PingManger", "serverSocket error: may over size(BACK_LOCK)")
        } finally {
            return ping
        }
    }
}