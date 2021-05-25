package lin.abcdq.avcache.player2

import lin.abcdq.avcache.player2.render.AVRender

abstract class AVBasePlayer {

    abstract fun render(render: AVRender)

    abstract fun render(): AVRender?

    abstract fun resource(URL: String)

    abstract fun play()

    abstract fun pause()

    abstract fun stop()

    abstract fun release()

    abstract fun seekTo(timeMs: Long)

    abstract fun currentTimeMs(): Long

    abstract fun isPlaying(): Boolean

    abstract fun width(): Int

    abstract fun height(): Int

    abstract fun duration(): Long

    abstract fun volume(): Float

    abstract fun volume(volume: Float)

    abstract fun addEventListener(listener: EventListener)

    abstract fun removeEventListener(listener: EventListener)

    interface EventListener {
        fun onIdle() {}
        fun onBuffering(percent: Int) {}
        fun onReady() {}
        fun onEnded() {}
        fun onUnknown() {}
        fun onError(e: Any) {}
        fun onCurrent(current: Long, duration: Long) {}
        fun onSizeChanged(w: Int, h: Int) {}
    }
}