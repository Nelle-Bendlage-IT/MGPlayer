/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mgplayer.tv.data.util

object StringConstants {

    object Composable {
        object ContentDescription {
            fun moviePoster(movieName: String) = "Movie poster of $movieName"
            fun image(imageName: String) = "image of $imageName"
            const val MoviesCarousel = "Movies Carousel"
            const val UserAvatar = "User Profile Button"
            const val DashboardSearchButton = "Dashboard Search Button"
            const val BrandLogoImage = "Brand Logo Image"
            const val FilterSelected = "Filter Selected"
            fun reviewerName(name: String) = "$name's logo"
        }

        const val CategoryDetailsFailureSubject = "category details"
        const val MoviesFailureSubject = "movies"
        const val MovieDetailsFailureSubject = "movie details"
        const val HomeScreenTrendingTitle = "Aktuelle Folgen"
        const val HomeScreenNowPlayingMoviesTitle = "Now Playing Movies"
        const val PopularFilmsThisWeekTitle = "Popular films this week"
        const val BingeWatchDramasTitle = "Bingewatch dramas"


        const val VideoPlayerControlPlaylistButton = "Playlist Button"
        const val VideoPlayerControlClosedCaptionsButton = "Playlist Button"
        const val VideoPlayerControlSettingsButton = "Playlist Button"
        const val VideoPlayerControlPlayPauseButton = "Playlist Button"
        const val VideoPlayerControlForward = "Fast forward 10 seconds"
    }
}
