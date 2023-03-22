package ru.javacat.nework.data.dto.request



import ru.javacat.nework.data.dto.response.Attachment
import ru.javacat.nework.data.dto.response.Coordinates


data class PostRequest(
    val id: Long,
    val content: String,
    val coords: Coordinates?,
    val link: String?,
    val attachment: Attachment?,
    val mentionIds: List<Long>?
)