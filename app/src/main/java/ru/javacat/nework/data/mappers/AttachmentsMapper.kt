package ru.javacat.nework.data.mappers

import ru.javacat.nework.data.dto.response.Attachment
import ru.javacat.nework.data.entity.AttachmentEmbeddable
import ru.javacat.nework.domain.model.AttachmentModel
import ru.javacat.nework.domain.model.AttachmentType

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

fun Attachment.toAttachmentEmbeddable() = AttachmentEmbeddable(
    url = url,
    type = AttachmentType.valueOf(this.type)
)