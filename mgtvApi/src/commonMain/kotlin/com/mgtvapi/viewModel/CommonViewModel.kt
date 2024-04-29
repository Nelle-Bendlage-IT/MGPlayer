package com.mgtvapi.viewModel

import com.mgtvapi.api.model.ClipFile
import com.mgtvapi.api.repository.MGTVApiRepository
import com.mgtvapi.domain.ResultState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.component.KoinComponent

data class ClipData(
    val cookie: String,
    val clipFiles: List<ClipFile>
)

class CommonViewModel(private val repo: MGTVApiRepository) : KoinComponent {
    private var _clipData = MutableStateFlow<ResultState<ClipData>>(ResultState.Empty)
    val clipData = _clipData.asStateFlow()
    suspend fun getClipFiles(clipId: String) {
        try {
            _clipData.value = ResultState.Loading
            val cookies = repo.getCookies()
            val result = repo.getClipFiles(clipId)
            _clipData.value =
                ResultState.Success(ClipData(cookie = cookies, clipFiles = result.files))
        } catch (e: Exception) {
            _clipData.value = ResultState.Error("${e.message}")
        }
    }
}