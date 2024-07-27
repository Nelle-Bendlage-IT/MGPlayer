package com.mgtvapi.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.mgtv.shared_core.core.ViewState
import com.mgtvapi.api.model.Clip
import com.mgtvapi.api.model.File
import com.mgtvapi.api.model.Progress
import com.mgtvapi.api.model.WatchResponse
import com.mgtvapi.api.repository.MGTVApiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

data class ClipData(
    val progress: Progress? = null,
    val clip: Clip? = null,
    val file: File? = null,
    val clipID: String = ""
)

class CommonViewModel(private val repo: MGTVApiRepository) : KoinComponent, ViewModel() {
    private var _clipData = MutableStateFlow<ViewState<WatchResponse>>(ViewState.Empty)
    val clipData: StateFlow<ViewState<WatchResponse>> = _clipData.asStateFlow()
    private var _selectedClip: ClipData = ClipData()
    var progress: Progress?
        get() = _selectedClip.progress
        set(value) {
            _selectedClip = _selectedClip.copy(progress = value)
        }

    var clip: Clip?
        get() = _selectedClip.clip
        set(value) {
            _selectedClip = _selectedClip.copy(clip = value)
        }

    var file: File?
        get() = _selectedClip.file
        set(value) {
            _selectedClip = _selectedClip.copy(file = value)
        }

    var clipID: String
        get() = _selectedClip.clipID
        set(value) {
            _selectedClip = _selectedClip.copy(clipID = value)
        }

    fun getClipFiles() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _clipData.value = ViewState.Loading
                val result = repo.getClipDetails(_selectedClip.clipID)
                file = result.stream.media.files[0]
                clip = result.clip
                _clipData.value =
                    ViewState.Success(result)
            } catch (e: Exception) {
                _clipData.value = ViewState.Error("${e.message}")
            }
        }
    }

    fun updateClipProgress(progress: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repo.updateClipProgress(_selectedClip.clipID, progress)
            } catch (e: Exception) {
                Logger.e("${e.message}")
            }
        }
    }
}