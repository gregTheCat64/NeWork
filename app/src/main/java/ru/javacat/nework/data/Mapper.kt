package ru.javacat.nework.data

import ru.javacat.nework.data.dto.request.PostRequest
import ru.javacat.nework.data.dto.response.Attachment
import ru.javacat.nework.data.dto.response.Coordinates
import ru.javacat.nework.data.dto.response.PostResponse
import ru.javacat.nework.domain.model.AttachmentModel
import ru.javacat.nework.domain.model.AttachmentType
import ru.javacat.nework.domain.model.CoordinatesModel
import ru.javacat.nework.domain.model.PostModel
import ru.javacat.nework.util.toLocalDateTime

fun PostResponse.toModel() = PostModel(
    id = id,
    authorId = authorId,
    author = author,
    authorAvatar = authorAvatar,
    authorJob = authorJob,
    content = content,
    published = published,
    coords = coords.toModel(),
    link = link,
    likeOwnerIds = likeOwnerIds,
    mentionIds = mentionIds,
    mentionMe = mentionMe,
    likedByMe = likedByMe,
    attachment = attachment.toModel(),
    ownedByMe = ownedByMe,
    users = users

)

fun PostModel.toPostRequest() = PostRequest(
    id = id,
    content = content,
    coords = coords.toCoordinates(),
    link = link,
    attachment = attachment.toAttachment(),
    mentionIds = mentionIds
)

fun Coordinates?.toModel() = this?.let {
    CoordinatesModel(
        latitude = this.latitude.toDouble(),
        longitude = this.longitude.toDouble()
    )
}

fun CoordinatesModel?.toCoordinates() = this?.let {
    Coordinates(
        latitude = latitude.toString(),
        longitude = longitude.toString()
    )
}

fun Attachment?.toModel() = this?.let {
    AttachmentModel(
        url = this.url,
        type = AttachmentType.valueOf(this.type)
    )
}

fun AttachmentModel?.toAttachment() = this?.let {
    Attachment(
        url = url,
        type = type.name
    )
}

