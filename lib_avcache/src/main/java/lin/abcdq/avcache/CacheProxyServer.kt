package lin.abcdq.avcache

import lin.abcdq.avcache.utils.CacheProxyUtils
import lin.abcdq.avcache.utils.Logger
import java.io.IOException
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import java.util.concurrent.Executors

internal class CacheProxyServer(cacheRoot: String) {

    companion object {
        const val HOST = "127.0.0.1"
        var PORT: Int = 0
        private const val BACK_LOCK = 8
    }

    private val mCacheRoot = cacheRoot
    private var mServerSocket: ServerSocket
    private val mServerSocketThread = Executors.newSingleThreadExecutor()
    private val mSocketThread = Executors.newFixedThreadPool(BACK_LOCK)

    init {
        val internetAddress = InetAddress.getByName(HOST)
        mServerSocket = ServerSocket(PORT, BACK_LOCK, internetAddress)
        PORT = mServerSocket.localPort
        mServerSocketThread.execute { waitRequest() }
    }

    private fun waitRequest() {
        try {
            while (!Thread.currentThread().isInterrupted) {
                val socket: Socket = mServerSocket.accept()!!
                mSocketThread.execute(SocketProcessRunnable(mCacheRoot, socket))
            }
        } catch (e: IOException) {
            e.printStackTrace()
            mServerSocketThread.shutdown()
        }
    }

    private class SocketProcessRunnable(private val cacheRoot: String, private val socket: Socket) :
        Runnable {
        override fun run() {
            processSocket(cacheRoot, socket)
        }

        private fun processSocket(cacheRoot: String, socket: Socket) {
            try {
                val inStream = socket.getInputStream()
                val format = InfoRequest.read(inStream)
                val url = CacheProxyUtils.decode(format.uri)
                if (PingManager.isPingRequest(url, socket)) {
                    socketRelease(socket)
                    return
                }
                if (CachePreload.preloading(url)) {
                    CachePreload.interrupt(url)
                }
                RequestManager.proxy(url, format, socket, FileCache.cache(cacheRoot, url))
            } catch (e: SocketException) {
                Logger.e("processSocket", "socket closing by newOne/connect reset/pipe break")
            } catch (e: Exception) {
                Logger.e("", "$e")
            } finally {
                socketRelease(socket)
            }
        }

        @Synchronized
        private fun socketRelease(socket: Socket) {
            try {
                if (!socket.isInputShutdown) socket.shutdownInput()
            } catch (e: Exception) {
                Logger.e("socketRelease", "SocketInput Shutdown")
            }
            try {
                if (!socket.isOutputShutdown) socket.shutdownOutput()
            } catch (e: Exception) {
                Logger.e("socketRelease", "SocketOutput Shutdown")
            }
            try {
                if (!socket.isClosed) socket.close()
            } catch (e: Exception) {
                Logger.e("socketRelease", "Socket Close")
            }
        }
    }
}