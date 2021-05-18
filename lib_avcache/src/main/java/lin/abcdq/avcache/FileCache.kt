package lin.abcdq.avcache

import lin.abcdq.avcache.utils.CacheProxyUtils
import okio.IOException
import java.io.File
import java.io.RandomAccessFile

internal class FileCache(private val root: String, private val url: String) {

    companion object {
        const val BUFFER_SIZE = 1024 * 8
        private const val FOLDER = "CACHE"
        private const val END_CACHE = ".CACHE"
        private const val END_FULL = ""

        fun cache(root: String, url: String): FileCache {
            return FileCache(root, url)
        }
    }

    val file: File? by lazy { return@lazy file() }
    private var cache: RandomAccessFile = cache()

    @Synchronized
    fun write(byteArray: ByteArray, offset: Long, len: Int) {
        try {
            if (completed()) return
            cache.seek(offset)
            cache.write(byteArray, 0, len)
        } catch (e: Exception) {
            throw IOException(e)
        }
    }

    @Synchronized
    fun read(byteArray: ByteArray, offset: Long, len: Int): Int {
        try {
            cache.seek(offset)
            return cache.read(byteArray, 0, len)
        } catch (e: Exception) {
            throw IOException(e)
        }
    }

    @Synchronized
    fun close() {
        try {
            cache.close()
            FileCacheLruManager.run(cacheFile())
        } catch (e: Exception) {
            throw IOException(e)
        }
    }

    @Synchronized
    fun complete() {
        try {
            if (completed()) return
            close()
            var old = File("$root/$FOLDER")
            val new = File(old, "$FOLDER-${CacheProxyUtils.computeMD5(url)}$END_FULL")
            old = File(old, "$FOLDER-${CacheProxyUtils.computeMD5(url)}$END_CACHE")
            old.renameTo(new)
            old = new
            cache = RandomAccessFile(old, "r")
            FileCacheLruManager.run(old)
        } catch (e: Exception) {
            throw IOException(e)
        }
    }

    @Synchronized
    fun length(): Long {
        try {
            return cache.length()
        } catch (e: Exception) {
            throw IOException(e)
        }
    }

    @Synchronized
    fun completed(): Boolean {
        var file = File("$root/$FOLDER")
        file = File(file, "$FOLDER-${CacheProxyUtils.computeMD5(url)}$END_FULL")
        return file.exists()
    }

    @Synchronized
    private fun cache(): RandomAccessFile {
        if (completed()) {
            return RandomAccessFile(file, "r")
        }
        var file = File("$root/$FOLDER")
        if (!file.exists()) file.mkdirs()
        file = File(file, "$FOLDER-${CacheProxyUtils.computeMD5(url)}$END_CACHE")
        return RandomAccessFile(file, "rw")
    }

    @Synchronized
    private fun cacheFile(): File {
        var file = File("$root/$FOLDER")
        if (!file.exists()) file.mkdirs()
        return File(file, "$FOLDER-${CacheProxyUtils.computeMD5(url)}$END_CACHE")
    }

    @Synchronized
    private fun file(): File {
        var file = File("$root/$FOLDER")
        if (!file.exists()) file.mkdirs()
        file = File(file, "$FOLDER-${CacheProxyUtils.computeMD5(url)}$END_FULL")
        return file
    }
}