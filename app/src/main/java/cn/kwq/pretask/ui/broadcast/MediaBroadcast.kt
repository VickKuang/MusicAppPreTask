package cn.kwq.pretask.ui.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import cn.kwq.pretask.helper.media.MediaPlayerHelper

class MediaBroadcast: BroadcastReceiver() {
    companion object{
        const val STOP="Stop"
        const val START="Start"
        const val NEXT="Next"
        const val PRE="Pre"
        const val SEEK_TO="SeekTo"
        const val START_WITH_PATH="StartWithPath"
    }



    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.e("After",System.currentTimeMillis().toString())
        // TODO: 不同步问题
        intent?.action?.also{ it ->
            val instance = MediaPlayerHelper.getInstance()
            when(it){
                STOP -> instance.stop()
                START -> instance.start()
                NEXT -> instance.next()
                PRE -> instance.pre()
                SEEK_TO -> instance.seekTo(intent.getIntExtra("SeekTo",0))
                START_WITH_PATH ->{intent.getStringExtra("StartWithPath")?.let { instance.apply { prepare(it) }.start() }}
            }
        }

    }
}