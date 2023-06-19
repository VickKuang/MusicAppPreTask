package cn.kwq.pretask.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import cn.kwq.pretask.MainActivity
import cn.kwq.pretask.R
import cn.kwq.pretask.common.getImg
import cn.kwq.pretask.helper.media.MediaPlayerHelper
import cn.kwq.pretask.logic.db.entity.SongEntity
import cn.kwq.pretask.logic.vm.SongListViewModel
import com.google.android.material.imageview.ShapeableImageView

class SongsAdapter(private val list: MutableList<SongEntity>) : Adapter<SongsAdapter.Holder>() {

    companion object {
        // 获取viewModel需要用到这个，必须先在MainActivity的onCreate方法中传过来
        var viewModelStoreOwner: MainActivity? = null
    }

    private val vm = ViewModelProvider(viewModelStoreOwner!!).get(SongListViewModel::class.java)

    inner class Holder(item: View) : ViewHolder(item) {
        val songImg: ShapeableImageView = item.findViewById<ShapeableImageView>(R.id.iv_song_card)
        val songName: TextView = item.findViewById<TextView>(R.id.tv_song_name)
        val singer: TextView = item.findViewById<TextView>(R.id.tv_song_singer)
        val isLike: CheckBox = item.findViewById<CheckBox>(R.id.cb_is_like)

        val itemBox = item
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {

        val inflate =
            LayoutInflater.from(parent.context).inflate(R.layout.item_song_card, parent, false)
        return Holder(inflate)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val songEntity = list[position]
        holder.songImg.setImageBitmap(songEntity.imgPath.getImg())
        holder.songName.text = songEntity.songName
        holder.singer.text = songEntity.singer
        holder.isLike.isSelected = songEntity.isLike != 0

        //点击播放
        holder.itemBox.setOnClickListener {
/*            val intent = Intent(viewModelStoreOwner, MediaBroadcast::class.java).apply {
                action = MediaBroadcast.SEEK_TO
                putExtra("StartWithPath",seekbar.progress)
            }
            sendBroadcast(intent)*/
            val instance = MediaPlayerHelper.getInstance()
            instance.prepare(songEntity.path)
            instance.start()

        }
        //收藏
        holder.isLike.setOnClickListener {
            holder.isLike.isSelected = !holder.isLike.isSelected
            // TODO: 操作数据库
            //数据库操作
            val like = if (!holder.isLike.isSelected) { 0 } else { 1 }
            vm.updateLikes(like, songEntity.id)
            //删除列表
            if (MainActivity.router == "Like" && like == 0) {
                list.remove(songEntity)
                this.notifyItemRemoved(position)
                this.notifyItemRangeChanged(position, list.size - position)
                vm.updatePlayingSong(null)
            }
        }

    }

}