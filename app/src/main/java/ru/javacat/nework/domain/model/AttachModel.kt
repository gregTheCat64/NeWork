package ru.javacat.nework.domain.model

import android.net.Uri

//data class PhotoModel(val uri: Uri? = null, val file: File? = null)
data class AttachModel(val uri: Uri? = null, val type: AttachmentType? = null)