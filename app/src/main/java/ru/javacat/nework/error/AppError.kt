package ru.javacat.nework.error

//sealed class AppError(var code: String): RuntimeException(){
//    companion object {
//        fun from(e: Throwable): AppError = when (e) {
//            is AppError -> e
//            is SQLException -> DbError
//            is UnknownHostException -> NetworkError
//            is IOException -> NetworkError
//            else -> UnknownError
//        }
//    }
//
//}
//
//
//class ApiError(val status: Int, code: String): AppError(code)
//object NetworkError : AppError("error_network")
//object UnknownError: AppError("error_unknown")
//object DbError : AppError("error_db")

sealed class AppError: RuntimeException()

data class NetworkError(override val message: String) : AppError()
data class ApiError(val responseCode: Int, override val message: String): AppError()
data class UnknownError(override val message: String): AppError()