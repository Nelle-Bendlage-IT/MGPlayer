package com.mgtvapi.api.model

import kotlinx.serialization.Serializable

@Serializable
data class MagazineResponse(
    val ok: Boolean,
    val clips: List<Clip>,
)
