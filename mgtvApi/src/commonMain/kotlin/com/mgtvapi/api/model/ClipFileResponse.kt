package com.mgtvapi.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ClipFileResponse(
    val ok: Boolean,
    val progress: Progress? = null,
    val files: List<ClipFile> = listOf(),
)

@Serializable
data class Progress(
    val id: String,
    val percentage: Long,
    val hasStarted: Boolean,
)

@Serializable
data class ClipFile(
    val size: Long,
    val cc: Boolean? = null,
    val t: String,
    @SerialName("size_readable") val sizeReadable: String,
    val dimensions: String? = null,
    val desc: String,
    val url: String,
) : ListableClip<ClipFile>() {
    override val magazineName: String
        get() = desc

    override fun getSelf() = this
}
