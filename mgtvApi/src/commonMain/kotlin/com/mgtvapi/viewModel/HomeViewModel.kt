package com.mgtvapi.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgtv.shared_core.core.ViewState
import com.mgtvapi.api.model.Clip
import com.mgtvapi.api.model.ProgressClip
import com.mgtvapi.api.repository.MGTVApiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CombinedState(
    val mainFeedClips: ViewState<List<Clip>>,
    val recentlyWatchedClips: ViewState<List<ProgressClip>>
)


class HomeViewModel(private val repo: MGTVApiRepository) : ViewModel() {
    private var _clips = MutableStateFlow<ViewState<List<Clip>>>(ViewState.Empty)
    val clips: StateFlow<ViewState<List<Clip>>> = _clips.asStateFlow()
    private var _paginationLoading = MutableStateFlow<ViewState<Boolean>>(ViewState.Empty)
    val paginationLoading: StateFlow<ViewState<Boolean>> = _paginationLoading.asStateFlow()
    private var _isInitial = MutableStateFlow(true)
    val isInitial: StateFlow<Boolean> = _isInitial.asStateFlow()
    private var _recentlyWatchedClips =
        MutableStateFlow<ViewState<List<ProgressClip>>>(ViewState.Empty)


    val recentlyWatchedClips: StateFlow<ViewState<List<ProgressClip>>> =
        _recentlyWatchedClips.asStateFlow()

    val homeScreenClips: StateFlow<CombinedState> = combine(
        clips,
        recentlyWatchedClips
    ) { clipsState, recentlyWatchedState ->
        CombinedState(clipsState, recentlyWatchedState)
    }.stateIn(viewModelScope, SharingStarted.Lazily, CombinedState(ViewState.Loading, ViewState.Loading))

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

    fun getRecentlyWatched() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _recentlyWatchedClips.value = ViewState.Loading
                val result = repo.getRecentlyWatched()
                _recentlyWatchedClips.value = ViewState.Success(result.progress)
            } catch (e: Exception) {
                _recentlyWatchedClips.value = ViewState.Error(e.message.toString())
            }
        }
    }
}
