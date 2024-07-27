package com.mgtvapi.api.model

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class ProgressResponse(
    val ok: Boolean,
    val progress: List<ProgressClip>,
)

@Serializable
data class ProgressClip(
    val id: String,
    val clip: Clip,
    val hasStarted: Boolean,
    @SerialName("remaining_sec")
    val remainingSec: Long,
    @SerialName("remaining_perc")
    val remainingPerc: Long,
    @SerialName("remaining_stamp")
    val remainingStamp: String,
    val lastAccessed: Long,
)