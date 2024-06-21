package com.mgtvapi.api.model

import com.mgtvapi.Parcelable
import com.mgtvapi.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MagazinesResponse(
    val ok: Boolean,
    val filter: List<String> = listOf(),
    val magazines: List<Magazine>,
)

@Serializable
@Parcelize
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
) : ListableClip<Magazine>(), Parcelable {
    override val magazineName: String
        get() = title

    override fun getSelf() = this
}

@Serializable
@Parcelize
data class Artwork(
    val logo: String,
    val logoSquare: String,
    val banner: Banner? = null,
) : Parcelable

@Serializable
@Parcelize
data class Banner(
    @SerialName("1x") val n1x: String,
    @SerialName("2x") val n2x: String,
) : Parcelable

@Serializable
@Parcelize
data class Category(
    val title: String,
    val categoryId: Long,
) : Parcelable

@Serializable
@Parcelize
data class Contact(
    val email: String,
    val title: String,
) : Parcelable
