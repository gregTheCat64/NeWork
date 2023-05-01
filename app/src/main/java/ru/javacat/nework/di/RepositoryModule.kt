package ru.javacat.nework.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.javacat.nework.data.impl.EventRepositoryImpl
import ru.javacat.nework.data.impl.JobRepositoryImpl
import ru.javacat.nework.data.impl.PostRepositoryImpl
import ru.javacat.nework.data.impl.UserRepositoryImpl
import ru.javacat.nework.domain.repository.EventRepository
import ru.javacat.nework.domain.repository.JobRepository
import ru.javacat.nework.domain.repository.PostRepository
import ru.javacat.nework.domain.repository.UserRepository
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
interface RepositoryModule {

    @Singleton
    @Binds
    fun bindsPostRepository(impl: PostRepositoryImpl): PostRepository

    @Singleton
    @Binds
    fun bindsEventRepository(impl: EventRepositoryImpl): EventRepository

    @Singleton
    @Binds
    fun bindsUserRepository(impl: UserRepositoryImpl): UserRepository

    @Singleton
    @Binds
    fun bindsJobRepository(impl: JobRepositoryImpl): JobRepository
}