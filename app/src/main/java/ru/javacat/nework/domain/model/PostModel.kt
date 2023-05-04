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
    val published:LocalDateTime?,
    var coords: CoordinatesModel?,
    var link: String?,
    var likeOwnerIds: List<Long>?,
    var mentionIds: List<Long>,
    val mentionMe: Boolean,
    val likedByMe: Boolean,
    var attachment: AttachmentModel?,
    var playBtnPressed: Boolean = false,
    val ownedByMe: Boolean,
    val users: Map<Long, UserPreview>
        )