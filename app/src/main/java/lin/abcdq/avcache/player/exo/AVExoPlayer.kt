package lin.abcdq.avcache.player.exo

import android.content.Context
import android.view.SurfaceView
import lin.abcdq.avcache.player.base.BaseCJPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer

class AVExoPlayer(context: Context) : BaseCJPlayer() {

    private val mExoPlayer: SimpleExoPlayer by lazy { SimpleExoPlayer.Builder(context).build() }
    private var mSurfaceView: SurfaceView? = null
    private var mURL = ""

    override fun surface(vararg any: Any) {
        mSurfaceView = any[0] as SurfaceView
        mExoPlayer.setVideoSurfaceView(mSurfaceView)
    }

    override fun surface(): Any? {
        return mSurfaceView
    }

    override fun resource(URL: String) {
        mURL = URL
        mExoPlayer.setMediaItem(MediaItem.fromUri(URL))
        mExoPlayer.playWhenReady = true
        mExoPlayer.prepare()
    }

    override fun play() {
        mExoPlayer.play()
    }

    override fun pause() {
        mExoPlayer.pause()
    }

    override fun stop() {
        mExoPlayer.stop()
    }

    override fun release() {
        mExoPlayer.release()
    }

    override fun seekTo(timeMs: Long) {
        mExoPlayer.seekTo(timeMs)
    }

    override fun currentTimeMs(): Long {
        return mExoPlayer.currentPosition
    }

    override fun isPlaying(): Boolean {
        return mExoPlayer.isPlaying
    }

    /**@param:Player.EventListener*/
    override fun setInfoListener(playerEventListener: Any) {
        mExoPlayer.addListener(playerEventListener as Player.EventListener)
    }
}