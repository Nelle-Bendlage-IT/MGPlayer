package com.mgtvapi.viewModel

import com.mgtvapi.api.model.Clip
import com.mgtvapi.api.repository.MGTVApiRepository
import com.mgtvapi.domain.ResultState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.component.KoinComponent

class HomeViewModel(private val repo: MGTVApiRepository) : KoinComponent {
    private var _clips = MutableStateFlow<ResultState<List<Clip>>>(ResultState.Empty)
    val clips = _clips.asStateFlow()
    private var _paginationLoading = MutableStateFlow<ResultState<Boolean>>(ResultState.Empty)
    val paginationLoading = _paginationLoading.asStateFlow()
    private var _isInitial = MutableStateFlow(true)
    val isInitial = _isInitial.asStateFlow()
    suspend fun getMainFeedClips(offset: Int, count: Int) {
        try {
            _clips.value = ResultState.Loading
            val result = repo.getMainFeed(offset, count)
            _clips.value = ResultState.Success(result.clips)
            _isInitial.value = false
        } catch (e: Exception) {
            _clips.value = ResultState.Error(e.message.toString())
        }
    }

    suspend fun getMainFeedClipsPagination(offset: Int) {
        try {
            _paginationLoading.value = ResultState.Loading
            val result = repo.getMainFeed(offset, 20)
            val mergedData =
                ((_clips.value as ResultState.Success).data + result.clips).toSet().toList()
            _clips.value = ResultState.Success(mergedData)
        } catch (e: Exception) {
            _paginationLoading.value = ResultState.Error(e.message.toString())
        }
    }
}