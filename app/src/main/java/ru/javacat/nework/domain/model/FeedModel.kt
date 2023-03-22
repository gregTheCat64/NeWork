package ru.javacat.nework.domain.model


//data class FeedModel (
//    val posts: List<Post> = emptyList(),
//    val empty: Boolean = false,
//
//    )

data class FeedModelState(
    val loading: Boolean = false,
    val error: Boolean = false,
    val refreshing: Boolean = false,
    val idle: Boolean = false
)