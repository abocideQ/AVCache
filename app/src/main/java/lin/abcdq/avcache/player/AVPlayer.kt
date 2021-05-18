package lin.abcdq.avcache.player

import android.annotation.SuppressLint
import android.content.Context
import lin.abcdq.avcache.player.base.BaseCJPlayer
import lin.abcdq.avcache.player.exo.AVExoPlayer

class AVPlayer {

    enum class PLAYER {
        EXO
    }

    companion object {

        @SuppressLint("StaticFieldLeak")
        private var mContext: Context? = null
        private var mType: PLAYER? = null

        fun init(context: Context, enum: PLAYER) {
            mContext = context
            mType = enum
        }

        @Volatile
        private var mPlayer: BaseCJPlayer? = null

        fun instance(): BaseCJPlayer {
            if (mPlayer == null) {
                synchronized(AVPlayer::class) {
                    if (mPlayer == null)
                        mPlayer = if (mType == PLAYER.EXO) AVExoPlayer(mContext!!)
                        else AVExoPlayer(mContext!!)
                }
            }
            return mPlayer!!
        }
    }
}