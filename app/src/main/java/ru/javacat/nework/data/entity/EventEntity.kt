package ru.javacat.nework.data.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.javacat.nework.data.dto.response.UserPreview
import ru.javacat.nework.domain.model.EventModel
import ru.javacat.nework.domain.model.EventType
import ru.javacat.nework.util.asString
import ru.javacat.nework.util.toLocalDateTime

@Entity
data class EventEntity (
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String?,
    val authorJob: String?,
    val content: String,
    val datetime: String,
    val published: String,
    @Embedded
    val coords: CoordinatesEmbeddable?,
    val typeOfEvent: String,
    val likeOwnerIds: List<Long>?,
    val likedByMe:Boolean,
    val speakerIds: List<Long>,
    val participantsIds: List<Long>,
    val participatedByMe: Boolean,
    @Embedded
    val attachment: AttachmentEmbeddable?,
    val link: String?,
    val ownedByMe: Boolean,
    val users: Map<Long, UserPreview>
        ){
    fun toDto() = EventModel(
        id,authorId,author,authorAvatar,authorJob,content,datetime.toLocalDateTime(),
        published.toLocalDateTime(), coords?.toDto(), EventType.valueOf(typeOfEvent),
        likeOwnerIds, likedByMe,speakerIds,participantsIds,participatedByMe,
        attachment?.toDto(),false, link,ownedByMe,users
    )

    companion object{
        fun fromDto(dto: EventModel) = EventEntity(
            dto.id,
            dto.authorId,
            dto.author,
            dto.authorAvatar,
            dto.authorJob,
            dto.content,
            dto.datetime.toString(),
            dto.published.toString(),
            CoordinatesEmbeddable.fromDto(dto.coords),
            dto.type.toString(),
            dto.likeOwnerIds,
            dto.likedByMe,
            dto.speakerIds,
            dto.participantsIds,
            dto.participatedByMe,
            AttachmentEmbeddable.fromDto(dto.attachment),
            dto.link,
            dto.ownedByMe,
            dto.users
        )
    }

}

fun List<EventEntity>.toDto(): List<EventModel> = map(EventEntity::toDto)
fun List<EventModel>.toEntity(): List<EventEntity> = map(EventEntity::fromDto)