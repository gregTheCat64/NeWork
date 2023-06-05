package ru.javacat.nework.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.javacat.nework.data.dto.MediaUpload
import ru.javacat.nework.domain.model.User
import ru.javacat.nework.ui.viewmodels.UserViewModel

interface UserRepository {
    val userData: Flow<List<User>>
    suspend fun getAll()
    suspend fun getById(id: Long): User?
    suspend fun getUsersById(list: List<Long>): List<User>?
    suspend fun updateUser(login: String, pass: String)
    suspend fun registerUser(login: String, pass: String, name: String, upload: MediaUpload?)
}