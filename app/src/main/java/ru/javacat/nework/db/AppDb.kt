package ru.javacat.nework.db

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.javacat.nework.dao.PostDao
import ru.javacat.nework.dao.PostRemoteKeyDao
import ru.javacat.nework.entity.PostEntity
import ru.javacat.nework.entity.PostRemoteKeyEntity

@Database(entities = [PostEntity::class, PostRemoteKeyEntity::class], version = 1)
abstract class AppDb : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun postRemoteKeyDao(): PostRemoteKeyDao

}
