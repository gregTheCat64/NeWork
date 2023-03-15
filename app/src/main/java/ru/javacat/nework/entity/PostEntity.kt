package ru.javacat.nework.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.javacat.nework.dto.*
import java.util.Date

@Entity
data class PostEntity (
        @PrimaryKey(autoGenerate = true)
        val id: Long,
        val authorId: Long,
        val author: String,
        val authorAvatar: String?,
        val content: String,
        val published: String,
        val likedByMe: Boolean,
        val likes: Int = 0,

        @Embedded
        val attachment: AttachmentEmbeddable?,
//        val mentionedMe: Boolean = false,
//        val users: List<User>? = null,
 //       val coords: Coordinates? = Coordinates(null, null),
        val ownedByMe: Boolean = false,

) {
        fun toDto() = Post(id, authorId, author, authorAvatar, content, published,  likedByMe, likes,
                attachment = attachment?.toDto(),
                //coords = coords
        )

        companion object {
                fun fromDto(dto: Post) =
                        PostEntity(
                                dto.id,
                                dto.authorId,
                                dto.author,
                                dto.authorAvatar,
                                dto.content,
                                dto.published,
                                dto.likedByMe,
                                dto.likes,
                                AttachmentEmbeddable.fromDto(dto.attachment),
                                //dto.coords,
                                dto.ownedByMe
                        )

        }
}

data class AttachmentEmbeddable(
        var url: String,
        var type: AttachmentType,
) {
        fun toDto() = Attachment(url, type)

        companion object {
                fun fromDto(dto: Attachment?) = dto?.let {
                        AttachmentEmbeddable(it.url, it.type)
                }
        }
}

fun List<PostEntity>.toDto(): List<Post> = map(PostEntity::toDto)
fun List<Post>.toEntity(): List<PostEntity> = map(PostEntity::fromDto)