package ru.javacat.nework.dto

import java.util.Date

data class Post (
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String? = "",
    val content: String,
    val published: String,
    val likedByMe: Boolean,
    val likes: Int = 0, //убирать?
    var attachment: Attachment? = null,
    val ownedByMe: Boolean = false,
//    val mentionedMe: Boolean = false,
//    val users: List<User>? = null,
 //   val coords: Coordinates? = Coordinates(null, null)
        )

data class User(
    val name: String,
    val avatar: String?
)

data class Coordinates(
    val lat: Double?,
    val long: Double?
)

data class Attachment(
    val url: String,
    val type: AttachmentType = AttachmentType.IMAGE
)

enum class AttachmentType {
    IMAGE,
    VIDEO,
    AUDIO
}