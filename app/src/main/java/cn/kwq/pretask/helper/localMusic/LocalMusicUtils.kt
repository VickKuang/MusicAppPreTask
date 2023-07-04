package cn.kwq.pretask.helper.localMusic

import android.content.Context
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import cn.kwq.pretask.common.BaseActivity
import cn.kwq.pretask.common.simpleToast
import cn.kwq.pretask.logic.db.entity.SongEntity
import cn.kwq.pretask.ui.activity.DebugActivity

/**
 * 本地音乐扫描工具
 *
 */
object LocalMusicUtils {

    //申请权限列表
    private val PERMISSIONS = arrayOf(
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    /**
     * 扫描系统里面的音频文件，返回一个list集合
     */
    fun getMusicData(context: BaseActivity): List<SongEntity> {
        //请求读写权限的回调
        context.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all { it.value }
            if (granted) {
                "获取权限成功".simpleToast(context)
            } else {
                "获取权限失败".simpleToast(context)
            }
        }.launch(PERMISSIONS)//获取权限

        val list: MutableList<SongEntity> = ArrayList<SongEntity>()
        // 媒体库查询语句（写一个工具类MusicUtils）
        val cursor = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null,
            null, MediaStore.Audio.Media.IS_MUSIC
        )
        if (cursor != null) {
            while (cursor.moveToNext()) {
                //歌曲名称
                var songName =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                //歌手
                var singer =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                //专辑名
                val album =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
                //歌曲路径
                val path =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                //歌曲时长
                val duration =
                    cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                //歌曲大小
                val size =
                    cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE))
                if (size > 1000 * 800) {
                    // 注释部分是切割标题，分离出歌曲名和歌手 （本地媒体库读取的歌曲信息不规范）
                    if (songName.contains("-")) {
                        val str: List<String> = songName.split("-")
                        singer = str[0]
                        songName = str[1]
                    }
                    list.add(SongEntity(0,songName,path,"",singer))
                }
            }
            // 释放资源
            cursor.close()
        }
        return list
    }


}
