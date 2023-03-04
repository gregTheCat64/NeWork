package ru.javacat.nework.repository


import kotlinx.coroutines.flow.Flow
import ru.javacat.nework.dto.MediaUpload
import ru.javacat.nework.dto.Post


interface PostRepository {
    val data: Flow<List<Post>>
    suspend fun getAll()
    fun getNewerCount(id: Long): Flow<Int>
    suspend fun likeById(id: Long)
    suspend fun save(post: Post)
    suspend fun saveWithAttachment(post: Post, upload: MediaUpload)
    suspend fun removeById(id: Long)
    }