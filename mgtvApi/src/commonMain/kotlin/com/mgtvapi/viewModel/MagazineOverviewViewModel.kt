package com.mgtvapi.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgtvapi.api.model.Ep
import com.mgtvapi.api.model.Magazine
import com.mgtvapi.api.repository.MGTVApiRepository
import com.mgtvapi.api.util.Exceptions
import com.mgtvapi.domain.ResultState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MagazineOverviewViewModel(private val repo: MGTVApiRepository) : ViewModel() {
    private var _magazines = MutableStateFlow<ResultState<List<Magazine>>>(ResultState.Empty)
    val magazines: StateFlow<ResultState<List<Magazine>>> = _magazines.asStateFlow()
    private var _magazine = MutableStateFlow<ResultState<List<Ep>>>(ResultState.Empty)
    val magazineEpisodes: StateFlow<ResultState<List<Ep>>> = _magazine.asStateFlow()
    private var isInitial = true

    init {
        fetchMagazines()
    }

    fun fetchMagazines() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val data = repo.getMagazines()
                _magazines.value = ResultState.Success(data.magazines)
                if (isInitial) {
                    fetchMagazine("${data.magazines.first().pid}", 20)
                }
                isInitial = false
            } catch (e: Exceptions.UnknownErrorException) {
                _magazines.value = ResultState.Error(e.message.toString())
            }
        }
    }

    fun fetchMagazine(magazineID: String, limit: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val data = repo.getMagazine(magazineID, limit)
                _magazine.value = ResultState.Success(data = data.eps)
            } catch (e: Exceptions.UnknownErrorException) {
                _magazine.value = ResultState.Error(e.message.toString())
            }
        }
    }
}