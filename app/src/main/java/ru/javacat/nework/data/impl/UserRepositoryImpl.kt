package ru.javacat.nework.data.impl

import android.util.Log
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.javacat.nework.data.api.UserApi
import ru.javacat.nework.data.auth.AppAuth
import ru.javacat.nework.data.dao.UserDao
import ru.javacat.nework.data.dto.MediaUpload
import ru.javacat.nework.data.entity.UserEntity
import ru.javacat.nework.data.entity.toModel
import ru.javacat.nework.data.mappers.toEntity
import ru.javacat.nework.data.mappers.toModel
import ru.javacat.nework.domain.model.User
import ru.javacat.nework.domain.repository.UserRepository
import ru.javacat.nework.error.ApiError
import ru.javacat.nework.error.NetworkError
import ru.javacat.nework.error.UnknownError
import java.io.IOException
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val userApi: UserApi,
    private val appAuth: AppAuth,

    ) : UserRepository {
    override val userData = userDao.getAll()
        .map(List<UserEntity>::toModel)
        .flowOn(Dispatchers.Default)


    override suspend fun getAll() {
        try {
            val response = userApi.getAll()
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            val result = body.map { it.toEntity() }
            userDao.insert(result)
        } catch (e: IOException) {
            e.printStackTrace()
            throw NetworkError

        } catch (e: Exception) {
            e.printStackTrace()
            throw UnknownError
        }
    }

    override suspend fun clearUserList() {
        try {
            val response = userApi.getAll()
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            val result = body.map { it.toEntity() }
            userDao.clearUserList(result)
        } catch (e: IOException) {
            e.printStackTrace()
            throw NetworkError

        } catch (e: Exception) {
            e.printStackTrace()
            throw UnknownError
        }
    }


    override suspend fun getById(id: Long): User? {
        try {
            val daoResult = userDao.getUserById(id)?.toModel()
            if (daoResult == null) {
                val response = userApi.getById(id)
                val body = response.body() ?: throw ApiError(response.code(), response.message())
                userDao.insert(body.toEntity())
            }
            return daoResult ?: userApi.getById(id).body()?.toModel()
        } catch (e: IOException) {
            e.printStackTrace()
            throw NetworkError
        } catch (e: Exception) {
            e.printStackTrace()
            throw UnknownError
        }
    }

    override suspend fun getUsersById(list: List<Long>): List<User>? {
        val userList = mutableListOf<User>()
        for (i in list){
            val u = userDao.getUserById(i)?.toModel()
            if (u != null) {
                userList.add(u)
            }
        }
        return userList.toList()
    }

    override suspend fun updateFavList(users: List<UserEntity>) {
        userDao.updateFavs(users)
    }

    override suspend fun addToFav(id: Long) {
        userDao.addToFav(id)
    }

    override suspend fun deleteFromFav(id: Long) {
       userDao.deleteFromFav(id)
    }

    override suspend fun registerUser(
        login: String,
        pass: String,
        name: String,
        upload: MediaUpload?
    ) {
        try {
            val userNameBody = name.toRequestBody("text/plain".toMediaTypeOrNull())
            val userPassBody = pass.toRequestBody("text/plain".toMediaTypeOrNull())
            val userLoginBody = login.toRequestBody("text/plain".toMediaTypeOrNull())
            val response = if (upload != null) {
                val avatar = MultipartBody.Part.createFormData(
                    "file", upload.file.name, upload.file.asRequestBody()
                )
                userApi.registerWithPhoto(userLoginBody, userPassBody, userNameBody, avatar)
            } else {
                userApi.registerUser(login, pass, name)
            }

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            val id = body.id
            val token = body.token
            if (token != null) {
                appAuth.setAuth(id, token)
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            println(e)
            throw UnknownError
        }
    }

    override suspend fun updateUser(login: String, pass: String) {
        try {
            val response = userApi.updateUser(login, pass)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            val id = body.id
            val token = body.token
            if (token != null) {
                appAuth.setAuth(id, token)
            }

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            println(e)
            throw UnknownError
        }
    }
}