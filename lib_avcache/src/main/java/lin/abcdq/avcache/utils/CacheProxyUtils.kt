package lin.abcdq.avcache.utils

import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.net.URLEncoder
import java.security.MessageDigest

internal object CacheProxyUtils {

    fun encode(url: String?): String {
        return try {
            URLEncoder.encode(url, "utf-8")
        } catch (e: UnsupportedEncodingException) {
            throw Exception("encoding url error", e)
        }
    }

    fun decode(url: String?): String {
        return try {
            URLDecoder.decode(url, "utf-8")
        } catch (e: UnsupportedEncodingException) {
            throw Exception("decoding url error", e)
        }
    }

    fun computeMD5(string: String): String {
        try {
            val messageDigest = MessageDigest.getInstance("MD5")
            val digestBytes = messageDigest.digest(string.toByteArray())
            return bytesToHexString(digestBytes)
        } catch (e: Exception) {
            throw Exception("md5 error", e)
        }
    }

    private fun bytesToHexString(bytes: ByteArray): String {
        val sb = StringBuffer()
        for (b in bytes) {
            sb.append(String.format("%02x", b))
        }
        return sb.toString()
    }
}