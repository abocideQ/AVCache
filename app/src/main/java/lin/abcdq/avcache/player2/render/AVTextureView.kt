package lin.abcdq.avcache.player2.render

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.TextureView
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout

class AVTextureView : TextureView {

    private val mTAG = "AVSurface"

    private fun log(d: String) {
        Log.d(mTAG, d)
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, def: Int) : super(context, attrs, def)

    private var mSourceWidthRecord: Float = 0f
    private var mSourceHeightRecord: Float = 0f
    private var mSourceWidth: Float = 0f
    private var mSourceHeight: Float = 0f

    fun onSizeChanged(w: Int, h: Int) {
        mSourceWidth = w.toFloat()
        mSourceHeight = h.toFloat()
        mSourceWidthRecord = mSourceWidth
        mSourceHeightRecord = mSourceHeight
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var mWidthMeasureSpec = widthMeasureSpec
        var mHeightMeasureSpec = heightMeasureSpec
        if (rotation == 90f || rotation == 270f) {
            mWidthMeasureSpec = heightMeasureSpec
            mHeightMeasureSpec = widthMeasureSpec
        }
        val mViewWidthMode = MeasureSpec.getMode(widthMeasureSpec)
        val mViewHeightMode = MeasureSpec.getMode(heightMeasureSpec)
        var mViewWidth = MeasureSpec.getSize(widthMeasureSpec).toFloat()
        var mViewHeight = MeasureSpec.getSize(heightMeasureSpec).toFloat()
        if (mSourceWidthRecord != 0f && mSourceHeightRecord != 0f) {
            mSourceWidth = mSourceWidthRecord
            mSourceHeight = mSourceHeightRecord
        }
        if (mSourceWidth == 0f || mSourceHeight == 0f) {
            setMeasuredDimension(1, 1)
            return
        }
        when (mViewWidthMode) {
            MeasureSpec.UNSPECIFIED -> log("WIDTH UNSPECIFIED")//unknown : normal by customView
            MeasureSpec.AT_MOST -> log("WIDTH AT_MOST")//wrap
            MeasureSpec.EXACTLY -> log("WIDTH EXACTLY")//dp/match
        }
        when (mViewHeightMode) {
            MeasureSpec.UNSPECIFIED -> log("HEIGHT UNSPECIFIED")//unknown : normal by customView
            MeasureSpec.AT_MOST -> log("HEIGHT AT_MOST")//wrap
            MeasureSpec.EXACTLY -> log("HEIGHT EXACTLY")//dp/match
        }
        if (mSourceWidth > mViewWidth && mSourceHeight > mViewHeight) {
            val widthDiff = mSourceWidth - mViewWidth
            val heightDiff = mSourceHeight - mViewHeight
            if (widthDiff > heightDiff) {
                val ratio = mViewWidth / mSourceWidth
                mSourceWidth = mViewWidth
                mSourceHeight *= ratio
            } else if (heightDiff > widthDiff) {
                val ratio = mViewHeight / mSourceHeight
                mSourceWidth *= ratio
                mSourceHeight = mViewHeight
            }
        } else if (mSourceWidth > mViewWidth) {
            val ratio = mViewWidth / mSourceWidth
            mSourceWidth = mViewWidth
            mSourceHeight *= ratio
        } else if (mSourceHeight > mViewHeight) {
            val ratio = mViewHeight / mSourceHeight
            mSourceWidth *= ratio
            mSourceHeight = mViewHeight
        } else if (mSourceWidth < mViewWidth && mSourceHeight < mViewHeight) {
            val widthDiff = mViewWidth - mSourceWidth
            val heightDiff = mViewHeight - mSourceHeight
            if (widthDiff > heightDiff) {
                val ratio = mViewHeight / mSourceHeight
                mSourceWidth *= ratio
                mSourceHeight = mViewHeight
            } else if (heightDiff > widthDiff) {
                val ratio = mViewWidth / mSourceWidth
                mSourceWidth = mViewWidth
                mSourceHeight *= ratio
            }
        }
        when (AVRender.DISPLAY_MODE) {
            AVRender.ORIGINAL -> {
                mViewWidth = mSourceWidth
                mViewHeight = mSourceHeight
            }
            AVRender.FILL_CROP -> {
                val widthDiff = mViewWidth - mSourceWidth
                val heightDiff = mViewHeight - mSourceHeight
                if (widthDiff > 0) {
                    val ratio = mViewWidth / mSourceWidth
                    mSourceWidth = mViewWidth
                    mSourceHeight *= ratio
                } else if (heightDiff > 0) {
                    val ratio = mViewHeight / mSourceHeight
                    mSourceWidth *= ratio
                    mSourceHeight = mViewHeight
                }
                mViewWidth = mSourceWidth
                mViewHeight = mSourceHeight
            }
            AVRender.FILL_PARENT -> {
                mViewWidth = MeasureSpec.getSize(mWidthMeasureSpec).toFloat()
                mViewHeight = MeasureSpec.getSize(mHeightMeasureSpec).toFloat()
            }
        }
        setMeasuredDimension(mViewWidth.toInt(), mViewHeight.toInt())
    }

//    private fun onGravity(view: View) {
//        val params = view.layoutParams ?: return
//        when (params) {
//            is FrameLayout.LayoutParams -> {
//                params.gravity = Gravity.CENTER
//                view.layoutParams = params
//            }
//            is RelativeLayout.LayoutParams -> {
//                params.addRule(RelativeLayout.CENTER_IN_PARENT)
//                view.layoutParams = params
//            }
//            is LinearLayout.LayoutParams -> {
//                val parent = view.parent ?: return
//                if (parent !is LinearLayout) return
//                parent.gravity = Gravity.CENTER
//            }
//        }
//    }
}