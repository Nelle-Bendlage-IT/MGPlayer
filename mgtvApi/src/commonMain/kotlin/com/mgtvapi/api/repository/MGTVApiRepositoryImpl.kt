package com.mgtvapi.api.repository

import com.mgtvapi.api.model.ClipFileResponse
import com.mgtvapi.api.model.MainFeedResponse
import com.mgtvapi.api.service.MGTVApiRemoteService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking


class MGTVApiRepositoryImpl(
    private val apiService: MGTVApiRemoteService,
    private val tokenManager: TokenManager
) :
    MGTVApiRepository {

    init {
        if (!tokenManager.email.isNullOrEmpty()) {
            runBlocking {
                login(tokenManager.email!!, tokenManager.password!!)
            }
        }
    }

    override fun isLoggedIn(): Flow<Boolean> {
        return tokenManager.observableCredentials.map { it != null }
    }

    override suspend fun login(email: String, password: String): Boolean {
        try {
            val result = apiService.login(email, password)
            if (result) {
                tokenManager.saveCredentials(email, password)
            }
            return result
        } catch (e: Exception) {
            //Napier.e(tag = "MGTVApiRepositoryImpl.login", message = "LOGIN_FAILED", throwable = e)
            throw e
        }
    }

    override suspend fun getMainFeed(offset: Int, count: Int): MainFeedResponse {
        try {
            return apiService.getMainFeed(offset, count)
        } catch (e: Exception) {
            /* Napier.e(
                 tag = "MGTVApiRepositoryImpl.getMainFeed",
                 message = "FAILED_TO_FETCH_MAIN_FEED",
                 throwable = e
             )*/
            throw e
        }
    }

    override fun logout() {
        tokenManager.cleanStorage()
    }

    override suspend fun getClipFiles(clipId: String): ClipFileResponse {
        try {
            return apiService.getClipFiles(clipId)
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun getCookies(): String {
        val cookies = apiService.getCookies()
        var cookie = ""
        cookies.forEach { cookie += "${it.name}=${it.value};" }
        return cookie
    }
}