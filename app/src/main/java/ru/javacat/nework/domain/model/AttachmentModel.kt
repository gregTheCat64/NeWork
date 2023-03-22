package ru.javacat.nework.domain.model

data class AttachmentModel (
    val url: String,
    val type: AttachmentType,
        )

enum class AttachmentType {
    IMAGE, VIDEO, AUDIO
}