package ru.javacat.nework.repository

import androidx.lifecycle.Transformations
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import ru.javacat.nework.dao.PostDao
import ru.javacat.nework.dto.Post
import ru.javacat.nework.entity.PostEntity
import java.io.IOException
import java.util.concurrent.TimeUnit

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

class PostRepositoryImpl: PostRepository {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()
    private val typeToken = object : TypeToken<List<Post>>() {}

    companion object {
        private const val BASE_URL = "http://10.0.2.2:9999"
        private val jsonType = "application/json".toMediaType()
    }

    override fun getAll(): List<Post> {
        val request: Request = Request.Builder()
            .url("${BASE_URL}/api/slow/posts")
            .build()

        return client.newCall(request)
            .execute()
            .let { it.body?.string() ?: throw RuntimeException("body is null") }
            .let {
                gson.fromJson(it, typeToken.type)
            }
    }

    override fun getAllAsync(callback: PostRepository.GetAllCallback) {
        val request: Request = Request.Builder()
            .url("${BASE_URL}/api/slow/posts")
            .build()

        client.newCall(request)
            .enqueue(object : Callback{
                override fun onFailure(call: Call, e: IOException) {
                    callback.onError(e)
                }
                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.string() ?: throw RuntimeException("body is null")
                    try {
                        callback.onSuccess(gson.fromJson(body, typeToken.type))
                    } catch (e: Exception){
                        callback.onError(e)
                    }
                }
            })
    }

    override fun likeById(id: Long) {
        val posts = getAll()
        var request: Request
        var currentPost:Post = Post(0,"","","","",false,0)
        for (p in posts){
            if (p.id == id) {currentPost = p}
        }
        if (currentPost.likedByMe == false){
            val request: Request = Request.Builder()
                .post(gson.toJson(currentPost).toRequestBody(jsonType))
                .url("${BASE_URL}/api/posts/$id/likes")
                .build()
            client.newCall(request)
                .execute()
                .close()
        } else {
            val request: Request = Request.Builder()
                .delete(gson.toJson(currentPost).toRequestBody(jsonType))
                .url("${BASE_URL}/api/posts/$id/likes")
                .build()
            client.newCall(request)
                .execute()
                .close()
        }

    }

    override fun save(post: Post) {
        val request: Request = Request.Builder()
            .post(gson.toJson(post).toRequestBody(jsonType))
            .url("${BASE_URL}/api/slow/posts")
            .build()

        client.newCall(request)
            .execute()
            .close()
    }

    override fun removeById(id: Long) {
        val request: Request = Request.Builder()
            .delete()
            .url("${BASE_URL}/api/slow/posts/$id")
            .build()

        client.newCall(request)
            .execute()
            .close()
    }
}