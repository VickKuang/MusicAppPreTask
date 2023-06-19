package cn.kwq.pretask.helper.notification

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import cn.kwq.pretask.MainActivity
import cn.kwq.pretask.R
import cn.kwq.pretask.common.getImg
import cn.kwq.pretask.logic.msgProvider.PlayerMsgProvider
import cn.kwq.pretask.ui.broadcast.MediaBroadcast


@SuppressLint("RemoteViewLayout")
object NotificationUtils {
    private const val CHANNEL_ID = "MusicPlayer"
    private const val NOTIFICATION_ID = 10086
    private lateinit var nm : NotificationManager

    private const val NEXT="Next"
    private const val PRE="Pre"

    private lateinit var context : AppCompatActivity

    @SuppressLint("RemoteViewLayout")
    fun initNotification(nm:NotificationManager,context:MainActivity){
        //创建渠道
        val b: NotificationChannel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            b = NotificationChannel(CHANNEL_ID, "通知栏音乐播放", NotificationManager.IMPORTANCE_MIN)
            nm.createNotificationChannel(b)
        }

        //添加自定义视图  activity_notification
        NotificationUtils.nm =nm
        NotificationUtils.context = context
    }
     fun clean(){
         nm.cancel(NOTIFICATION_ID)
     }

     fun update(songName:String,singer:String,imgPath:String){
         val remoteView= RemoteViews("cn.kwq.pretask", R.layout.view_paly_notification_small)
         val bigRemoteView= RemoteViews("cn.kwq.pretask", R.layout.view_paly_notification)

         val nextToDo = if (PlayerMsgProvider.isPlaying()) { "Stop" } else { "Start" }//判断点击后动作

         initContent(remoteView,nextToDo,songName,singer,imgPath)
         initBigContent(bigRemoteView,nextToDo,songName,singer,imgPath)

         //主要设置setContent参数  其他参数 按需设置
         val customNotification = NotificationCompat.Builder(context, CHANNEL_ID)
             .apply {
             setStyle(NotificationCompat.DecoratedCustomViewStyle())
             setCustomBigContentView(bigRemoteView)
             setSmallIcon(R.drawable.icon_more)
             setOngoing(true)
             setCustomContentView(remoteView)
             }.build()
         //更新Notification
         nm.notify(NOTIFICATION_ID, customNotification)
     }

    private fun initBigContent(rv:RemoteViews, ntd:String, songName:String, singer:String, imgPath:String){
        /**
         * 暂停按钮 点击事件
         */
        val intent = Intent(context,MediaBroadcast::class.java)
        intent.action = ntd
        val pendingIntent: PendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_MUTABLE)
        rv.setOnClickPendingIntent (R.id.iv_click_bn,pendingIntent)
        val icon = if (ntd != "Stop") { R.drawable.icon_play_n } else { R.drawable.icon_stop_n }//判断当前播放状态

        /**
         * 下一曲
         */
        val intentNext = Intent(context,MediaBroadcast::class.java)
        intentNext.action = NEXT
        val pendingIntentN: PendingIntent = PendingIntent.getBroadcast(context, 0, intentNext, PendingIntent.FLAG_MUTABLE)
        rv.setOnClickPendingIntent (R.id.iv_next_bn,pendingIntentN)

        /**
         * 上一曲
         */
        val intentPre = Intent(context,MediaBroadcast::class.java)
        intentPre.action = PRE
        val pendingIntentP: PendingIntent = PendingIntent.getBroadcast(context, 0, intentPre, PendingIntent.FLAG_MUTABLE)
        rv.setOnClickPendingIntent (R.id.iv_pre_bn,pendingIntentP)


        rv.apply {
            setImageViewResource(R.id.iv_click_bn,icon)//播放图标
            setTextViewText(R.id.tv_singer_bn, singer)
            setTextViewText(R.id.tv_song_name_bn, songName)
            setImageViewBitmap(R.id.iv_img_bn, imgPath.getImg())
        }
    }

    private fun initContent(rv:RemoteViews, ntd: String, songName:String, singer:String, imgPath:String){
        /**
         * 暂停按钮 点击事件
         */
        val intent = Intent(context,MediaBroadcast::class.java)
        intent.action = ntd
        val pendingIntent: PendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_MUTABLE)
        rv.setOnClickPendingIntent (R.id.iv_click_n,pendingIntent)

        val icon = if (ntd != "Stop") { R.drawable.icon_play_n } else { R.drawable.icon_stop_n }//判断当前播放状态

        rv.apply {
            setImageViewResource(R.id.iv_click_n,icon)//播放图标
            setTextViewText(R.id.tv_singer_n, singer)
            setTextViewText(R.id.tv_song_name_n, songName)
            setImageViewBitmap(R.id.iv_song_img_n, imgPath.getImg())

        }
    }



}