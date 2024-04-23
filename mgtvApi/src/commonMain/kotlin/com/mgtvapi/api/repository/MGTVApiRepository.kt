package com.mgtvapi.api.repository

import com.mgtvapi.api.model.MainFeedResponse
import kotlinx.coroutines.flow.Flow


interface MGTVApiRepository {
    suspend fun login(email: String, password: String): Boolean
    suspend fun getMainFeed(offset: Int, count: Int): MainFeedResponse

    fun isLoggedIn(): Flow<Boolean>
    fun logout(): Unit
}