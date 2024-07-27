package com.mgtvapi.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MagazinesResponse(
    val ok: Boolean,
    val filter: List<String> = listOf(),
    val magazines: List<Magazine>,
)

@Serializable
data class Magazine(
    val title: String,
    val handle: String,
    val artwork: Artwork,
    val pid: Long,
    val categories: List<Category>,
    val hosts: List<String>,
    val contact: List<Contact>,
    val active: Boolean,
    val original: Boolean,
) : ListableClip<Magazine>() {
    override val magazineName: String
        get() = title

    override fun getSelf() = this
}

@Serializable
data class Artwork(
    val logo: String,
    val logoSquare: String,
    val banner: Banner? = null,
)

@Serializable
data class Banner(
    @SerialName("1x") val n1x: String,
    @SerialName("2x") val n2x: String,
)

@Serializable
data class Category(
    val title: String,
    val categoryId: Long,
)

@Serializable
data class Contact(
    val email: String,
    val title: String,
)
