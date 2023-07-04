package cn.kwq.pretask.logic.msgProvider

import cn.kwq.pretask.common.calculateTime
import cn.kwq.pretask.common.coverSeekBar
import cn.kwq.pretask.helper.media.MediaPlayerHelper
import cn.kwq.pretask.logic.db.entity.SongEntity
import kotlin.time.times

object PlayerMsgProvider {
    private val player = MediaPlayerHelper.getInstance()

    fun getSong(): SongEntity? {
        return player.getPlayingSongMsg().song
    }

    fun isAlive(): Boolean {
        return player.getPlayingSongMsg().isAlive
    }

    fun isPlaying(): Boolean {
        return player.getPlayingSongMsg().isPlaying
    }

    fun getCurrent(): Int? {
        return player.getPlayingSongMsg().songCurrent
    }

    fun getCurrentCoverSeek(): Int {
        return player.getPlayingSongMsg().songCurrent
            ?.coverSeekBar((player.getPlayingSongMsg().songSize ?: 0))
            ?:0
    }

    fun getCurrentTime(): String {
        return player.getPlayingSongMsg().songCurrent?.calculateTime() ?: "0:00"
    }

    fun getTime(): String {
        return player.getPlayingSongMsg().songSize?.calculateTime() ?: "3:00"
    }

    fun getCurrentChange(pos:Int): String {
        val songIndex = pos.toDouble() / 100
        return player.getPlayingSongMsg().songSize?.times(songIndex)?.toInt()?.calculateTime()?: "0:00"


    }

}