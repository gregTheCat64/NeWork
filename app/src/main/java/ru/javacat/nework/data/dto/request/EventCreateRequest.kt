package ru.javacat.nework.data.dto.request

import ru.javacat.nework.data.dto.response.Attachment
import ru.javacat.nework.data.dto.response.Coordinates

data class EventCreateRequest (
    val id: Long,
    val content: String,
    val dateTime: String,
    val coords: Coordinates?,
    val type: String,
    val attachment: Attachment?,
    val link: String?,
    val speakerIds: List<Long>
        )