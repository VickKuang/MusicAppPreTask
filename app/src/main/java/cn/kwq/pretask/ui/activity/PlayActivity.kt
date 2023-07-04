package cn.kwq.pretask.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import cn.kwq.pretask.R
import cn.kwq.pretask.common.BaseActivity
import cn.kwq.pretask.common.getImg
import cn.kwq.pretask.common.isAllScreenDevice
import cn.kwq.pretask.databinding.ActivityPlayBinding
import cn.kwq.pretask.helper.media.MediaPlayerHelper
import cn.kwq.pretask.helper.notification.NotificationUtils
import cn.kwq.pretask.logic.msgProvider.PlayerMsgProvider
import cn.kwq.pretask.ui.broadcast.MediaBroadcast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Timer
import java.util.TimerTask
private const val NEXT="Next"
private const val PRE="Pre"
class PlayActivity : BaseActivity(), View.OnClickListener {

    var isSeekbarChanging = false//互斥变量，防止进度条和定时器冲突。
    lateinit var binding: ActivityPlayBinding
    lateinit var player: MediaPlayerHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayBinding.inflate(layoutInflater)
        setContentView(binding.root)
        player= MediaPlayerHelper.getInstance()

        initView()//初始化工作
        val vm = player.vm

        //当前歌曲发生变化 刷新页面
        vm.playingSong.observe(this) {
            refresh()
        }
        /**
         * 进度条更改
         */
        binding.sebPlaySeekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                binding.tvPlayNow.text = PlayerMsgProvider.getCurrentTime() ?: "0:00"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                isSeekbarChanging = true
                binding.tvPlayNow.text = PlayerMsgProvider.getCurrentChange(seekBar.progress)
            }

            override fun onStopTrackingTouch(seekbar: SeekBar) {
                isSeekbarChanging = false
               /* val intent = Intent(this@PlayActivity,MediaBroadcast::class.java).apply {
                    action = MediaBroadcast.SEEK_TO
                    putExtra("SeekTo",seekbar.progress)
                }
                sendBroadcast(intent)*/
                player.seekTo(seekbar.progress)
                binding.cbSongStart.isSelected = PlayerMsgProvider.isPlaying()


            }
        })



    }

    private fun initView(){
        //非全面屏适配
        if (!isAllScreenDevice()) binding.vBottomBox.visibility = View.GONE
        //初始化点击事件
        initCLick()
        //刷新页面数据
        refresh()
        //初始化定时器
        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                if (!isSeekbarChanging) { syncSeekBar() }
            } }, 0, 100)
    }
    private fun initCLick(){
        binding.ivBack.setOnClickListener(this)
        binding.ibSongPre.setOnClickListener{
            player.pre()
        }
        binding.ibSongNext.setOnClickListener{
            player.next()
        }
        binding.cbSongStart.setOnClickListener(this)
    }

    private fun refresh() {
        /**
         * 获取播放中歌曲数据(渲染界面)
         */
        PlayerMsgProvider.let {
            binding.ivSongImg.setImageBitmap(it.getSong()?.imgPath?.getImg())
            binding.tvPlaySinger.text = it.getSong()?.singer
            binding.tvPlaySongName.text = it.getSong()?.songName
            binding.tvPlayEnd.text = it.getTime()
            //播放状态
            binding.cbSongStart.isSelected = it.isPlaying()
        }
        syncSeekBar()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.iv_back -> {
                finish()
                overridePendingTransition(R.anim.top_in, R.anim.top_out)
            }
            R.id.cb_song_start -> {
                binding.cbSongStart.let {
                    it.isSelected = !it.isSelected
                    if (!PlayerMsgProvider.isPlaying()) {
                        player.start()
                    } else {
                        player.stop()
                    }
                }
            }
        }
    }
    /**
     * 同步seekbar 和 音乐
     */
    private fun syncSeekBar() {
        CoroutineScope(Dispatchers.IO).launch {
            val currentCoverSeek = PlayerMsgProvider.getCurrentCoverSeek()
            val currentTime = PlayerMsgProvider.getCurrentTime()
            withContext(Dispatchers.Main){
                binding.sebPlaySeekBar.progress = currentCoverSeek
                binding.tvPlayNow.text = currentTime
            }

        }
    }


}