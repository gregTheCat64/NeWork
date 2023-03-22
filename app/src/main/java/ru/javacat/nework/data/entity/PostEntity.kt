package ru.javacat.nework.data.entity

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.javacat.nework.domain.model.AttachmentModel
import ru.javacat.nework.domain.model.AttachmentType
import ru.javacat.nework.domain.model.CoordinatesModel
import ru.javacat.nework.domain.model.PostModel
import ru.javacat.nework.util.toLocalDateTime

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String?,
    val authorJob: String?,
    val content: String,
    val published: String,
    @Embedded
    val coords: CoordinatesEmbeddable?,
    val link: String?,
    //val likeOwnerIds: List<Int>,
    //val mentionIds: List<Int>,
    val mentionMe: Boolean,
    val likedByMe: Boolean,
    @Embedded
    val attachment: AttachmentEmbeddable?,
    val ownedByMe: Boolean,
    //val users: Map<Int, UserPreview>,
    ) {


    fun toDto() = PostModel(
        id, authorId, author, authorAvatar, authorJob, content, published.toString(),
        coords = coords?.toDto(),
        link, null, null, mentionMe, likedByMe,
        attachment = attachment?.toDto(),
        ownedByMe, null
    )

    companion object {
        fun fromDto(dto: PostModel) =
            PostEntity(
                dto.id,
                dto.authorId,
                dto.author,
                dto.authorAvatar,
                dto.authorJob,
                dto.content,
                dto.published.toString(),
                CoordinatesEmbeddable.fromDto(dto.coords),
                dto.link, dto.mentionMe,
                dto.likedByMe,
                AttachmentEmbeddable.fromDto(dto.attachment),
                dto.ownedByMe
            )
    }
}

data class AttachmentEmbeddable(
    var url: String,
    var type: AttachmentType,
) {
    fun toDto() = AttachmentModel(url, type)

    companion object {
        fun fromDto(dto: AttachmentModel?) = dto?.let {
            AttachmentEmbeddable(it.url, it.type)
        }
    }
}

data class CoordinatesEmbeddable(
    var lat: Double,
    var longitude: Double
) {
    fun toDto() = CoordinatesModel(lat, longitude)

    companion object {
        fun fromDto(dto: CoordinatesModel?) = dto?.let {
            CoordinatesEmbeddable(it.latitude, it.longitude)
        }
    }
}


fun List<PostEntity>.toDto(): List<PostModel> = map(PostEntity::toDto)
fun List<PostModel>.toEntity(): List<PostEntity> = map(PostEntity.Companion::fromDto)