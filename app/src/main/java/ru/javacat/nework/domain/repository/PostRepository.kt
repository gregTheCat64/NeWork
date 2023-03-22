package ru.javacat.nework.domain.repository


import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.javacat.nework.data.dto.Media
import ru.javacat.nework.data.dto.MediaUpload
import ru.javacat.nework.data.dto.request.PostRequest
import ru.javacat.nework.domain.model.PostModel


interface PostRepository {
    val data: Flow<PagingData<PostModel>>
    suspend fun getAll()
    fun getNewerCount(id: Long): Flow<Int>
    suspend fun likeById(id: Long)
    suspend fun save(post: PostRequest, upload: MediaUpload?)
    suspend fun removeById(id: Long)
    suspend fun upload(upload: MediaUpload): Media
    suspend fun updateUser(login: String, pass: String)
    suspend fun registerUser(login: String, pass: String, name: String, upload: MediaUpload?)
    }