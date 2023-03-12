package ru.javacat.nework.repository


import androidx.paging.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import okhttp3.*
import okhttp3.RequestBody.Companion.asRequestBody
import ru.javacat.nework.api.PostsApiService
import ru.javacat.nework.auth.AppAuth
import ru.javacat.nework.dao.PostDao
import ru.javacat.nework.dao.PostRemoteKeyDao
import ru.javacat.nework.db.AppDb
import ru.javacat.nework.dto.*
import ru.javacat.nework.entity.PostEntity
import ru.javacat.nework.entity.toEntity
import ru.javacat.nework.error.*
import java.io.IOException
import javax.inject.Inject


class PostRepositoryImpl @Inject constructor(
    private val postDao: PostDao,
    private val apiService: PostsApiService,
    postRemoteKeyDao: PostRemoteKeyDao,
    private val appAuth: AppAuth,
    appDb: AppDb

) : PostRepository {
    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<Post>> = Pager(
        config = PagingConfig(pageSize = 5),
        pagingSourceFactory = { postDao.getPagingSource() },
        remoteMediator = PostRemoteMediator(
            apiService,
            postDao,
            postRemoteKeyDao,
            appDb
        )
    ).flow
        .map { it.map(PostEntity::toDto) }


    override suspend fun getAll() {
        try {
            val response = apiService.getAll()
            val body = response.body() ?: throw ApiError(response.code(), response.message())
//            for (i in body) {
//                i.savedOnServer = true
//            }
            postDao.insert(body.toEntity())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }

    }

    override fun getNewerCount(id: Long): Flow<Int> = flow {
        while (true) {
            delay(10_000L)
            val response = apiService.getNewer(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(body.toEntity())
            emit(body.size)
        }
    }
        .catch { e -> throw AppError.from(e) }
        .flowOn(Dispatchers.Default)

    override suspend fun save(post: Post, upload: MediaUpload?) {
        //post.savedOnServer = false
        try {
            val postWithAttachment = upload?.let {
                upload(it)
            }?.let {
                post.copy(attachment = Attachment(it.id, AttachmentType.IMAGE))
            }
            if (postWithAttachment!=null) {postDao.insert(PostEntity.fromDto(postWithAttachment))}
            val response = apiService.save(postWithAttachment?: post)
            //val body = response.body() ?: throw ApiError(response.code(), response.message())
            //body.savedOnServer = true
            //postDao.insert(PostEntity.fromDto(body))
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

            val response = apiService.upload(media)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw ru.javacat.nework.error.UnknownError
        }
    }

    override suspend fun removeById(id: Long) {
        try {
            postDao.removeById(id)
            apiService.removeById(id)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun likeById(id: Long) {
        //врем.решение
        val posts = apiService.getAll().body()
        var currentPost: Post = Post(0, 0, "", "", "","",  false, 0)
        if (!posts.isNullOrEmpty()) {
            for (p in posts) {
                if (p.id == id) {
                    currentPost = p
                }
            }
            postDao.likeById(id)
            if (!currentPost.likedByMe) {
                apiService.likeById(id)
            } else {
                apiService.dislikeById(id)
            }
        }
    }

    override suspend fun updateUser(login: String, pass: String) {
        try {
            val response = apiService.updateUser(login, pass)
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

    override suspend fun registerUser(login: String, pass: String, name: String) {
        try {
            val response = apiService.registerUser(login, pass, name)
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