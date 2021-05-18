package lin.abcdq.avcache.player.exo

import android.content.Context
import android.os.Build
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleObserver
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import lin.abcdq.avcache.player.AVPlayer
import lin.abcdq.avcache.MCache

@RequiresApi(Build.VERSION_CODES.KITKAT)
class AVExoListPlayerHelper {

    class Data {
        var cover = ""
        var url = ""
        var currentTimeMs = 0L
    }

    //1.初始化(Context + Recyclerview + ControllerView + Lifecycle : onCreate调用)
    fun init(context: Context, v1: RecyclerView, v2: View, lifecycle: Lifecycle) {
        mRecyclerView = v1
        mControllerView = v2
        init(context)
        initLifecycle(lifecycle)
        initScroller(mRecyclerView ?: return)
        MCache.init(context)
    }

    //2.初始化数据(Position + Data : onCreate调用)
    fun set(map: HashMap<Int, Data>) {
        mDataMap = map
    }

    //3.更新调用(onBindViewHolder调用)
    fun onDataSetChanged(ContainerView: View?) {
        if (mScrolling) return
        if (ContainerView != null) mContainerID = ContainerView.id
        exchangePlayer(mRecyclerView ?: return)
    }

    private var mDataMap = HashMap<Int, Data>()        //视频数据
    private var mData: Data? = null                     //记录用

    private var mRecyclerView: RecyclerView? = null
    private var mContainerID = 0                   //RecyclerView中item包裹PlayerView的布局ID
    private var mContainer: ViewGroup? = null      //RecyclerView中item包裹PlayerView的布局
    private var mPlayerView: FrameLayout? = null   //包裹SurfaceView + 控制层UI的布局，动态添加到item中
    private var mSurfaceView: SurfaceView? = null  //surfaceView
    private var mControllerView: View? = null      //控制层UI

    private var lifecycleObserver: LifecycleObserver? = null
    private var scrollListener: RecyclerView.OnScrollListener? = null
    private var mScrolling = false                 //滑动中禁止 onDataSetChanged 计算布局

    //1.初始化播放器+播放器布局+控制UI
    private fun init(context: Context) {
        if (mSurfaceView != null) return
        mSurfaceView = SurfaceView(context)
        AVPlayer.init(context, AVPlayer.PLAYER.EXO)
        AVPlayer.instance().surface(mSurfaceView ?: return)
        mPlayerView = FrameLayout(context)
        val measure = ViewGroup.LayoutParams.MATCH_PARENT
        mPlayerView?.addView(mSurfaceView, measure, measure)
        mPlayerView?.addView(mControllerView, measure, measure)
    }

    //2.初始化Lifecycle(onResume onPause onDestroy)
    private fun initLifecycle(lifecycle: Lifecycle) {
        if (lifecycleObserver != null) return
        lifecycleObserver = LifecycleEventObserver { source, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    if (AVPlayer.instance().surface() != mSurfaceView) {
                        AVPlayer.instance().surface(mSurfaceView ?: return@LifecycleEventObserver)
                    }
                    AVPlayer.instance().play()
                }
                Lifecycle.Event.ON_PAUSE -> {
                    AVPlayer.instance().pause()
                }
                Lifecycle.Event.ON_DESTROY -> {
                    source.lifecycle.removeObserver(lifecycleObserver!!)
                    lifecycleObserver = null
                    mSurfaceView = null
                    scrollListener = null
                    mPlayerView = null
                    AVPlayer.instance().stop()
                }
                else -> {
                }
            }
        }
        lifecycle.addObserver(lifecycleObserver!!)
    }

    //3.RecyclerView滑动监听
    private fun initScroller(view: RecyclerView) {
        if (scrollListener != null) return
        scrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) mScrolling = false
                if (newState == RecyclerView.SCROLL_STATE_IDLE) exchangePlayer(view)
                else mScrolling = true
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!mScrolling) return
                if (mContainer == null) exchangePlayer(view)
                else if (mPlayerView?.parent != null) {
                    if (!(mContainer?.isAttachedToWindow ?: return)) {
                        AVPlayer.instance().pause()
                        mContainer?.removeView(mPlayerView)
                    }
                } else return
            }
        }
        view.addOnScrollListener(scrollListener ?: return)
    }

    //播放器布局计算
    private fun exchangePlayer(view: RecyclerView) {
        //顶部完全可见item + 底部完全可见item -> 计算出两者中间的播放器可放置布局
        val manager = view.layoutManager as LinearLayoutManager
        val indexTop = manager.findFirstCompletelyVisibleItemPosition()
        val indexBottom = manager.findLastCompletelyVisibleItemPosition()
        val indexStart: Int
        val indexEnd: Int
        if (!view.canScrollVertically(1)) {
            indexStart = indexBottom
            indexEnd = indexTop
        } else {
            indexStart = indexTop
            indexEnd = indexBottom
        }
        var holder: RecyclerView.ViewHolder? = null
        var temp: View? = null
        var index = 0
        if (indexStart <= indexEnd) {
            for (i in indexStart..indexEnd) {
                holder = view.findViewHolderForLayoutPosition(i) ?: continue
                temp = holder.itemView.findViewById(mContainerID) ?: continue
                index = i
                if (temp is FrameLayout) break
            }
        } else {
            for (i in indexStart downTo indexEnd) {
                holder = view.findViewHolderForLayoutPosition(i) ?: continue
                temp = holder.itemView.findViewById(mContainerID) ?: continue
                index = i
                if (temp is FrameLayout) break
            }
        }
        if (holder == null || temp == null) return
        holder.setIsRecyclable(false)
        if (temp !is FrameLayout) return
        if (mContainer == temp) return
        //视频播放
        if (mData != null) {
            var time = if (AVPlayer.instance().currentTimeMs() < 0) 0
            else AVPlayer.instance().currentTimeMs()
            mData?.currentTimeMs = time
        }
        mData = mDataMap[index] ?: return
        AVPlayer.instance().resource(MCache.proxy(mData?.url ?: return) ?: return)
        AVPlayer.instance().seekTo(mData?.currentTimeMs ?: return)
        //布局添加
        if (mContainer != null && mPlayerView?.parent != null) mContainer?.removeView(mPlayerView)
        mContainer = temp
        mContainer?.addView(mPlayerView, 0)
    }
}