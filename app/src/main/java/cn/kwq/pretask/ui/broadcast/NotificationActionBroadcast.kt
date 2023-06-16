package cn.kwq.pretask.ui.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import cn.kwq.pretask.common.simpleToast
import cn.kwq.pretask.ui.helper.media.MediaPlayerHelper

class NotificationActionBroadcast: BroadcastReceiver() {

    private val STOP="Stop"
    private val START="Start"
    private val NEXT="Next"
    private val PRE="Pre"


    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.action?.let{
            val instance = MediaPlayerHelper.getInstance()
            when(it){
                STOP -> instance.stop()
                START -> instance.start()
                NEXT -> instance.next()
                PRE -> instance.pre()
            }
        }

    }
}