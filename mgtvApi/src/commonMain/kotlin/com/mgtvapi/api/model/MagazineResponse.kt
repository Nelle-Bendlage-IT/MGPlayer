package com.mgtvapi.api.model

import kotlinx.serialization.Serializable

@Serializable
data class MagazineResponse(
    val eps: List<Ep>,
//    val pages: Long? = null,
//    val next: Long? = null,
//    val prev: Boolean? = null,
)

@Serializable
data class Ep(
    val identifier: String,
    val pid: Long,
    val contentType: Long,
    val title: String,
    val pdesc: String,
    val img: String,
    val desc: String,
    val duration: String,
    val enum: Long,
    val teaserFile: String,
    val thumbnail: String,
    val date: Long,
    val canAccess: Boolean,
) : PlayableClip() {
    override val artworkUrl: String
        get() = img
    override val summary: String
        get() = desc
    override val episodeTitle: String
        get() = title
    override val clipID: String
        get() = identifier
    override val magazineTitle: String
        get() = pdesc
}

