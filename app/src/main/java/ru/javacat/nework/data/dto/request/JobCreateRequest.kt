package ru.javacat.nework.data.dto.request

data class JobCreateRequest (
    val id: Long,
    val name: String,
    val position: String,
    val start: String,
    val finish: String?,
    val link: String?
        )