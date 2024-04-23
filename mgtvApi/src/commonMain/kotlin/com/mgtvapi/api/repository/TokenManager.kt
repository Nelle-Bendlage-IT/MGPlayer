package com.mgtvapi.api.repository

import kotlinx.coroutines.flow.Flow

interface TokenManager {

    val observableCredentials: Flow<String?>
    var email: String?
    var password: String?
    fun saveCredentials(email: String, password: String) {
        this.email = email
        this.password = password
    }

    fun cleanStorage()
}

enum class StorageKeys {
    EMAIL,
    PASSWORD;

    val key get() = this.name
}