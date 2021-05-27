package lin.abcdq.avcache.player2.utils

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver.OnPreDrawListener
import kotlin.math.abs

class AVGesture : View.OnTouchListener {

    private var mListener: EventListener? = null

    fun set(view: View, listener: EventListener) {
        view.viewTreeObserver.addOnPreDrawListener(object : OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                mListener = listener
                view.viewTreeObserver.removeOnPreDrawListener(this)
                mMeasureWidth = view.measuredWidth
                view.setOnTouchListener(this@AVGesture)
                return false
            }
        })
    }

    interface EventListener {
        fun onSingleClick() {}
        fun onDoubleClick() {}
        fun onHorizontalScroll(x: Float, end: Boolean) {}
        fun onVerticalLeftScroll(y: Float, end: Boolean) {}
        fun onVerticalRightScroll(y: Float, end: Boolean) {}
    }

    companion object{
        private const val LIMIT_DOUBLE_CLICK: Long = 200
        private const val CLICK_SINGLE = 0
        private const val CLICK_DOUBLE = 1
    }

    private var mLastClick: Long = 0
    private val mClickHandler = Handler(Looper.getMainLooper()) {
        when (it.what) {
            CLICK_SINGLE -> {
                mListener?.onSingleClick()
            }
            CLICK_DOUBLE -> {
                mListener?.onDoubleClick()
            }
        }
        false
    }

    private var mMeasureWidth = 0
    private var mDownX = 0f
    private var mDownY = 0f
    private var mScrollType = 0

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, e: MotionEvent): Boolean {
        when (e.action) {
            MotionEvent.ACTION_DOWN -> {
                mDownX = e.rawX
                mDownY = e.rawY
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val x = mDownX - e.rawX
                val y = mDownY - e.rawY
                val xAbs = abs(x)
                val yAbs = abs(y)
                when {
                    xAbs > yAbs -> {
                        if (mScrollType == 0) mScrollType = 1
                        if (mScrollType != 1) return true
                        mListener?.onHorizontalScroll(x, false)
                    }
                    xAbs < yAbs && mDownX > mMeasureWidth / 2 -> {
                        if (mScrollType == 0) mScrollType = 2
                        if (mScrollType != 2) return true
                        mListener?.onVerticalRightScroll(y, false)
                    }
                    xAbs < yAbs && mDownX < mMeasureWidth / 2 -> {
                        if (mScrollType == 0) mScrollType = 3
                        if (mScrollType != 3) return true
                        mListener?.onVerticalLeftScroll(y, false)
                    }
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (mScrollType != 0) {
                    when (mScrollType) {
                        1 -> {
                            mListener?.onHorizontalScroll(0f, true)
                        }
                        2 -> {
                            mListener?.onVerticalRightScroll(0f, true)
                        }
                        3 -> {
                            mListener?.onVerticalLeftScroll(0f, true)
                        }
                    }
                    mScrollType = 0
                    return true
                }
                val mCurrentClick = System.currentTimeMillis()
                val diff = mCurrentClick - mLastClick
                if (diff <= LIMIT_DOUBLE_CLICK) {
                    mClickHandler.removeCallbacksAndMessages(null)
                    mClickHandler.sendEmptyMessage(CLICK_DOUBLE)
                } else if (diff > LIMIT_DOUBLE_CLICK) {
                    mClickHandler.sendEmptyMessageDelayed(CLICK_SINGLE, LIMIT_DOUBLE_CLICK)
                }
                mLastClick = mCurrentClick
                return true
            }
            else -> {
                mClickHandler.removeCallbacksAndMessages(null)
            }
        }
        return false
    }

}