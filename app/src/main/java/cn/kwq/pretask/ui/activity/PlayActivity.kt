package cn.kwq.pretask.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.lifecycle.ViewModelProvider
import cn.kwq.pretask.MainActivity
import cn.kwq.pretask.R
import cn.kwq.pretask.common.calculateTime
import cn.kwq.pretask.common.getImg
import cn.kwq.pretask.common.isAllScreenDevice
import cn.kwq.pretask.databinding.ActivityPlayBinding
import cn.kwq.pretask.logic.vm.SongListViewModel
import cn.kwq.pretask.ui.adapter.SongsAdapter
import cn.kwq.pretask.ui.helper.media.MediaPlayerHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Timer
import java.util.TimerTask
import kotlin.concurrent.timer
import kotlin.coroutines.coroutineContext
import kotlin.math.log

class PlayActivity : AppCompatActivity() {
    var isSeekbarChanging = false//互斥变量，防止进度条和定时器冲突。

    lateinit var binding: ActivityPlayBinding
    private var songLong: Int = 0
    val instance = MediaPlayerHelper.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (!isAllScreenDevice()) binding.vBottomBox.visibility = View.GONE //非全面屏适配

        //当前歌曲发生变化 刷新页面
        instance.vm.playingSong.observe(this) {
            refresh()
        }

        /**
         * 返回按钮
         */
        binding.ivBack.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.top_in, R.anim.top_out)

        }
        refresh()//刷新数据

        binding.ibSongPre.setOnClickListener {
            instance.pre()
        }

        binding.ibSongNext.setOnClickListener {
            instance.next()
        }
        binding.cbSongStart.setOnClickListener {
            it.isSelected = !it.isSelected
            if (instance.state()) {
                instance.stop()
            } else {
                instance.start()
            }
        }
        /**
         * 进度条更改
         */
        binding.sebPlaySeekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                val songWhen = instance.getSongWhen()
                binding.tvPlayNow.text = songWhen ?: "0:00"
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                isSeekbarChanging = true
            }

            override fun onStopTrackingTouch(seekbar: SeekBar) {
                isSeekbarChanging = false
                instance.seekTo(seekbar.progress)
                binding.cbSongStart.isSelected = instance.state()

            }
        })
        /**
         * 定时器更改进度条状态
         */
        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                if (!isSeekbarChanging) {
                    syncSeekBar()
                }
            }
        }, 0, 500)


    }

    private fun refresh() {
        /**
         * 获取播放中歌曲数据(渲染界面)
         */
        binding.ivSongImg.setImageBitmap(instance.getPlayingSongMsg().imgPath.getImg())
        binding.tvPlaySinger.text = instance.getPlayingSongMsg().singer
        binding.tvPlaySongName.text = instance.getPlayingSongMsg().songName
        binding.tvPlayEnd.text = instance.getSongSize() ?: "0:00"
        songLong = instance.getSongLong() ?: 0
        //播放状态
        binding.cbSongStart.isSelected = instance.state()

        syncSeekBar()
    }

    /**
     * 同步seekbar 和 音乐
     */
    private fun syncSeekBar() {
        CoroutineScope(Dispatchers.Main).launch {
            binding.tvPlayNow.text = instance.getSongWhen() ?: "0:00"
            instance.getSongNow()?.let {
                binding.sebPlaySeekBar.progress =
                    (it.toDouble() / songLong.toDouble() * 100).toInt()
            }
        }
    }


}