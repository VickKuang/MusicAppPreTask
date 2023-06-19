package cn.kwq.pretask.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.os.Build
import android.view.WindowManager
import android.widget.Toast
import cn.kwq.pretask.logic.dto.MusicMsgDTO
import cn.kwq.pretask.logic.dto.MusicPathDTO


fun String.simpleToast(context: Context?) {
    Toast.makeText(context, this, Toast.LENGTH_SHORT).show()
}

/**
 * assets文件解析路径 工具
 */
fun Context.readMusicLib(): ArrayList<MusicPathDTO> {
    val list = ArrayList<MusicPathDTO>()
    //全部目录
    val musicList = assets.list("musicLib")
    musicList?.forEach { root ->
        var songPath = ""
        var imagePath = ""
        //具体每个目录的内容
        val itemList = assets.list("musicLib/${root}")
        itemList?.forEach { item ->
            val split = item.split(".")
            when (split[split.size - 1]) {
                "mp3" -> {
                    songPath = "musicLib/${root}/${item}"
                }
                else -> {
                    imagePath = "musicLib/${root}/${item}"
                }
            }
        }
        val pathObj = MusicPathDTO(songPath, imagePath)
        list.add(pathObj)
    }
    return list
}

/**
 * 裁剪歌曲名工具(歌曲名格式为songName-singer1,singer2,singer3...)
 */
fun String.splitSong(): MusicMsgDTO {
    val split0 = this.split("/")
    val split1 = split0[split0.size - 1].split("-")
    val songName = split1[0]
    val split2 = split1[1].split(".")
    var singer = String()
    //歌手名字有.的情况处理
    val subList = split2.subList(0, split2.size - 1)
    subList.forEach {
        singer += ".$it"
    }

    return MusicMsgDTO(songName, singer.substring(1, singer.length))
}

/**
 * 读取assets图片工具
 */
fun String.getImg(): Bitmap? {
    val open = MyApplication.context.assets.open(this)
    val bitmap = BitmapFactory.decodeStream(open)
    open.close()
    return bitmap
}

/**
 * 毫秒转换播放时间
 */
fun Int.calculateTime(): String? {
    val time = this / 1000
    val minute: Int
    val second: Int
    return if (time >= 60) {
        minute = time / 60
        second = time % 60
        //分钟在0~9
        if (minute < 10) {
            //判断秒
            if (second < 10) {
                "0$minute:0$second"
            } else {
                "0$minute:$second"
            }
        } else {
            //分钟大于10再判断秒
            if (second < 10) {
                "$minute:0$second"
            } else {
                "$minute:$second"
            }
        }
    } else {
        second = time
        if (second in 0..9) {
            "00:0$second"
        } else {
            "00:$second"
        }
    }
}

/**
 * 判断全面屏
 */
fun Context.isAllScreenDevice(): Boolean {
    // 低于 API 21的，基本不是全面屏
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        return false;
    }
    val windowManager = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val display = windowManager.getDefaultDisplay();
    val point = Point()
    display.getRealSize(point)
    var width = 0.0
    var height = 0.0
    if (point.x < point.y) {
        width = point.x.toDouble();
        height = point.y.toDouble();
    } else {
        width = point.y.toDouble();
        height = point.x.toDouble();
    }
    return height / width >= 1.97f
}

/**
 * 毫秒转换进度条百分比
 */
fun Int.coverSeekBar(songLong:Int):Int{
    return (((this.toDouble() / songLong.toDouble()) * 100)).toInt()
}

