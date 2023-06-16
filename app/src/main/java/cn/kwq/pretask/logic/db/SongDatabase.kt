package cn.kwq.pretask.logic.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import cn.kwq.pretask.logic.db.dao.SongDao
import cn.kwq.pretask.logic.db.entity.SongEntity


@Database(version = 1, entities = [SongEntity::class])
abstract class SongDatabase: RoomDatabase() {
    abstract fun getDao():SongDao
    //单例模式
    companion object {
        private var instance: SongDatabase? = null
        @Synchronized
        fun getDatabase(context: Context): SongDatabase {
            //存在实例情况下直接返回
            instance?.let {
                return it
            }
            //不存在情况下新建实例
            return Room
                .databaseBuilder(context.applicationContext, SongDatabase::class.java, "music_db")
                .build().apply { instance = this }
        }
    }


}