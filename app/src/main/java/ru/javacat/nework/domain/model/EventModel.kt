package ru.javacat.nework.domain.model

import ru.javacat.nework.data.dto.response.UserPreview
import java.time.LocalDateTime

data class EventModel(
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String?,
    val authorJob: String?,
    val content: String,
    val datetime: LocalDateTime,
    val published: LocalDateTime,
    val coords: CoordinatesModel?,
    val type: EventType,
    val likeOwnerIds: List<Long>?,
    val likedByMe:Boolean,
    val speakerIds: List<Long>,
    val participantsIds: List<Long>,
    val participatedByMe: Boolean,
    val attachment: AttachmentModel?,
    val link: String?,
    val ownedByMe: Boolean,
    val users: Map<Long, UserPreview>
)