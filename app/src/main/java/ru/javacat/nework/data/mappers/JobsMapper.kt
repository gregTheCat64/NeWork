package ru.javacat.nework.data.mappers

import ru.javacat.nework.data.dto.request.JobCreateRequest
import ru.javacat.nework.domain.model.JobModel
import ru.javacat.nework.util.asString

fun JobModel.toJobRequest() = JobCreateRequest(
    id = id,
    name = name,
    position = position,
    start = start.toString(),
    finish = finish?.toString(),
    link = link
)