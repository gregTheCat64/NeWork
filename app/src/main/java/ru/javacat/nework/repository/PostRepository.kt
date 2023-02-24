package ru.javacat.nework.repository

import androidx.lifecycle.LiveData
import ru.javacat.nework.dto.Post

interface PostRepository {
    fun getAll(): LiveData<List<Post>>
    fun likeById(id: Long)
    fun save(post: Post)
    fun removeById(id: Long)
}