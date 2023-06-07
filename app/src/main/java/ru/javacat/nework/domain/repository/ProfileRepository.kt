package ru.javacat.nework.domain.repository

import ru.javacat.nework.data.entity.ProfileEntity

interface ProfileRepository {
    suspend fun getFavListIds(profileId: Long): ProfileEntity?

    suspend fun insert(profile: ProfileEntity)

    suspend fun updateUsers(profileId: Long, userIds: List<Long>)
}