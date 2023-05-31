package ru.javacat.nework.domain.repository


import androidx.lifecycle.LiveData
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.javacat.nework.data.dto.Media
import ru.javacat.nework.data.dto.MediaUpload
import ru.javacat.nework.data.dto.request.PostRequest
import ru.javacat.nework.domain.model.AttachmentType
import ru.javacat.nework.domain.model.PostModel
import ru.javacat.nework.domain.model.User


interface PostRepository {
    val data: Flow<PagingData<PostModel>>
    suspend fun getAll()

    suspend fun getLatest(count: Int)
    suspend fun getPostsByAuthorId(authorId: Long):List<PostModel>?
    suspend fun updatePostsByAuthorId(authorId: Long):List<PostModel>?
    fun getNewerCount(id: Long): Flow<Int>
    suspend fun likeById(id: Long)
    suspend fun save(post: PostRequest)

    suspend fun create(post: PostRequest, upload: MediaUpload?, type: AttachmentType?)
    suspend fun removeById(id: Long)
    suspend fun upload(upload: MediaUpload): Media
    }