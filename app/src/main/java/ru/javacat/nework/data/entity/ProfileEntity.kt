package ru.javacat.nework.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ProfileEntity (
    @PrimaryKey
    val id: Long,
    val favListIds: List<Long>
        )