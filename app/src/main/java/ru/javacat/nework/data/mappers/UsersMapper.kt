package ru.javacat.nework.data.mappers

import ru.javacat.nework.data.dto.response.UserResponse
import ru.javacat.nework.data.entity.UserEntity
import ru.javacat.nework.domain.model.User

fun UserResponse.toModel() = User(
    id,login, name, avatar
)

fun UserResponse.toEntity(): UserEntity = UserEntity(
    id,login, name, avatar
)