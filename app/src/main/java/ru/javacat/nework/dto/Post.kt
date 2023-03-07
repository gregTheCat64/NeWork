package ru.javacat.nework.dto

data class Post (
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String = "",
    val content: String,
    val published: String,
    val likedByMe: Boolean,
    val likes: Int = 0,
    var attachment: Attachment? = null,
    var savedOnServer:Boolean = true,
    val ownedByMe: Boolean = false

        )

data class Attachment(
    val url: String,
    val type: AttachmentType = AttachmentType.IMAGE
)

enum class AttachmentType {
    IMAGE
}