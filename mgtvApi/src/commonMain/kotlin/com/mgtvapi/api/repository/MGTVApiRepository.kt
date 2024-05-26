package com.mgtvapi.api.repository

import com.mgtvapi.api.model.ClipFileResponse
import com.mgtvapi.api.model.MainFeedResponse
import kotlinx.coroutines.flow.Flow

interface MGTVApiRepository {
    suspend fun login(
        email: String,
        password: String,
    ): Boolean

    suspend fun getMainFeed(
        offset: Int,
        count: Int,
    ): MainFeedResponse

    suspend fun getClipFiles(clipId: String): ClipFileResponse

    fun isLoggedIn(): Flow<Boolean>

    fun logout()

    suspend fun getCookies(): String

    suspend fun init(): Unit
}
