package ru.javacat.nework.data.impl


import androidx.paging.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.javacat.nework.data.AppDb
import ru.javacat.nework.data.api.PostsApi
import ru.javacat.nework.data.auth.AppAuth
import ru.javacat.nework.data.dao.PostDao
import ru.javacat.nework.data.dao.PostRemoteKeyDao
import ru.javacat.nework.data.dto.Media
import ru.javacat.nework.data.dto.MediaUpload
import ru.javacat.nework.data.dto.request.PostRequest
import ru.javacat.nework.data.dto.response.Attachment
import ru.javacat.nework.data.entity.PostEntity
import ru.javacat.nework.data.mappers.toEntity
import ru.javacat.nework.domain.model.AttachmentType
import ru.javacat.nework.domain.model.PostModel
import ru.javacat.nework.domain.repository.PostRepository
import ru.javacat.nework.error.ApiError
import ru.javacat.nework.error.NetworkError
import ru.javacat.nework.error.UnknownError
import java.io.IOException
import javax.inject.Inject


class PostRepositoryImpl @Inject constructor(
    private val postDao: PostDao,
    private val postsApi: PostsApi,
    private val postRemoteKeyDao: PostRemoteKeyDao,
    private val appAuth: AppAuth,
    private val appDb: AppDb
) : PostRepository {

    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<PostModel>> = Pager(
        config = PagingConfig(pageSize = 5),
        pagingSourceFactory = { postDao.getPagingSource() },
        remoteMediator = PostRemoteMediator(
            postsApi,
            postDao,
            postRemoteKeyDao,
            appDb
        )
    ).flow
        .map { it.map(PostEntity::toDto) }


    override suspend fun getAll() {
        try {
            val response = postsApi.getAll()
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(body.map { it.toEntity() })
        } catch (e: ApiError) {
            throw ApiError(e.responseCode, e.message)
        } catch (e: IOException) {
            throw NetworkError("error_network")
        }
        catch (e: Exception) {
            throw UnknownError("error_unknown")
        }
    }

    override suspend fun getLatest(count: Int) {
        try {
            val response = postsApi.getLatest(count)
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(body.map { it.toEntity() })
        }
        catch (e: ApiError) {
            throw ApiError(e.responseCode, e.message)
        } catch (e: IOException) {
            throw NetworkError("error_network")
        }
        catch (e: Exception) {
            throw UnknownError("error_unknown")
        }
    }


    override fun getNewerCount(id: Long): Flow<Int> = flow {
        while (true) {
            delay(20_000L)
            val response = postsApi.getNewer(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(body.map { it.toEntity() })
            emit(body.size)
        }
    }
        .catch { e -> throw UnknownError(e.message) }
        .flowOn(Dispatchers.Default)


    override suspend fun save(post: PostRequest) {
        postsApi.save(post)

    }

    override suspend fun create(post: PostRequest, upload: MediaUpload?, type: AttachmentType?) {
        try {
            val postWithAttachment = upload?.let {
                upload(it)
            }?.let {
                post.copy(attachment = type?.name?.let { type -> Attachment(it.url, type) })
            }
            postsApi.save(postWithAttachment ?: post)
        } catch (e: ApiError) {
            throw ApiError(e.responseCode, e.message)
        } catch (e: IOException) {
            throw NetworkError("error_network")
        }
        catch (e: Exception) {
            throw UnknownError("error_unknown")
        }
    }

    override suspend fun upload(upload: MediaUpload): Media {
        try {
            val media = MultipartBody.Part.createFormData(
                "file", upload.file.name, upload.file.asRequestBody()
            )
            val response = postsApi.upload(media)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: ApiError) {
            throw ApiError(e.responseCode, e.message)
        } catch (e: IOException) {
            throw NetworkError("error_network")
        }
        catch (e: Exception) {
            throw UnknownError("error_unknown")
        }
    }

    override suspend fun removeById(id: Long) {
        try {
            postDao.removeById(id)
            postsApi.removeById(id)
        } catch (e: ApiError) {
            throw ApiError(e.responseCode, e.message)
        } catch (e: IOException) {
            throw NetworkError("error_network")
        }
        catch (e: Exception) {
            throw UnknownError("error_unknown")
        }
    }

    override suspend fun likeById(id: Long) {
        val authorId = appAuth.authStateFlow.value.id
        val currentPost = postDao.getById(id)
        var likeOwnersIdList = currentPost.likeOwnerIds
        try {
            if (currentPost.likedByMe) {
                likeOwnersIdList = likeOwnersIdList?.minusElement(authorId)
                postDao.likeById(id, likeOwnersIdList)
                postsApi.dislikeById(id)
            } else {
                likeOwnersIdList = likeOwnersIdList?.plusElement(authorId)
                postDao.likeById(id, likeOwnersIdList)
                postsApi.likeById(id)
            }
        } catch (e: ApiError) {
            throw ApiError(e.responseCode, e.message)
        } catch (e: IOException) {
            throw NetworkError("error_network")
        }
        catch (e: Exception) {
            throw UnknownError("error_unknown")
        }

    }

}