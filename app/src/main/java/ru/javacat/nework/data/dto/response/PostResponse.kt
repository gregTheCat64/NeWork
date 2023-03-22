package ru.javacat.nework.data.dto.response



data class PostResponse (
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String?,
    val authorJob: String?,
    val content: String,
    val published:String,
    val coords: Coordinates?,
    val link: String?,
    val likeOwnerIds: List<Long>,
    val mentionIds: List<Long>,
    val mentionMe: Boolean,
    val likedByMe: Boolean,
    val attachment: Attachment,
    val ownedByMe: Boolean,
    val users: Map<Int, UserPreview>
        )