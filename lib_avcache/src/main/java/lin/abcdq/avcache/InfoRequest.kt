package lin.abcdq.avcache

import android.text.TextUtils
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.regex.Pattern
import kotlin.math.max

internal class InfoRequest(request: String) {

    private val mRangePattern =
        Pattern.compile("[R,r]ange:[ ]?bytes=(\\d*)-")
    private val mURIPattern =
        Pattern.compile("GET /(.*) HTTP")

    var uri: String? = null
    var range: Long = 0
    var string: String? = request

    init {
        try {
            val offset = findRange(request)
            range = max(0, offset)
            uri = findUri(request)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        fun read(inputStream: InputStream?): InfoRequest {
            val reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
            val stringRequest = StringBuilder()
            var line: String?
            while (!TextUtils.isEmpty(reader.readLine().also { line = it })) {
                stringRequest.append(line).append('\n')
            }
            return InfoRequest(stringRequest.toString())
        }
    }

    private fun findRange(request: String): Long {
        val matcher = mRangePattern.matcher(request)
        return if (matcher.find()) matcher.group(1)?.toLong() ?: -1
        else -1
    }

    private fun findUri(request: String): String {
        val matcher = mURIPattern.matcher(request)
        return if (matcher.find()) matcher.group(1) ?: ""
        else ""
    }
}