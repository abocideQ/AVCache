package lin.abcdq.avcache.player2.render

import android.content.Context
import android.graphics.SurfaceTexture
import android.view.*
import android.widget.FrameLayout
import androidx.core.content.ContextCompat

class AVRender(context: Context) : FrameLayout(context) {

    companion object {
        const val ORIGINAL = 1
        const val FILL_CROP = 2
        const val FILL_PARENT = 3
        var DISPLAY_MODE = ORIGINAL

        const val SURFACE = 1
        const val TEXTURE = 2
        var VIEW = TEXTURE
    }

    //SurfaceView
    private var mSurfaceView: AVSurfaceView = AVSurfaceView(context)
    private var mSurfaceControl: SurfaceControl? = null

    fun surfaceView(): AVSurfaceView {
        return mSurfaceView
    }

    fun surfaceControl(): SurfaceControl? {
        return mSurfaceControl
    }

    fun surfaceControl(surfaceControl: SurfaceControl?) {
        mSurfaceControl = surfaceControl
    }

    //TextureView
    private var mTextureView: AVTextureView = AVTextureView(context)
    private var mSurfaceTexture: SurfaceTexture? = null//操作SurfaceTexture防重构闪烁
    fun textureView(): AVTextureView {
        return mTextureView
    }

    fun surfaceTexture(): SurfaceTexture? {
        return mSurfaceTexture
    }

    fun surfaceTexture(surfaceTexture: SurfaceTexture?) {
        mSurfaceTexture = surfaceTexture
    }

    init {
        val measure = ViewGroup.LayoutParams.MATCH_PARENT
        val gravity = Gravity.CENTER
        val params = LayoutParams(measure, measure, gravity)
        addView(mSurfaceView, params)
        addView(mTextureView, params)
        setBackgroundColor(ContextCompat.getColor(context, android.R.color.black))
    }

    fun onSizeChanged(w: Int, h: Int) {
        if (VIEW == SURFACE) mSurfaceView.onSizeChanged(w, h)
        else if (VIEW == TEXTURE) mTextureView.onSizeChanged(w, h)
    }
}