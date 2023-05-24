package ru.javacat.nework.domain.model

import androidx.core.net.toUri

data class AttachmentModel (
    val url: String,
    val type: AttachmentType,
        )

enum class AttachmentType {
    IMAGE, VIDEO, AUDIO
}

fun AttachmentModel.toAttachModel() = AttachModel(
    uri = url.toUri(),
    type = type
)