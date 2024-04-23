package com.mgtvapi.api.model

import kotlinx.serialization.Serializable

@Serializable
data class MainFeedResponse(
    val ok: Boolean,
    val clips: List<Clip>,
)

@Serializable
data class Clip(
    val id: String,
    val magazineId: Long,
    val canAccess: Boolean,
    val hasDownload: Boolean,
    val categoryId: Long,
    val time: Long,
    val seqNr: Long,
    val hideSeqNr: Boolean,
    val title: String,
    val projectTitle: String,
    val categoryTitle: String?,
    val image: String,
    val description: String,
    val shortDescription: String,
    val duration: String,
    //val next: Any,
    val chapterSections: List<ChapterSection>,
    val participants: List<Participant>,
    val teaserFile: String?,
)

@Serializable
data class NextEpisode(
    val id: String,
    val enum: Int,
)

@Serializable
data class ChapterSection(
    val title: String,
    val chapters: List<Chapter>,
)

@Serializable
data class Chapter(
    val title: String,
    val start: Long,
    val end: Long,
)



@Serializable
data class Participant(
    val id: String,
    val name: String,
    val img: String,
)