package lin.abcdq.avcache.player2

import android.content.Context
import lin.abcdq.avcache.player2.exo.AVExoPlayer

class AVPlayerFactory {

    enum class Player {
        Exo,
        Other
    }

    companion object {

        private val mPlayerLock = Any()

        @Synchronized
        fun init(context: Context, player: Player) {
            synchronized(mPlayerLock) {
                if (mAVPlayer == null) {
                    mAVPlayer = when (player) {
                        Player.Exo -> AVExoPlayer(context)
                        Player.Other -> AVExoPlayer(context)
                    }
                }
            }
        }

        @Synchronized
        fun release() {
            mAVPlayer?.release()
            mAVPlayer = null
        }

        @Volatile
        private var mAVPlayer: AVBasePlayer? = null

        @Synchronized
        fun instance(): AVBasePlayer {
            synchronized(mPlayerLock) {
                return mAVPlayer!!
            }
        }
    }
}