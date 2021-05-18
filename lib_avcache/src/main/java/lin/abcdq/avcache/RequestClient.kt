package lin.abcdq.avcache

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

internal object RequestClient {

    private val mClient by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
        OkHttpClient.Builder().connectTimeout(1, TimeUnit.DAYS)
            .readTimeout(1, TimeUnit.DAYS)
            .writeTimeout(1, TimeUnit.DAYS).build()
    }

    fun instanceClient(): OkHttpClient {
        return mClient
    }
}