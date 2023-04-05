package ru.javacat.nework.data.mappers

import ru.javacat.nework.data.dto.response.EventResponse
import ru.javacat.nework.data.entity.EventEntity
import ru.javacat.nework.domain.model.EventModel
import ru.javacat.nework.domain.model.EventType
import ru.javacat.nework.util.toLocalDateTime

fun EventResponse.toEventModel() = EventModel(
    id = id,
    authorId = authorId,
    author = author,
    authorAvatar = authorAvatar,
    authorJob = authorJob,
    content = content,
    published = published.toLocalDateTime(),
    datetime = datetime.toLocalDateTime(),
    coords = coords.toModel(),
    type = EventType.valueOf(this.type),
    link = link,
    likeOwnerIds = likeOwnerIds,
    likedByMe = likedByMe,
    speakerIds = speakerIds,
    participantsIds = participantsIds,
    participatedByMe = participatedByMe,
    attachment = attachment.toModel(),
    ownedByMe = ownedByMe,
    users = users
)

fun EventResponse.toEventEntity() = EventEntity(
    id = id,
    authorId = authorId,
    author = author,
    authorAvatar = authorAvatar,
    authorJob = authorJob,
    content = content,
    published = published,
    datetime = datetime,
    coords = coords?.toCoordinatesEmbeddable(),
    typeOfEvent = type,
    link = link,
    likeOwnerIds = likeOwnerIds,
    likedByMe = likedByMe,
    speakerIds = speakerIds,
    participantsIds = participantsIds,
    participatedByMe = participatedByMe,
    attachment = attachment?.toAttachmentEmbeddable(),
    ownedByMe = ownedByMe,
    users = users
)