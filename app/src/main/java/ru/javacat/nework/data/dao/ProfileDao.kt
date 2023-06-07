package ru.javacat.nework.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.javacat.nework.data.entity.ProfileEntity

@Dao
interface ProfileDao {
    @Query("SELECT * FROM ProfileEntity WHERE id = :profileId")
    suspend fun getFavListIds(profileId: Long): ProfileEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: ProfileEntity)


    @Query("""
        UPDATE ProfileEntity SET
        favListIds = :userIds
        WHERE id = :profileId
    """)
    suspend fun updateUsers(profileId: Long, userIds: List<Long>)
}