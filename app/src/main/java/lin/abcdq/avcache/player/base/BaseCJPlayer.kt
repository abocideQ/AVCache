package lin.abcdq.avcache.player.base

abstract class BaseCJPlayer {

    abstract fun surface(vararg any: Any)

    abstract fun surface(): Any?

    abstract fun resource(URL: String)

    abstract fun play()

    abstract fun pause()

    abstract fun stop()

    abstract fun release()

    abstract fun seekTo(timeMs: Long)

    abstract fun currentTimeMs(): Long

    abstract fun isPlaying(): Boolean

    abstract fun setInfoListener(listener: Any)
}