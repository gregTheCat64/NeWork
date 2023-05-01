package ru.javacat.nework.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.javacat.nework.domain.model.User

@Entity
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val login: String,
    val name: String,
    val avatar: String?
){
    fun toModel() = User(id,login,name, avatar)

    companion object{
        fun fromModel(model: User) = UserEntity(model.id,model.login,model.name,model.avatar)
    }
}

fun List<UserEntity>.toModel(): List<User> = map(UserEntity::toModel)
fun List<User>.toEntity(): List<UserEntity> = map(UserEntity::fromModel)