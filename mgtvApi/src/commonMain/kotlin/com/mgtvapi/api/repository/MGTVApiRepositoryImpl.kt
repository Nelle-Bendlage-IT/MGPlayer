package com.mgtvapi.api.repository

import co.touchlab.kermit.Logger
import com.mgtvapi.api.model.ClipFileResponse
import com.mgtvapi.api.model.MainFeedResponse
import com.mgtvapi.api.service.MGTVApiRemoteService
import com.mgtvapi.api.util.Exceptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MGTVApiRepositoryImpl(
    private val apiService: MGTVApiRemoteService,
    private val tokenManager: TokenManager,
) :
    MGTVApiRepository {
    override suspend fun init() {
        if (!tokenManager.email.isNullOrEmpty()) {
            login(tokenManager.email!!, tokenManager.password!!)
        }
    }

    override fun isLoggedIn(): Flow<Boolean> {
        return tokenManager.observableCredentials.map { it != null }
    }

    override suspend fun login(
        email: String,
        password: String,
    ): Boolean {
        try {
            val result = apiService.login(email, password)
            if (result) {
                tokenManager.saveCredentials(email, password)
            }
            return result
        } catch (e: Exception) {
            Logger.e("LOGIN", e)
            throw Exceptions.LoginException(e.message)
        }
    }

    override suspend fun getMainFeed(
        offset: Int,
        count: Int,
    ): MainFeedResponse {
        return try {
            apiService.getMainFeed(offset, count)
        } catch (e: Exception) {
            Logger.e("GETMAINFEED", e)
            throw Exceptions.UnknownErrorException(e.message)
        }
    }

    override fun logout() {
        tokenManager.cleanStorage()
    }

    override suspend fun getClipFiles(clipId: String): ClipFileResponse {
        try {
            return apiService.getClipFiles(clipId)
        } catch (e: Exception) {
            Logger.e("GETCLIPFILES", e)
            throw Exceptions.UnknownErrorException(e.message)
        }
    }

    override suspend fun getCookies(): String {
        val cookies = apiService.getCookies()
        var cookie = ""
        cookies.forEach { cookie += "${it.name}=${it.value};" }
        return cookie
    }
}
