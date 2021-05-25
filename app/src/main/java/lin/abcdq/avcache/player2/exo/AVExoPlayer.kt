package lin.abcdq.avcache.player2.exo

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.SurfaceTexture
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.Surface
import android.view.SurfaceControl
import android.view.SurfaceControl.Transaction
import android.view.SurfaceHolder
import android.view.TextureView
import lin.abcdq.avcache.player2.AVBasePlayer
import lin.abcdq.avcache.player2.render.AVRender
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.video.VideoListener

internal class AVExoPlayer(context: Context) : AVBasePlayer() {

    private var mExoPlayer: SimpleExoPlayer = SimpleExoPlayer.Builder(context).build()
    private var mAVRender: AVRender? = AVRender(context)
    private var mURL = ""

    private var mListeners: ArrayList<EventListener>? = null

    init {
        initEventListener()
        initRecycleRender()
    }

    override fun render(render: AVRender) {
        mAVRender = render
        initRecycleRender()
    }

    override fun render(): AVRender? {
        return mAVRender
    }

    override fun addEventListener(listener: EventListener) {
        if (mListeners == null) mListeners = arrayListOf()
        mListeners?.add(listener)
    }

    override fun removeEventListener(listener: EventListener) {
        if (mListeners == null) return
        mListeners?.remove(listener)
    }

    override fun resource(URL: String) {
        mURL = URL
        mExoPlayer.setMediaItem(MediaItem.fromUri(URL))
        mExoPlayer.playWhenReady = true
        mExoPlayer.prepare()
        mHandler.sendEmptyMessageDelayed(0, 500)
    }

    override fun play() {
        mHandler.sendEmptyMessageDelayed(0, 500)
        mExoPlayer.play()
    }

    override fun pause() {
        mHandler.removeCallbacksAndMessages(null)
        mExoPlayer.pause()
    }

    override fun stop() {
        mAVRender?.surfaceControl(null)
        mAVRender?.surfaceTexture(null)
        mHandler.removeCallbacksAndMessages(null)
        mExoPlayer.stop()
    }

    override fun release() {
        mAVRender?.surfaceControl(null)
        mAVRender?.surfaceTexture(null)
        mHandler.removeCallbacksAndMessages(null)
        mExoPlayer.release()
    }

    override fun seekTo(timeMs: Long) {
        mHandler.removeCallbacksAndMessages(null)
        mExoPlayer.seekTo(timeMs)
        mHandler.sendEmptyMessageDelayed(0, 500)
    }

    override fun currentTimeMs(): Long {
        return mExoPlayer.currentPosition
    }

    override fun isPlaying(): Boolean {
        return mExoPlayer.isPlaying
    }

    override fun width(): Int {
        return mExoPlayer.videoFormat?.width ?: 0
    }

    override fun height(): Int {
        return mExoPlayer.videoFormat?.height ?: 0
    }

    override fun duration(): Long {
        return mExoPlayer.duration
    }

    override fun volume(): Float {
        return mExoPlayer.volume
    }

    override fun volume(volume: Float) {
        mExoPlayer.volume = volume
    }

    private fun initRecycleRender() {
        if (AVRender.VIEW == AVRender.SURFACE) {
            mAVRender?.surfaceView()?.holder?.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceCreated(holder: SurfaceHolder) {
                    if (mAVRender?.surfaceControl() == null) {
                        val control = SurfaceControl.Builder().setName("").build()
                        mAVRender?.surfaceControl(control)
                        mExoPlayer.setVideoSurface(Surface(control))
                    }
                    val oldControl = mAVRender?.surfaceControl() ?: return
                    val newControl: SurfaceControl =
                        mAVRender?.surfaceView()?.surfaceControl ?: return
                    Transaction().reparent(oldControl, newControl)
                        .setVisibility(oldControl, true)
                        .apply()
                }

                override fun surfaceChanged(sh: SurfaceHolder, f: Int, w: Int, h: Int) {
                    Transaction().setBufferSize(mAVRender?.surfaceControl() ?: return, w, h).apply()
                }

                override fun surfaceDestroyed(holder: SurfaceHolder) {
                }
            })
        } else if (AVRender.VIEW == AVRender.TEXTURE) {
            mAVRender?.textureView()?.surfaceTextureListener =
                object : TextureView.SurfaceTextureListener {
                    override fun onSurfaceTextureAvailable(st: SurfaceTexture, w: Int, h: Int) {
                        if (mAVRender?.surfaceTexture() == null) {
                            mAVRender?.surfaceTexture(st)
                            mExoPlayer.setVideoSurface(Surface(mAVRender?.surfaceTexture()))
                        } else {
                            mAVRender?.textureView()
                                ?.setSurfaceTexture(mAVRender?.surfaceTexture() ?: return)
                        }
                    }

                    override fun onSurfaceTextureSizeChanged(s: SurfaceTexture, w: Int, h: Int) {
                    }

                    override fun onSurfaceTextureDestroyed(s: SurfaceTexture): Boolean {
                        return false
                    }

                    override fun onSurfaceTextureUpdated(s: SurfaceTexture) {
                    }
                }
        }
    }

    private fun initEventListener() {
        mExoPlayer.addListener(object : Player.EventListener {
            override fun onPlaybackStateChanged(state: Int) {
                super.onPlaybackStateChanged(state)
                when (state) {
                    Player.STATE_IDLE -> {
                        mListeners?.forEach { it -> it.onIdle() }
                    }
                    Player.STATE_BUFFERING -> {
                        mListeners?.forEach { it -> it.onBuffering(-1) }
                    }
                    Player.STATE_READY -> {
                        mListeners?.forEach { it -> it.onReady() }
                    }
                    Player.STATE_ENDED -> {
                        mListeners?.forEach { it -> it.onEnded() }
                    }
                    else -> {
                        mListeners?.forEach { it -> it.onUnknown() }
                    }
                }
            }

            override fun onPlayerError(error: ExoPlaybackException) {
                super.onPlayerError(error)
                mListeners?.forEach { it -> it.onError(error) }
            }
        })
        mExoPlayer.addVideoListener(object : VideoListener {
            override fun onVideoSizeChanged(width: Int, height: Int, degress: Int, ratio: Float) {
                super.onVideoSizeChanged(width, height, degress, ratio)
                mAVRender?.onSizeChanged(width, height)
                mListeners?.forEach { it -> it.onSizeChanged(width, height) }
            }
        })
    }

    private val mHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            mListeners?.forEach { it -> it.onCurrent(currentTimeMs(), mExoPlayer.duration) }
            sendEmptyMessageDelayed(0, 500)
        }
    }

    /**
     * ExoPlayer.getCurrentPosition can just running on mainThread
     */
//    private var mCurrentRunnable: CurrentRunnable? = null
//
//    private fun exCurrentRunnable(interrupt: Boolean) {
//        if (interrupt) mCurrentRunnable?.interrupt()
//        if (interrupt) return
//        if (mCurrentRunnable == null) {
//            mCurrentRunnable = CurrentRunnable(mListener ?: return, this)
//            mCurrentThread.execute(mCurrentRunnable)
//        } else if (mCurrentRunnable?.interrupted() ?: return) {
//            mCurrentRunnable = CurrentRunnable(mListener ?: return, this)
//            mCurrentThread.execute(mCurrentRunnable)
//        }
//    }

//    class CurrentRunnable(
//        private val listener: EventListener?,
//        private val cjBasePlayer: CjBasePlayer?
//    ) : Runnable {
//
//        override fun run() {
//            try {
//                while (!interrupted()) {
//                    listener?.onCurrent(cjBasePlayer.currentTimeMs() ?: return)
//                    Thread.sleep(500)
//                    continue
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//                interrupt()
//            }
//        }
//
//        private var interrupt = false
//
//        fun interrupted(): Boolean {
//            return interrupt || Thread.currentThread().isInterrupted
//        }
//
//        fun interrupt() {
//            try {
//                interrupt = true
//                Thread.currentThread().interrupt()
//            } catch (e: Exception) {
//            }
//        }
//    }

}