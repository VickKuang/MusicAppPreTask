package cn.kwq.pretask

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import cn.kwq.pretask.common.getImg
import cn.kwq.pretask.common.readMusicLib
import cn.kwq.pretask.common.simpleToast
import cn.kwq.pretask.common.splitSong
import cn.kwq.pretask.databinding.ActivityMainBinding
import cn.kwq.pretask.logic.db.SongDatabase
import cn.kwq.pretask.logic.db.entity.SongEntity
import cn.kwq.pretask.logic.vm.SongListViewModel
import cn.kwq.pretask.ui.activity.DebugActivity
import cn.kwq.pretask.ui.activity.PlayActivity
import cn.kwq.pretask.ui.adapter.SongsAdapter
import cn.kwq.pretask.ui.helper.media.MediaPlayerHelper
import cn.kwq.pretask.ui.helper.notification.NotificationHelper
import cn.kwq.pretask.ui.helper.notification.NotificationUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity() {
    companion object {
        var router = "Index"//互斥变量,判断当前页面歌单列表
    }
    private lateinit var binding: ActivityMainBinding
    lateinit var vm: SongListViewModel
    private lateinit var instance: MediaPlayerHelper
    private var playAlive = false

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        initDB()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        instance = MediaPlayerHelper.getInstance(this)
        //初始化
        initView()
        initObserve()
        //执行查询数据库
        vm.listSongsFromDB()

        /**
         * 全部-喜欢 转换逻辑
         */
        binding.rlJump.setOnClickListener {
            titleSwitch(binding)
            //虚拟加载条动画
            CoroutineScope(Dispatchers.Main).launch {
                binding.loading.visibility = View.VISIBLE
                withContext(Dispatchers.IO) {
                    delay(500)
                }
                binding.loading.visibility = View.INVISIBLE
            }
            // DONE: 重新刷新列表 封装进去方法内部
        }

        /**
         * 跳转播放详情页
         */
        binding.btnJumpPlay.setOnClickListener {
            if (instance.isAlive()) {
                val intent = Intent(this, PlayActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.bottom_in, R.anim.bottom_out)
            } else {
                "播放列表为空".simpleToast(this)
            }

        }

        //底部栏功能
        binding.ibMainNext.setOnClickListener { instance.next() }
        binding.ibMainPre.setOnClickListener { instance.pre() }
        binding.cbMainStart.setOnClickListener {
            it.isSelected = !it.isSelected
            if (instance.state()) {
                instance.stop()
            } else {
                instance.start()
            }
        }
    }


    /**
     * 初始化ui
     */
    @SuppressLint("ClickableViewAccessibility", "ResourceAsColor")
    private fun initView() {
        //初始化通知栏
        NotificationHelper.initNotification(getSystemService(NOTIFICATION_SERVICE) as NotificationManager,this)
        NotificationUtil.askNotify(this)
        binding.cbMainStart.isSelected = instance.state()
        SongsAdapter.viewModelStoreOwner = this
        vm = ViewModelProvider(this).get(SongListViewModel::class.java)
        //状态栏标题设置
        binding.mainTb.apply {
            titleMarginStart = 150
        }
        //沉浸式状态栏
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        binding.rvSongCards.apply {
            setOnTouchListener { v, event ->
                when (event.action) {
                    //当用户按下的时候，我们告诉父组件，不要拦截我的事件（这个时候子组件是可以正常响应事件的），拿起之后就会告诉父组件可以阻止。
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> v.parent.requestDisallowInterceptTouchEvent(
                        true
                    )

                    MotionEvent.ACTION_UP -> v.parent.requestDisallowInterceptTouchEvent(false)
                }
                false
            }
            layoutParams.height = 2000
        }

        /**
         * 滚动超过10dp隐藏,以及回显
         */
        binding.abRollView.addOnOffsetChangedListener { _, verticalOffset ->
            if (verticalOffset < -10) {
                binding.rlJump.visibility = View.INVISIBLE
            } else if (verticalOffset > 20 || verticalOffset == 0) {
                binding.rlJump.visibility = View.VISIBLE
            }
        }

        /**
         * debug功能
         */
        binding.ivDebug.setOnLongClickListener {
            val intent = Intent(this, DebugActivity::class.java)
            startActivity(intent)
            return@setOnLongClickListener true
        }
    }

    /**
     * 初始化观察者
     */
    private fun initObserve() {
        /**
         *  列表改变时渲染
         */
        vm.songList.observe(this) {
            if (it != null) {
                binding.rvSongCards.adapter = SongsAdapter(it)
                MediaPlayerHelper.getInstance().changeMusicList(it)
            } else {
                binding.rvSongCards.adapter = null
            }
            binding.rvSongCards.layoutManager =
                object : LinearLayoutManager(this, VERTICAL, false) {
                    override fun canScrollVertically(): Boolean {
                        return true
                    }
                }
        }
        /**
         * 歌曲改变时渲染 底部卡片
         */
        vm.playingSong.observe(this) {
            if (instance.isAlive()) {
                binding.btnJumpPlay.setImageBitmap(instance.getPlayingSongMsg().imgPath.getImg())
                binding.tvBottomName.text = instance.getPlayingSongMsg().songName
                binding.tvBottomSinger.text = instance.getPlayingSongMsg().singer
                binding.tvBottomIcon.visibility = View.VISIBLE
            } else {
                if (it != null) {
                    binding.btnJumpPlay.setImageBitmap(it.imgPath.getImg())
                    binding.tvBottomName.text = it.songName
                    binding.tvBottomSinger.text = it.singer
                    binding.tvBottomIcon.visibility = View.VISIBLE
                } else {
                    binding.btnJumpPlay.setImageBitmap(null)
                    binding.tvBottomName.text = ""
                    binding.tvBottomSinger.text = ""
                    binding.tvBottomIcon.visibility = View.INVISIBLE
                    //更新通知栏
                    NotificationHelper.clean()

                }
            }
        }

        //监听歌曲状态 底部按钮变化
        vm.isPlaying.observe(this) {
            if (binding.cbMainStart.isSelected != it) {
                binding.cbMainStart.isSelected = it
            }
        }
    }

    /**
     * 标题内容转换
     */
    private fun titleSwitch(binding: ActivityMainBinding) {
        when (router) {
            "Index" -> {
                binding.tbMainTitle.title = "我收藏的歌曲"
                binding.tvJumpTitle.text = "我的歌曲"
                router = "Like"
                vm.listLikesFromDB()
            }

            "Like" -> {
                binding.tbMainTitle.title = "我的歌曲"
                binding.tvJumpTitle.text = "我收藏的歌曲"
                router = "Index"
                vm.listSongsFromDB()
            }
        }
        binding.cbMainStart.isSelected = false
    }

    /**
     * 初始化数据库 新设备安装时自动加载文件
     */
    private fun initDB() {
        val dao = SongDatabase.getDatabase(this).getDao()
        CoroutineScope(Dispatchers.IO).launch {
            if (dao.listAllSongs().size <= 0) {
                //创建内部文件目录
                if (!filesDir.exists()) filesDir.mkdir()
                val musicPaths = readMusicLib()//context.readMusicLib() Ext.kt
                //插入数据库
                dao.dropAllSongs()
                musicPaths.forEach {
                    val splitSong = it.mp3.splitSong()
                    val song = SongEntity(0, splitSong.songName, it.mp3, it.img, splitSong.singer)
                    dao.insertSongs(song)
                }
            }
        }
    }


}