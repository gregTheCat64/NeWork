package ru.javacat.nework.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.javacat.nework.data.dto.response.JobResponse
import ru.javacat.nework.domain.model.JobModel
import ru.javacat.nework.util.asString
import ru.javacat.nework.util.toLocalDateTime

@Entity
data class JobEntity (
   @PrimaryKey(autoGenerate = true)
    val id: Long,
    val userId: Long,
    val name: String,
    val position: String,
    val start: String,
    val finish: String?,
    val link: String?
        ){
    fun toModel() = JobModel(
        userId,id,name,position,start.toLocalDateTime(),finish?.toLocalDateTime(),link
    )

    companion object {
        fun fromModel(model: JobModel) =
            JobEntity(
                model.id,
                model.userId,
                model.name,
                model.position,
                model.start.asString(),
                model.finish?.asString(),
                model.link
            )
    }
}

fun List<JobEntity>.toModel(): List<JobModel> = map ( JobEntity::toModel )
fun List<JobModel>.toEntity(): List<JobEntity> = map(JobEntity.Companion::fromModel)

fun JobResponse.toEntity(): JobEntity = JobEntity(
    id, userId, name, position, start, finish, link
)