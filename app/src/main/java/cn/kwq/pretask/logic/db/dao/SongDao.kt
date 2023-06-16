package cn.kwq.pretask.logic.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import cn.kwq.pretask.logic.db.entity.SongEntity

@Dao
interface SongDao {
    @Query("DELETE FROM songs WHERE id IS NOT NULL")
    fun dropAllSongs()
    @Insert
    fun insertSongs(song:SongEntity)
    @Query("SELECT * FROM songs")
    fun listAllSongs():MutableList<SongEntity>
    @Query("SELECT * FROM songs WHERE is_like=1")
    fun listAllLikes():MutableList<SongEntity>
    @Query("UPDATE songs SET is_like=:type  WHERE id=:id")
    fun updateLike(type:Int,id:Long)
    @Query("SELECT * FROM songs WHERE id=:id")
    fun findSongById(id:Int):SongEntity


}