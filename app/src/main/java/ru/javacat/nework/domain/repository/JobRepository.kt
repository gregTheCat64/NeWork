package ru.javacat.nework.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.javacat.nework.data.dto.request.JobCreateRequest
import ru.javacat.nework.domain.model.JobModel

interface JobRepository {
    val jobsData: Flow<List<JobModel>>
    suspend fun getJobsByUserId(id: Long): List<JobModel>?
    suspend fun updateJobsByUserId(id: Long): List<JobModel>?
    suspend fun create(job: JobCreateRequest)
    suspend fun removeById(id: Long)
}