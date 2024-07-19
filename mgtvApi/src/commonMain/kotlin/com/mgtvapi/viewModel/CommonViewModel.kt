package com.mgtvapi.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.mgtv.shared_core.core.ViewState
import com.mgtvapi.api.model.WatchResponse
import com.mgtvapi.api.repository.MGTVApiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent


class CommonViewModel(private val repo: MGTVApiRepository) : KoinComponent, ViewModel() {
    private var _clipData = MutableStateFlow<ViewState<WatchResponse>>(ViewState.Empty)
    val clipData: StateFlow<ViewState<WatchResponse>> = _clipData.asStateFlow()

    fun getClipFiles(clipId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _clipData.value = ViewState.Loading
                val result = repo.getClipDetails(clipId)
                _clipData.value =
                    ViewState.Success(result)
            } catch (e: Exception) {
                _clipData.value = ViewState.Error("${e.message}")
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
