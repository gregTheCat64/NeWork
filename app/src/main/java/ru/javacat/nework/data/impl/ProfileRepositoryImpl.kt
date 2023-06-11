package ru.javacat.nework.data.impl

import ru.javacat.nework.data.auth.AppAuth
import ru.javacat.nework.data.dao.ProfileDao
import ru.javacat.nework.data.entity.ProfileEntity
import ru.javacat.nework.domain.repository.ProfileRepository
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val profileDao: ProfileDao,
    private val appAuth: AppAuth,
) : ProfileRepository {
    override suspend fun getFavListIds(profileId: Long): ProfileEntity {
        return profileDao.getFavListIds(profileId)

    }

    override suspend fun insert(profile: ProfileEntity) {
        profileDao.insert(profile)
    }

    override suspend fun updateUsers(profileId: Long, userIds: List<Long>) {
        profileDao.updateUsers(profileId, userIds)
    }
}