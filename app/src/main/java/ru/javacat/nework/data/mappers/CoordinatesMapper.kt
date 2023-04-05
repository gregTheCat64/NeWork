package ru.javacat.nework.data.mappers

import ru.javacat.nework.data.dto.response.Coordinates
import ru.javacat.nework.data.entity.CoordinatesEmbeddable
import ru.javacat.nework.domain.model.CoordinatesModel

fun Coordinates?.toModel() = this?.let {
    CoordinatesModel(
        latitude = this.latitude.toDouble(),
        longitude = this.longitude.toDouble()
    )
}

fun Coordinates.toCoordinatesEmbeddable() = CoordinatesEmbeddable(
    lat = this.latitude.toDouble(),
    longitude = this.longitude.toDouble()
)

fun CoordinatesModel?.toCoordinates() = this?.let {
    Coordinates(
        latitude = latitude.toString(),
        longitude = longitude.toString()
    )
}