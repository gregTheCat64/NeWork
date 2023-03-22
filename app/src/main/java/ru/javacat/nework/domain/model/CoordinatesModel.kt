package ru.javacat.nework.domain.model

class CoordinatesModel(
    val latitude: Double,
    val longitude: Double
)
{
    override fun toString(): String {
        return "lat: $latitude, long: $longitude"
    }
}