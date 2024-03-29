package ru.javacat.nework.data.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.javacat.nework.data.entity.EventEntity
import ru.javacat.nework.data.entity.PostEntity

@Dao
interface EventDao {
    @Query("SELECT * FROM EventEntity ORDER BY id DESC")
    fun getAll(): Flow<List<EventEntity>>

    @Query("SELECT * FROM EventEntity ORDER BY id DESC")
    fun getPagingSource(): PagingSource<Int, EventEntity>

    @Query("SELECT * FROM EventEntity WHERE authorId = :userId ORDER BY id DESC")
    fun getWallPagingSource(userId: Long): PagingSource<Int, EventEntity>

    @Query("SELECT * FROM EventEntity WHERE id = :id")
    suspend fun getById(id: Long): EventEntity

    @Query("SELECT * FROM EventEntity WHERE authorId = :authorId ")
    suspend fun getByAuthorId(authorId: Long): List<EventEntity>


    @Query("SELECT COUNT(*) == 0 FROM EventEntity")
    suspend fun isEmpty(): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(events: List<EventEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: EventEntity)

    @Query("""
        UPDATE EventEntity SET 
        likeOwnerIds = :likeOwners,
        likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END
        WHERE  id = :id
        """)
    suspend fun likeById(id: Long, likeOwners: List<Long>?)

    @Query("""
        UPDATE EventEntity SET
        participantsIds = :participants,
        participatedByMe = 1
        WHERE id = :eventId
    """)
    suspend fun insertParticipant(eventId: Long, participants: List<Long>)

    @Query("""
        UPDATE EventEntity SET
        participantsIds = :participants,
        participatedByMe = 0
        WHERE id = :eventId
    """)
    suspend fun removeParticipant(eventId: Long, participants: List<Long>)

    @Query("DELETE FROM EventEntity WHERE id = :id")
    suspend fun removeById(id: Long)

    @Query("DELETE FROM EventEntity")
    suspend fun clear()
}