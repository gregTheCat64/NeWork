package ru.javacat.nework.domain.model


import ru.javacat.nework.data.dto.response.UserPreview
import java.time.LocalDateTime

data class PostModel (
        val id: Long,
        val authorId: Long,
        val author: String,
        val authorAvatar: String?,
        val authorJob: String?,
        val content: String,
        val published:String?,
        var coords: CoordinatesModel?,
        var link: String?,
        val likeOwnerIds: List<Long>?,
        var mentionIds: List<Long>?,
        val mentionMe: Boolean,
        val likedByMe: Boolean,
        var attachment: AttachmentModel?,
        val ownedByMe: Boolean,
        val users: Map<Int, UserPreview>?
        )