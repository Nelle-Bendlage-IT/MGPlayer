package com.mgtvapi.api.repository

import com.mgtvapi.api.model.MagazineResponse
import com.mgtvapi.api.model.MagazinesResponse
import com.mgtvapi.api.model.MainFeedResponse
import com.mgtvapi.api.model.WatchResponse
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

    suspend fun getClipDetails(clipId: String): WatchResponse

    fun isLoggedIn(): Flow<Boolean>

    fun logout()

    suspend fun getCookies(): String

    suspend fun init(): Unit

    suspend fun getMagazines(): MagazinesResponse

    suspend fun getMagazine(magazineID: String, limit: Int, offset: Int): MagazineResponse

    suspend fun updateClipProgress(magazineID: String, progress: Int)
}
