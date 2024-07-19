package com.mgtvapi.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgtv.shared_core.core.ViewState
import com.mgtvapi.api.model.Clip
import com.mgtvapi.api.repository.MGTVApiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val repo: MGTVApiRepository) : ViewModel() {
    private var _clips = MutableStateFlow<ViewState<List<Clip>>>(ViewState.Empty)
    val clips: StateFlow<ViewState<List<Clip>>> = _clips.asStateFlow()
    private var _paginationLoading = MutableStateFlow<ViewState<Boolean>>(ViewState.Empty)
    val paginationLoading: StateFlow<ViewState<Boolean>> = _paginationLoading.asStateFlow()
    private var _isInitial = MutableStateFlow(true)
    val isInitial: StateFlow<Boolean> = _isInitial.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repo.init()
        }
    }

    fun getMainFeedClips(
        offset: Int,
        count: Int,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _clips.value = ViewState.Loading
                val result = repo.getMainFeed(offset, count)
                _clips.value = ViewState.Success(result.clips)
                _isInitial.value = false
            } catch (e: Exception) {
                _clips.value = ViewState.Error(e.message.toString())
            }
        }
    }

    fun getMainFeedClipsPagination(offset: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _paginationLoading.value = ViewState.Loading
                val result = repo.getMainFeed(offset, 20)
                val mergedData =
                    ((_clips.value as ViewState.Success).data + result.clips).toSet()
                        .toList()
                _clips.value = ViewState.Success(mergedData)
            } catch (e: Exception) {
                _paginationLoading.value = ViewState.Error(e.message.toString())
            }
        }
    }

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            repo.logout()
        }
    }
}
