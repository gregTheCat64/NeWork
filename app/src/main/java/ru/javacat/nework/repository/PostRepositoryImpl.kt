package ru.javacat.nework.repository

import androidx.lifecycle.Transformations
import ru.javacat.nework.dao.PostDao
import ru.javacat.nework.dto.Post
import ru.javacat.nework.entity.PostEntity

class PostRepositoryImpl (
    private val dao: PostDao,
) : PostRepository {
    override fun getAll() = Transformations.map(dao.getAll()) { list ->
        list.map {
            it.toDto()
        }
    }

    override fun likeById(id: Long) {
        dao.likeById(id)
    }

    override fun save(post: Post) {
        dao.save(PostEntity.fromDto(post))
    }

    override fun removeById(id: Long) {
        dao.removeById(id)
    }
}