package ru.javacat.nework.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.javacat.nework.data.dao.*
import ru.javacat.nework.data.entity.*
import ru.javacat.nework.util.Converters

@Database(entities = [
    PostEntity::class,
    PostRemoteKeyEntity::class,
    EventEntity::class,
    UserEntity::class,
    JobEntity::class
                     ], version = 1)
@TypeConverters(Converters::class)
abstract class AppDb : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun postRemoteKeyDao(): PostRemoteKeyDao
    abstract fun eventDao(): EventDao
    abstract fun userDao(): UserDao
    abstract fun jobDao(): JobsDao

}
