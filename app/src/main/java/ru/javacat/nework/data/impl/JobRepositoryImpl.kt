package ru.javacat.nework.data.impl

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import ru.javacat.nework.data.api.JobsApi
import ru.javacat.nework.data.dao.JobsDao
import ru.javacat.nework.data.dto.request.JobCreateRequest
import ru.javacat.nework.data.entity.JobEntity
import ru.javacat.nework.data.entity.toEntity
import ru.javacat.nework.data.entity.toModel
import ru.javacat.nework.domain.model.JobModel
import ru.javacat.nework.domain.repository.JobRepository
import ru.javacat.nework.error.ApiError
import ru.javacat.nework.error.NetworkError
import ru.javacat.nework.error.UnknownError
import java.io.IOException
import javax.inject.Inject

class JobRepositoryImpl @Inject constructor(
    private val jobDao: JobsDao,
    private val jobsApi: JobsApi

): JobRepository {
    override val jobsData: Flow<List<JobModel>> = jobDao.getAll()
        .map ( List<JobEntity>::toModel )
        .flowOn(Dispatchers.Default)

    override suspend fun getJobsByUserId(id: Long):List<JobModel> {
        try {
            val daoResult = jobDao.getByAuthorId(id)
            Log.i("DAORES","$daoResult")
            if (daoResult.isEmpty()){
                val response = jobsApi.getJobsById(id)
                val body = response.body() ?: throw ApiError(response.code(), response.message())
                val result = body.map {
                    it.copy(userId = id)
                }.map { it.toEntity() }
                jobDao.insert(result)
            }
            return daoResult.toModel()

        } catch (e: IOException) {
            e.printStackTrace()
            throw NetworkError

        } catch (e: Exception) {
            e.printStackTrace()
            throw UnknownError
        }
    }

    override suspend fun create(job: JobCreateRequest) {
        try {
            jobsApi.createJob(job)
        } catch (e: Exception) {
            throw ru.javacat.nework.error.UnknownError
        }
    }

    override suspend fun removeById(id: Long) {
       try {
           jobsApi.removeById(id)
           jobDao.removeById(id)
       } catch (e: IOException) {
           throw NetworkError
       } catch (e: Exception) {
           throw UnknownError
       }
    }
}