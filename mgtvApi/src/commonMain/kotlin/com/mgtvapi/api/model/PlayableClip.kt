package com.mgtvapi.api.model

abstract class PlayableClip {
    abstract val magazineTitle: String
    abstract val episodeTitle: String
    abstract val artworkUrl: String
    abstract val summary: String
    abstract val clipID: String
}