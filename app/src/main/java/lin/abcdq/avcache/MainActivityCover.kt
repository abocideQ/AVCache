package lin.abcdq.avcache

import android.view.View
import androidx.fragment.app.FragmentActivity
import lin.abcdq.avcache.player2.ActivityCover

class MainActivityCover(context: FragmentActivity) : ActivityCover(context) {

    override fun onCoverCreateView(): Int {
        return 0
    }

    override fun onCoverViewCreated(view: View) {
    }

    override fun onActivityResumed() {
    }

    override fun onActivityPaused() {
    }

    override fun onActivityStopped() {
    }

    override fun onActivityDestroyed() {
    }

    override fun onBackPress() {
    }
}