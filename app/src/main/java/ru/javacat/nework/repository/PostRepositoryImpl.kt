package ru.javacat.nework.repository


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import okhttp3.*
import okhttp3.RequestBody.Companion.asRequestBody
import ru.javacat.nework.api.PostsApi
import ru.javacat.nework.dao.PostDao
import ru.javacat.nework.dto.*
import ru.javacat.nework.entity.PostEntity
import ru.javacat.nework.entity.toDto
import ru.javacat.nework.entity.toEntity
import ru.javacat.nework.error.*
import java.io.IOException

//class PostRepositoryImpl (
//    private val dao: PostDao,
//) : PostRepository {
//    override fun getAll() = Transformations.map(dao.getAll()) { list ->
//        list.map {
//            it.toDto()
//        }
//    }
//
//    override fun likeById(id: Long) {
//        dao.likeById(id)
//    }
//
//    override fun save(post: Post) {
//        dao.save(PostEntity.fromDto(post))
//    }
//
//    override fun removeById(id: Long) {
//        dao.removeById(id)
//    }
//}

class PostRepositoryImpl(private val postDao: PostDao) : PostRepository {
    override val data = postDao.getAll()
        .map (List<PostEntity>::toDto)
        .flowOn(Dispatchers.Default)


    override suspend fun getAll() {
        try {
            val response = PostsApi.retrofitService.getAll()
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            for (i in body) {
                i.savedOnServer = true
            }
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
            val response = PostsApi.retrofitService.getNewer(id)
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

    override suspend fun save(post: Post) {
        post.savedOnServer = false
        try {
            val response = PostsApi.retrofitService.save(post)
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            body.savedOnServer = true
            postDao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun saveWithAttachment(post: Post, upload: MediaUpload) {
        try {
            val media = uploadFile(upload)
            // TODO: add support for other types
            val postWithAttachment = post.copy(attachment = Attachment(media.id, AttachmentType.IMAGE))
            save(postWithAttachment)
        } catch (e: AppError) {
            throw e
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    private suspend fun uploadFile(upload: MediaUpload): Media {
        try {
            val media = MultipartBody.Part.createFormData(
                "file", upload.file.name, upload.file.asRequestBody()
            )

            val response = PostsApi.retrofitService.upload(media)
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
            PostsApi.retrofitService.removeById(id)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun likeById(id: Long) {
        //врем.решение
        val posts = PostsApi.retrofitService.getAll().body()
        var currentPost:Post = Post(0,"","","","",false,0)
        if (!posts.isNullOrEmpty()){
            for (p in posts){
                if (p.id == id) {currentPost = p}
            }
            postDao.likeById(id)
            if (!currentPost.likedByMe){
                PostsApi.retrofitService.likeById(id)
            } else {
               PostsApi.retrofitService.dislikeById(id)
            }
        }
    }
}