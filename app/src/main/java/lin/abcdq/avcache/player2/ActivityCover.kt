package lin.abcdq.avcache.player2

import android.app.Activity
import android.content.Intent
import android.view.*
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

/**
 * 视频无缝衔接用（假装activity）
 */
abstract class ActivityCover(context: FragmentActivity) : FrameLayout(context) {

    fun setIntent(intent: Intent) {
        this.intent = intent
    }

    fun intent(): Intent? {
        return intent
    }

    fun setOnResult(onResult: Runnable?) {
        this.onResult = onResult
    }

    fun onResult(): Runnable? {
        return onResult
    }

    fun activity(): Activity? {
        return mActivity
    }

    fun startAcSham() {
        init()
    }

    private var intent: Intent? = null
    private var onResult: Runnable? = null
    private var mActivity: Activity? = context
    private var mDecorView: View? = context.window.decorView
    private var mOwnerLifecycle: Lifecycle = context.lifecycle
    private var mOwnerLifecycleObserver: LifecycleEventObserver? = null

    private fun init() {
        mOwnerLifecycleObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    onResumed()
                }
                Lifecycle.Event.ON_PAUSE -> {
                    onPaused()
                }
                Lifecycle.Event.ON_STOP -> {
                    onStopped()
                }
                Lifecycle.Event.ON_DESTROY -> {
                    onDestroyed()
                    release(true)
                }
                else -> {
                }
            }
        }
        mOwnerLifecycle.addObserver(mOwnerLifecycleObserver ?: return)
        if (mDecorView == null) return
        focusRequest(mDecorView as ViewGroup, this)
        val measure = ViewGroup.LayoutParams.MATCH_PARENT
        (mDecorView as ViewGroup).addView(this, measure, measure)
        val view = LayoutInflater.from(context).inflate(onCreateView(), null)
        this.addView(view, measure, measure)
        onViewCreated(view)
    }

    protected abstract fun onCreateView(): Int

    protected abstract fun onViewCreated(view: View)

    protected abstract fun onResumed()

    protected abstract fun onPaused()

    protected abstract fun onStopped()

    protected abstract fun onDestroyed()

    protected abstract fun finish(): Boolean

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return true
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val finish = finish()
        if (!finish) return true
        release(false)
        return true
    }

    protected fun release(finished: Boolean) {
        if (finished) {
            intent = null
            onResult = null
            val parent = this.parent
            if (parent != null) (parent as ViewGroup).removeView(this)
        }
        mActivity = null
        if (mDecorView != null) {
            focusReturn(mDecorView as ViewGroup, this)
            mDecorView = null
        }
        if (mOwnerLifecycleObserver != null) {
            mOwnerLifecycle.removeObserver(mOwnerLifecycleObserver ?: return)
            mOwnerLifecycleObserver = null
        }
    }

    private fun focusRequest(root: ViewGroup, view: View) {
        root.descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS
        view.isFocusable = true
        view.isFocusableInTouchMode = true
        view.requestFocus()
        view.requestFocusFromTouch()
    }

    private fun focusReturn(root: ViewGroup, view: View) {
        root.descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
        view.isFocusable = false
        view.isFocusableInTouchMode = false
    }

}