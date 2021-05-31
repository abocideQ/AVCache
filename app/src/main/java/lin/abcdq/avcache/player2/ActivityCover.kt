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

    fun startActivityCover() {
        init()
    }

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

    fun finish() {
        release()
    }

    private var intent: Intent? = null
    private var onResult: Runnable? = null
    private var mActivity: Activity? = context
    private var mDecorView: View? = context.window.decorView
    private var mOwnerLifecycle: Lifecycle = context.lifecycle
    private var mOwnerLifecycleObserver: LifecycleEventObserver? = null

    private fun init() {
        initLifecycle()
        initView()
    }

    private fun initLifecycle() {
        mOwnerLifecycleObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    onActivityResumed()
                }
                Lifecycle.Event.ON_PAUSE -> {
                    onActivityPaused()
                }
                Lifecycle.Event.ON_STOP -> {
                    onActivityStopped()
                }
                Lifecycle.Event.ON_DESTROY -> {
                    onActivityDestroyed()
                }
                else -> {
                }
            }
        }
        mOwnerLifecycle.addObserver(mOwnerLifecycleObserver ?: return)
    }

    private fun initView() {
        if (mDecorView == null) return
        focusRequest(mDecorView as ViewGroup, this)
        val measure = ViewGroup.LayoutParams.MATCH_PARENT
        (mDecorView as ViewGroup).addView(this, measure, measure)
        val view = LayoutInflater.from(context).inflate(onCoverCreateView(), null)
        this.addView(view, measure, measure)
        onCoverViewCreated(view)
    }

    private fun release() {
        if (mDecorView != null) {
            focusReturn(mDecorView as ViewGroup, this)
            mDecorView = null
        }
        if (mOwnerLifecycleObserver != null) {
            mOwnerLifecycle.removeObserver(mOwnerLifecycleObserver ?: return)
            mOwnerLifecycleObserver = null
        }
        if (this.parent != null) (this.parent as ViewGroup).removeView(this)
        intent = null
        onResult = null
        mActivity = null
    }

    protected abstract fun onCoverCreateView(): Int

    protected abstract fun onCoverViewCreated(view: View)

    protected abstract fun onActivityResumed()

    protected abstract fun onActivityPaused()

    protected abstract fun onActivityStopped()

    protected abstract fun onActivityDestroyed()

    protected abstract fun onBackPress()

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return true
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        onBackPress()
        return true
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