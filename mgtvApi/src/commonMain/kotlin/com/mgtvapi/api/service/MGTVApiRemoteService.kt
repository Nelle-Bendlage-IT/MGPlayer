package com.mgtvapi.api.service

import co.touchlab.kermit.Logger
import com.mgtvapi.api.model.MagazineResponse
import com.mgtvapi.api.model.MagazinesResponse
import com.mgtvapi.api.model.MainFeedResponse
import com.mgtvapi.api.model.ProgressResponse
import com.mgtvapi.api.model.UpdateClipProgressRequest
import com.mgtvapi.api.model.WatchResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.cookies.cookies
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.Cookie
import io.ktor.http.parameters

class MGTVApiRemoteService(
    private val client: HttpClient,
) {
    companion object {
        private const val BASE_URL: String = "https://massengeschmack.tv"
    }

    suspend fun login(
        email: String,
        password: String,
    ): Boolean {
        val data =
            client.submitForm(
                "$BASE_URL/index_login.php",
                formParameters =
                parameters {
                    append("email", email)
                    append("password", password)
                },
            )
        // Believe it or not:
        // If you're credentials are wrong you're still getting status code 200
        // except of the set-cookie header. That's why I have to check the header
        return data.headers.contains("set-cookie")
    }

    suspend fun getMainFeed(
        offset: Int,
        count: Int,
    ): MainFeedResponse {
        val data =
            client.get("$BASE_URL/api/v2/feed/main") {
                url {
                    parameters.append("offset", "$offset")
                    parameters.append("count", "$count")
                }
            }
        return data.body<MainFeedResponse>()
    }

    suspend fun getClipDetails(clipId: String): WatchResponse {
        val data = client.get("$BASE_URL/api/v2/watch/$clipId")

        return data.body<WatchResponse>()
    }

    suspend fun getCookies(): List<Cookie> {
        return client.cookies(BASE_URL)
    }

    suspend fun getMagazines(): MagazinesResponse {
        val data = client.get("$BASE_URL/api/v2/magazine")

        return data.body<MagazinesResponse>()
    }

    suspend fun getMagazine(magazineID: String, count: Int, offset: Int): MagazineResponse {
        val data = client.get("$BASE_URL/api/v2/feed/filter") {
            url {
                parameters.append("offset", "$offset")
                parameters.append("count", "$count")
                parameters.append("filter", "{\"mag\":{\"$magazineID\":[]}}")
            }
        }
        return data.body<MagazineResponse>()
    }

    suspend fun updateClipProgress(clipID: String, time: Int) {
        val data = client.put("$BASE_URL/api/v2/watch/$clipID/progress") {
            setBody(UpdateClipProgressRequest(percentage = time))
            this.header("Content-Type", "application/json")
        }
        Logger.d(data.body<String>().toString(), tag = "updateClipProgress")
        //return data.status
    }

    suspend fun getRecentlyWatched(): ProgressResponse {
        val data = client.get("$BASE_URL/api/v2/feed/progress/recent")

        return data.body<ProgressResponse>()
    }
}
