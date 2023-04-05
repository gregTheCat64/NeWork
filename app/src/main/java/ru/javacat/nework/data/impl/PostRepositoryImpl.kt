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
import ru.javacat.nework.domain.repository.PostRemoteMediator
import ru.javacat.nework.domain.repository.PostRepository
import ru.javacat.nework.error.ApiError
import ru.javacat.nework.error.AppError
import ru.javacat.nework.error.NetworkError
import ru.javacat.nework.error.UnknownError
import java.io.IOException
import javax.inject.Inject


class PostRepositoryImpl @Inject constructor(
    private val postDao: PostDao,
    private val postsApi: PostsApi,
    postRemoteKeyDao: PostRemoteKeyDao,
    private val appAuth: AppAuth,
    appDb: AppDb

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
//            for (i in body) {
//                i.savedOnServer = true
//            }
            postDao.insert(body.map { it.toEntity()})
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }

    }

    override fun getNewerCount(id: Long): Flow<Int> = flow {
        while (true) {
            delay(10_000L)
            val response = postsApi.getNewer(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(body.map { it.toEntity() })
            emit(body.size)
        }
    }
        .catch { e -> throw AppError.from(e) }
        .flowOn(Dispatchers.Default)


    override suspend fun save(post: PostRequest, upload: MediaUpload?) {
        try {
            val postWithAttachment = upload?.let {
                upload(it)
            }?.let {
                post.copy(attachment =  Attachment(it.url, AttachmentType.IMAGE.name))
            }
            postsApi.save(postWithAttachment?: post)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun upload(upload: MediaUpload): Media {
        try {
            val media = MultipartBody.Part.createFormData(
                "file", upload.file.name, upload.file.asRequestBody()
            )
            println("UPLOADFILE: ${upload.file}")

            val response = postsApi.upload(media)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            println("RESPONSE: ${response.body()}")
            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun removeById(id: Long) {
        try {
            postDao.removeById(id)
            postsApi.removeById(id)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun likeById(id: Long) {
        val authorId = appAuth.authStateFlow.value.id
        val  currentPost = postDao.getById(id)
        var likeOwners = currentPost.likeOwnerIds
        if (currentPost.likedByMe == true) {
            likeOwners = likeOwners?.minusElement(authorId)
            //currentPost.likedByMe = false
            //postDao.insert(currentPost)
            postDao.likeById(id, likeOwners)
            postsApi.dislikeById(id)
        } else {
            likeOwners = likeOwners?.plusElement(authorId)
            //currentPost.likedByMe = true
            //postDao.insert(currentPost)
            postDao.likeById(id, likeOwners)
            postsApi.likeById(id)
        }
        }

    override suspend fun registerUser(login: String, pass: String, name: String, upload: MediaUpload?) {
        try {
            val response = if (upload != null){
                val avatar = MultipartBody.Part.createFormData(
                    "file", upload.file.name, upload.file.asRequestBody()
                )
                postsApi.registerWithPhoto(login, pass, name, avatar)
            } else {
                postsApi.registerUser(login, pass, name)
            }

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            val id = body.id
            val token = body.token
            if (token != null) {
                appAuth.setAuth(id, token)
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            println(e)
            throw UnknownError

        }
    }

    override suspend fun updateUser(login: String, pass: String) {
        try {
            val response = postsApi.updateUser(login, pass)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            val id = body.id
            val token = body.token
            if (token != null) {
                appAuth.setAuth(id, token)
            }

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            println(e)
            throw UnknownError
        }
    }
}