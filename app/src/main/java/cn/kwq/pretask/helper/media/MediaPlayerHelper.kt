package cn.kwq.pretask.helper.media


import android.media.MediaPlayer
import androidx.lifecycle.ViewModelProvider
import cn.kwq.pretask.MainActivity
import cn.kwq.pretask.common.MyApplication
import cn.kwq.pretask.helper.notification.NotificationUtils
import cn.kwq.pretask.logic.db.entity.SongEntity
import cn.kwq.pretask.logic.dto.PlayingSongMsgDTO
import cn.kwq.pretask.logic.vm.SongListViewModel
import kotlin.streams.toList

/**
 * 单例模式
 */
object MediaPlayerHelper {

    private var musicList: List<SongEntity> = ArrayList<SongEntity>()//播放列表
    private var mediaPlayer: MediaPlayer? = null  //播放器
    private var index = 0 //歌单当前进度
    lateinit var vm: SongListViewModel//MainActivity的vm用于更新当前播放

    /**
     * 保证单例（获得实体播放器的时候一定是当前唯一的播放器）
     */
    fun getInstance(): MediaPlayerHelper {
        mediaPlayer?.also{
            return this
        }
        mediaPlayer = MediaPlayer()
        return this
    }

    /**
     * 保证单例（获得实体播放器的时候一定是当前唯一的播放器）
     */
    fun getInstance(mainActivity: MainActivity): MediaPlayerHelper {
        mediaPlayer?.also{
            return this
        }
        vm = ViewModelProvider(mainActivity).get(SongListViewModel::class.java)
        mediaPlayer = MediaPlayer()
        mediaPlayer?.setOnCompletionListener {
            next()
        }
        return this
    }

    /**
     * 获取当前播放歌曲相关信息
     */
    fun getPlayingSongMsg(): PlayingSongMsgDTO {
        return if (mediaPlayer != null&& musicList.isNotEmpty()){
            PlayingSongMsgDTO(
                musicList[index], mediaPlayer!!.isPlaying,true,
                mediaPlayer!!.duration,
                mediaPlayer!!.currentPosition)
        }else{
            PlayingSongMsgDTO(null,false, isAlive = false,null,null)
        }
    }

    /**
     * 播放列表改变时，刷新播放器内容
     */
    fun changeMusicList(list: List<SongEntity>) {
        musicList = list
        if (list.isNotEmpty()) {
            setMusicPath(list[0].path)
            list[0].let {
                NotificationUtils.update(it.songName,it.singer,it.imgPath)
            }
        }
        index = 0

    }

    /**
     * 下一曲
     */
    fun next() {
        if (musicList.isNotEmpty()){
            if (index < (musicList.size - 1)) {
                index += 1
            } else {
                index = 0
            }
            setMusicPath(musicList[index].path)
            musicList[index].let {
                NotificationUtils.update(it.songName,it.singer,it.imgPath)
            }
            start()
        }
    }

    /**
     * 准备播放
     */
    fun prepare(path: String) {
        setMusicPath(path)
        val to = musicList.stream().map { i -> i.path }.toList()
        index = to.indexOf(path)
    }

    /**
     * 上一曲
     */
    fun pre() {
        if (index > 0) {
            index -= 1
        } else {
            index = musicList.size - 1
        }
        setMusicPath(musicList[index].path)
        musicList [index].let {
            NotificationUtils.update(it.songName,it.singer,it.imgPath)
        }
        start()
    }

    /**
     * 播放
     */
    fun start() {
        mediaPlayer?.let { player->
            if (musicList.isNotEmpty()){
                player.start()
                vm.updatePlayingSong(getPlayingSongMsg().song)
                musicList [index].let {
                    NotificationUtils.update(it.songName,it.singer,it.imgPath)
                }
            }
        }


    }

    /**
     * 移动相关位置
     */
    fun seekTo(pos: Int) {
        mediaPlayer?.apply {
            val ms = duration.times((pos.toDouble() / 100)).toInt()
            seekTo(ms)
            start()
            if (musicList.isNotEmpty()){
                musicList [index].let {
                    NotificationUtils.update(it.songName,it.singer,it.imgPath)
                }
            }

        }

    }

    /**
     * 暂停
     */
    fun stop() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
            vm.changePlayingState(false)
            musicList [index].let {
                NotificationUtils.update(it.songName,it.singer,it.imgPath)
            }
        }
    }

    /**
     * 切换歌曲就要销毁上一个播放器 保证播放器唯一
     */
    private fun setMusicPath(path: String?) {
        if (mediaPlayer ==null){
            mediaPlayer = MediaPlayer()
        }
        mediaPlayer?.reset()//更新播放器
        mediaPlayer?.let{
            mediaPlayer =MediaPlayer()
        }
        path?.let {
            val musicFile = MyApplication.context.assets.openFd(it)
            mediaPlayer?.setDataSource(musicFile)
            mediaPlayer?.prepare()
        }
    }


}