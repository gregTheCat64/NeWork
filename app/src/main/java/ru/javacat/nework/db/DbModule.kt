package ru.javacat.nework.db

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.javacat.nework.dao.PostDao
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DbModule {

    @Singleton
    @Provides
    fun provideDb(
        @ApplicationContext
        context: Context
    ): AppDb = Room.databaseBuilder(context, AppDb::class.java, "app.db")
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    fun providePostDao(db: AppDb): PostDao = db.postDao()
}