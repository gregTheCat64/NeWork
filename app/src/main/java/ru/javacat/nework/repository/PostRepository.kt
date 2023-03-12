package ru.javacat.nework.repository


import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.javacat.nework.dto.Media
import ru.javacat.nework.dto.MediaUpload
import ru.javacat.nework.dto.Post


interface PostRepository {
    val data: Flow<PagingData<Post>>
    suspend fun getAll()
    fun getNewerCount(id: Long): Flow<Int>
    suspend fun likeById(id: Long)
    suspend fun save(post: Post, upload: MediaUpload?)
    suspend fun removeById(id: Long)
    suspend fun upload(upload: MediaUpload): Media
    suspend fun updateUser(login: String, pass: String)
    suspend fun registerUser(login: String, pass: String, name: String)
    }