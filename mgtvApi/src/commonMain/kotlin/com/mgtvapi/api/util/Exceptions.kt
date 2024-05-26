package com.mgtvapi.api.util

object Exceptions {
    const val UNKNOWN_ERROR = "Unknown error"
    const val LOGIN_ERROR = "Login error"
    const val NETWORK_ERROR = "Network error"

    class LoginException(message: String? = null) : Exception(message ?: LOGIN_ERROR)

    class NetworkException(message: String? = null) : Exception(message ?: NETWORK_ERROR)

    class UnknownErrorException(message: String? = null) : Exception(message ?: UNKNOWN_ERROR)
}
