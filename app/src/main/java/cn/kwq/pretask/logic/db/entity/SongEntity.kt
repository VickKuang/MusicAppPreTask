package cn.kwq.pretask.logic.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class SongEntity(@ColumnInfo(name = "is_like")
                      var isLike:Int,
                      @ColumnInfo(name = "song_name")
                      var songName:String,
                      @ColumnInfo(name = "path")
                      var path:String,
                      @ColumnInfo(name = "img_path")
                      var imgPath:String,
                      @ColumnInfo(name = "singer")
                      var singer:String) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

}