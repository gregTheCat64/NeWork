package ru.javacat.nework.domain.model

import java.time.LocalDateTime

data class JobModel(
    val userId: Long,
    val id: Long,
    val name: String,
    val position: String,
    val start:LocalDateTime,
    val finish: LocalDateTime?,
    val link: String?
)