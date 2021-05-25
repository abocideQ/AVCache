package lin.abcdq.avcache.player2.utils

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator

class TransitionAnimation(
    private val view: ViewGroup,
    private val start: ViewAttr,
    private val end: ViewAttr,
    private val duration: Long
) {

    fun startAnim(endRunnable: Runnable?) {
        val xAnim = ObjectAnimator.ofFloat(view, "x", start.x.toFloat(), end.x.toFloat())
        val yAnim = ObjectAnimator.ofFloat(view, "y", start.y.toFloat(), end.y.toFloat())
        val widthAnim = ValueAnimator.ofInt(start.width, end.width)
        val heightAnim = ValueAnimator.ofInt(start.height, end.height)
        widthAnim.addUpdateListener { valueAnimator ->
            val param = view.layoutParams
            param.width = valueAnimator.animatedValue as Int
            view.layoutParams = param
        }
        heightAnim.addUpdateListener { valueAnimator ->
            val param = view.layoutParams
            param.height = valueAnimator.animatedValue as Int
            view.layoutParams = param
        }
        val animation = AnimatorSet()
        animation.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                endRunnable?.run()
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationRepeat(animation: Animator?) {
            }
        })
        animation.playTogether(xAnim, yAnim, widthAnim, heightAnim)
        animation.duration = duration
        animation.interpolator = DecelerateInterpolator()
        animation.start()
    }
}