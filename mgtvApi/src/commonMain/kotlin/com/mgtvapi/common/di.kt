package com.mgtvapi.common

import com.mgtvapi.api.repository.MGTVApiRepository
import com.mgtvapi.api.repository.MGTVApiRepositoryImpl
import com.mgtvapi.api.repository.TokenManager
import com.mgtvapi.api.repository.TokenManagerImpl
import com.mgtvapi.api.service.MGTVApiRemoteService
import com.mgtvapi.viewModel.CommonViewModel
import com.mgtvapi.viewModel.HomeViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val mgtvApiSharedModule =
    module {
        single<HttpClient> { provideHttpClient() }
        single<TokenManager> { TokenManagerImpl() }

        single<MGTVApiRemoteService> { MGTVApiRemoteService(get()) }
        single<MGTVApiRepository> { MGTVApiRepositoryImpl(get(), get()) }
        single {
            HomeViewModel(get())
        }
        single {
            CommonViewModel(get())
        }
    }

private fun provideHttpClient(): HttpClient {
    return HttpClient {
        install(HttpCookies) {
            storage = AcceptAllCookiesStorage()
        }
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    useAlternativeNames = false
                },
            )
        }
    }
}
