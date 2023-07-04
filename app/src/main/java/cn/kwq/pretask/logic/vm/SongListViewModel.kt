package cn.kwq.pretask.logic.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import cn.kwq.pretask.logic.db.SongDatabase
import cn.kwq.pretask.logic.db.entity.SongEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SongListViewModel(application: Application) : AndroidViewModel(application)  {
    //歌单列表
    var songList: MutableLiveData<MutableList<SongEntity>> = MutableLiveData()
    //当前播放歌曲
    var playingSong: MutableLiveData<SongEntity> = MutableLiveData()
    //是否正在播放
    var isPlaying:MutableLiveData<Boolean> = MutableLiveData(false)

    fun listSongsFromDB() {
        val dao = SongDatabase.getDatabase(getApplication()).getDao()
        CoroutineScope(Dispatchers.IO).launch {
            val songs = dao.listAllSongs()
            songList.postValue(null)
            Thread.sleep(500)
            songList.postValue(songs)
            playingSong.postValue(songs[0])
        }
    }

    fun listLikesFromDB() {
        val dao = SongDatabase.getDatabase(getApplication()).getDao()
        CoroutineScope(Dispatchers.IO).launch {
            val songs = dao.listAllLikes()
            songList.postValue(null)
            Thread.sleep(500)
            songList.postValue(songs)
            var post: SongEntity? = null
            if (songs != null && songs.isNotEmpty()) {
                post = songs[0]
            }
            playingSong.postValue(post)
        }
    }

    fun updateLikes(type: Int, id: Long) {
        val dao = SongDatabase.getDatabase(getApplication()).getDao()
        CoroutineScope(Dispatchers.IO).launch {
            dao.updateLike(type, id)
        }
    }

    fun updatePlayingSong(song: SongEntity?) {
        CoroutineScope(Dispatchers.IO).launch {
            playingSong.postValue(song)
            //切歌肯定变为播放
            isPlaying.postValue(true)
        }
    }

    fun changePlayingState(state:Boolean){
        CoroutineScope(Dispatchers.IO).launch {
           isPlaying.postValue(state)
        }
    }



}