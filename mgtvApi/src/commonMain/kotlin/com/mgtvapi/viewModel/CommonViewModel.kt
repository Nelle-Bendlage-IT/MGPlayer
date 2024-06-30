package com.mgtvapi.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.mgtvapi.api.model.ClipFile
import com.mgtvapi.api.repository.MGTVApiRepository
import com.mgtvapi.domain.ResultState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

data class ClipData(
    val cookie: String,
    val clipFiles: List<ClipFile>,
)

class CommonViewModel(private val repo: MGTVApiRepository) : KoinComponent, ViewModel() {
    private var _clipData = MutableStateFlow<ResultState<ClipData>>(ResultState.Empty)
    val clipData: StateFlow<ResultState<ClipData>> = _clipData.asStateFlow()

    fun getClipFiles(clipId: String) {
        viewModelScope.launch(Dispatchers.IO) {
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

    fun updateClipProgress(clipId: String, progress: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repo.updateClipProgress(clipId, progress)
            } catch (e: Exception) {
                Logger.e("${e.message}")
            }
        }
    }
}
