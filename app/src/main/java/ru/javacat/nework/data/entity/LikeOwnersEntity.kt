package ru.javacat.nework.data.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Relation


@Entity(
    foreignKeys = [
        ForeignKey(
            entity = PostEntity::class,
            parentColumns = ["id"],
            childColumns = ["postId"],
            onDelete = ForeignKey.CASCADE
        )
                  ],
    primaryKeys = ["postId", "likeOwnerId"])
data class LikeOwnersEntity (
    val likeOwnerId: Long,
    val postId: Long,
        )

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = PostEntity::class,
            parentColumns = ["id"],
            childColumns = ["postId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    primaryKeys = ["postId", "mentionId"])
data class MentionEntity (
    val mentionId: Long,
    val postId: Long,
)


class PostWithLikeOwnersAndMentions(
    @Embedded
    val postEntity: PostEntity,
    @Relation(parentColumn = "id",
        entity = LikeOwnersEntity::class,
        entityColumn = "postId")
    val likeOwners: List<LikeOwnersEntity>,
    @Relation(parentColumn = "id",
        entity = MentionEntity::class,
        entityColumn = "postId")
    val mention: List<MentionEntity>
)

