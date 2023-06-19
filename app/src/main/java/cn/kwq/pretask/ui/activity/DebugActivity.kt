package cn.kwq.pretask.ui.activity

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import cn.kwq.pretask.common.BaseActivity
import cn.kwq.pretask.common.readMusicLib
import cn.kwq.pretask.common.simpleToast
import cn.kwq.pretask.common.splitSong
import cn.kwq.pretask.databinding.ActivityDebugBinding
import cn.kwq.pretask.logic.db.SongDatabase
import cn.kwq.pretask.logic.db.entity.SongEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class DebugActivity : BaseActivity() {

    companion object {
        //文件读写需要获取的权限
        val PERMISSIONS = arrayOf(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("RemoteViewLayout")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityDebugBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //请求读写权限的回调
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all { it.value }
            if (granted) {
                "获取权限成功".simpleToast(this)
            } else {
                "获取权限失败".simpleToast(this)
            }
        }.launch(PERMISSIONS)//获取权限

        val dao = SongDatabase.getDatabase(this).getDao()

        binding.debugWriteMusic.setOnClickListener {
            //创建内部文件目录
            if (!filesDir.exists()) filesDir.mkdir()
            val musicPaths = readMusicLib()//context.readMusicLib()

            //插入数据库
            CoroutineScope(Dispatchers.IO).launch {
                dao.dropAllSongs()
                musicPaths.forEach {
                    val splitSong = it.mp3.splitSong()
                    val song = SongEntity(0, splitSong.songName, it.mp3, it.img, splitSong.singer)
                    dao.insertSongs(song)
                }
            }


        }

        binding.debugPlayMusic.setOnClickListener {
//            val list = ArrayList<String>()
//            list.add("musicLib/知足/知足-五月天.mp3")
//            val mediaPlayer = MediaPlayerHelper.getInstance()
//            mediaPlayer.changeMusicList(list)
//            mediaPlayer.start()
            "未设置功能".simpleToast(this)
        }

        binding.debugSelect.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val songs = dao.listAllSongs()
                songs.forEach {
                    Log.i("DB_DEBUG", it.singer)
                }
            }
        }

    }

}
