package ru.javacat.nework.data

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.javacat.nework.data.dao.PostDao
import ru.javacat.nework.data.dao.PostRemoteKeyDao
import ru.javacat.nework.data.entity.LikeOwnersEntity
import ru.javacat.nework.data.entity.PostEntity
import ru.javacat.nework.data.entity.PostRemoteKeyEntity

@Database(entities = [PostEntity::class, PostRemoteKeyEntity::class, LikeOwnersEntity::class], version = 1)
abstract class AppDb : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun postRemoteKeyDao(): PostRemoteKeyDao
}
