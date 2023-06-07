package ru.javacat.nework.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.javacat.nework.data.entity.EventEntity
import ru.javacat.nework.data.entity.UserEntity

@Dao
interface UserDao {
    @Query("SELECT * FROM UserEntity ORDER BY id ASC")
    fun getAll(): Flow<List<UserEntity>>

    @Query("SELECT * FROM UserEntity WHERE id = :id")
    suspend fun getUserById(id: Long): UserEntity?

    @Query("""
        UPDATE UserEntity SET
        favoured = 1
        WHERE id = :id
    """)
    suspend fun addToFav(id: Long)

    @Query("""
        UPDATE UserEntity SET
        favoured = 0
        WHERE id = :id
    """)
    suspend fun deleteFromFav(id: Long)


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(users: List<UserEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(user: UserEntity)
}