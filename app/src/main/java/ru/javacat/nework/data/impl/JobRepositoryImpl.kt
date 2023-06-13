package ru.javacat.nework.data.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import ru.javacat.nework.data.api.JobsApi
import ru.javacat.nework.data.auth.AppAuth
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
    private val jobsApi: JobsApi,
    //private val appAuth: AppAuth,

    ) : JobRepository {
    override val jobsData: Flow<List<JobModel>> = jobDao.getAll()
        .map(List<JobEntity>::toModel)
        .flowOn(Dispatchers.Default)

    override suspend fun getJobsByUserId(id: Long): List<JobModel> {
        try {
            val response = jobsApi.getJobsById(id)
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            val apiResult = body.map {
                it.copy(userId = id)
            }.map { it.toEntity() }
            jobDao.insert(apiResult)
            val daoResult = jobDao.getByAuthorId(id).map { it.toModel() }
            return daoResult

        } catch (e: ApiError) {
            throw ApiError(e.responseCode, e.message)
        } catch (e: IOException) {
            throw NetworkError("error_network")
        }
        catch (e: Exception) {
            throw UnknownError("error_unknown")
        }
    }

    override suspend fun updateJobsByUserId(id: Long): List<JobModel>? {
        try {
            val result = jobsApi.getJobsById(id)
            result.body()?.let { it -> jobDao.insert(it.map { it.toEntity() }) }
            return result.body()?.map { it.toModel() }
        } catch (e: ApiError) {
            throw ApiError(e.responseCode, e.message)
        } catch (e: IOException) {
            throw NetworkError("error_network")
        }
        catch (e: Exception) {
            throw UnknownError("error_unknown")
        }
    }

    override suspend fun create(job: JobCreateRequest) {
        try {
            jobsApi.createJob(job)
        } catch (e: ApiError) {
            throw ApiError(e.responseCode, e.message)
        } catch (e: IOException) {
            throw NetworkError("error_network")
        }
        catch (e: Exception) {
            throw UnknownError("error_unknown")
        }
    }

    override suspend fun removeById(id: Long) {
        try {
            jobsApi.removeById(id)
            jobDao.removeById(id)
        } catch (e: ApiError) {
            throw ApiError(e.responseCode, e.message)
        } catch (e: IOException) {
            throw NetworkError("error_network")
        }
        catch (e: Exception) {
            throw UnknownError("error_unknown")
        }
    }
}