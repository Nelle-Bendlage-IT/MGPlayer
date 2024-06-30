package com.mgtvapi.api.model

import kotlinx.serialization.Serializable

@Serializable
internal data class UpdateClipProgressRequest(
    val percentage: Int
)
