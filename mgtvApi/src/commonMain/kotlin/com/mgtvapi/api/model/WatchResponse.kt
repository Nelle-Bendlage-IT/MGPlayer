package com.mgtvapi.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WatchResponse(
    val ok: Boolean,
    val clip: Clip,
    val progress: Progress,
    val stream: Stream,
    val next: Next? = null,
    val participants: List<Participant> = listOf()
)


@Serializable
data class Next(
    val id: String,
    val enum: Long,
)


@Serializable
data class Stream(
    val id: String,
    val access: Boolean,
    val media: Media,
)

@Serializable
data class Media(
    val streams: Streams,
    val files: List<File>,
)

@Serializable
data class Streams(
    @SerialName("application/x-mpegURL")
    val applicationXMpegUrl: String,
)

@Serializable
data class File(
    val size: Long,
    val cc: Boolean? = null,
    val t: String,
    @SerialName("size_readable")
    val sizeReadable: String,
    val dimensions: String? = null,
    val desc: String,
    val url: String,
) : ListableClip<File>() {
    override val magazineName: String
        get() = desc

    override fun getSelf() = this
}

@Serializable
data class Progress(
    val id: String,
    val percentage: Long,
    val hasStarted: Boolean,
)

