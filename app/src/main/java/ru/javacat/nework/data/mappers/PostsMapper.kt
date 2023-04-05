package ru.javacat.nework.data.mappers

import ru.javacat.nework.data.dto.request.PostRequest
import ru.javacat.nework.data.dto.response.Attachment
import ru.javacat.nework.data.dto.response.Coordinates
import ru.javacat.nework.data.dto.response.EventResponse
import ru.javacat.nework.data.dto.response.PostResponse
import ru.javacat.nework.data.entity.*
import ru.javacat.nework.domain.model.*
import ru.javacat.nework.util.toLocalDateTime

fun PostResponse.toModel() = PostModel(
    id = id,
    authorId = authorId,
    author = author,
    authorAvatar = authorAvatar,
    authorJob = authorJob,
    content = content,
    published = published.toLocalDateTime(),
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

fun PostResponse.toEntity(): PostEntity = PostEntity(
    id, authorId,author,authorAvatar,authorJob,content,published,
    coords = coords?.toCoordinatesEmbeddable(),link,likeOwnerIds,mentionIds,mentionMe,likedByMe,
    attachment = attachment?.toAttachmentEmbeddable(), ownedByMe, users
)



fun PostModel.toPostRequest() = PostRequest(
    id = id,
    content = content,
    coords = coords.toCoordinates(),
    link = link,
    attachment = attachment.toAttachment(),
    mentionIds = mentionIds
)






