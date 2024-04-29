package com.mgtvapi.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ClipFileResponse(
    val ok: Boolean,
    val progress: Progress,
    val files: List<ClipFile>,
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
)
