package lin.abcdq.avcache

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_player.view.*
import lin.abcdq.avcache.player2.utils.AVPlayerRecyclerViewHelper
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    /**
     * CacheProxy.Builder(context).build()
     * CacheProxy.proxyUrl(url)
     * CacheProxy.preload(url)
     */

    private val mVideos = arrayOf(
        "http://8.136.101.204/v/饺子主动.mp4",
        "http://8.136.101.204/v/饺子运动.mp4",
        "http://8.136.101.204/v/饺子想吹.mp4",
        "http://8.136.101.204/v/饺子汪汪.mp4"
    )

    private val mAVData = HashMap<Int, AVPlayerRecyclerViewHelper.Data>()
    private val mAVPlayerHelper = AVPlayerRecyclerViewHelper()
    private val mList = ArrayList<Any>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        MCache.init(this)
        mRecyclerView.layoutManager = LinearLayoutManager(this)
        mRecyclerView.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

            override fun getItemViewType(position: Int): Int {
                return if (mList[position] is Int) R.layout.item_place
                else R.layout.item_player
            }

            override fun onCreateViewHolder(parent: ViewGroup, type: Int): RecyclerView.ViewHolder {
                return object : RecyclerView.ViewHolder(
                    LayoutInflater.from(baseContext).inflate(type, null)
                ) {}
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                mAVPlayerHelper.onDataSetChanged(holder.itemView.mContainer)
                if (mList[holder.adapterPosition] is Int) return
            }

            override fun getItemCount(): Int {
                return mList.size
            }
        }
        mAVPlayerHelper.init(this, mRecyclerView, View(this), mAVData)
        initData()
    }

    private fun initData() {
        for (i in 0..50) {
            if (i % 2 == 0) {
                val url = mVideos[Random.nextInt(4)]
                mList.add(url)
                mAVData[mList.size - 1] = newData(url)
                MCache.preProxy("" + url)
            } else mList.add(i)
        }
        mRecyclerView.adapter?.notifyDataSetChanged()
    }

    private fun newData(url: String): AVPlayerRecyclerViewHelper.Data {
        return AVPlayerRecyclerViewHelper.Data("", url, 0)
    }
}