package ru.javacat.nework.db

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.javacat.nework.dao.PostDao
import ru.javacat.nework.entity.PostEntity

@Database(entities = [PostEntity::class], version = 1)
abstract class AppDb : RoomDatabase() {
    abstract fun postDao(): PostDao

}
