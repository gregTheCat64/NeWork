package ru.javacat.nework.data.dto.response

data class JobResponse (
    val id: Long,
    var userId: Long,
    val name: String,
    val position: String,
    val start: String,
    val finish: String?,
    val link: String?
        )