package ru.javacat.nework.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import ru.javacat.nework.data.dto.request.JobCreateRequest
import ru.javacat.nework.data.dto.response.JobResponse

interface JobsApi {

    @GET("{user_id}/jobs/")
    suspend fun getJobsById(@Path("user_id") id: Long): Response<List<JobResponse>>

    @POST("my/jobs/")
    suspend fun createJob(@Body jobCreateRequest: JobCreateRequest): Response<JobResponse>

    @DELETE("my/jobs/{job_id}/")
    suspend fun removeById(@Path("job_id") id: Long)


}