package com.mgtvapi.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgtvapi.api.model.Clip
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
    private var _magazine = MutableStateFlow<ResultState<List<Clip>>>(ResultState.Empty)
    val magazineEpisodes: StateFlow<ResultState<List<Clip>>> = _magazine.asStateFlow()
    private var _isInitial = MutableStateFlow(true)
    val isInitial: StateFlow<Boolean> = _isInitial.asStateFlow()
    private var chosenMagazine: String = ""
    fun fetchMagazines() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val data = repo.getMagazines()
                _magazines.value = ResultState.Success(data.magazines)
            } catch (e: Exceptions.UnknownErrorException) {
                _magazines.value = ResultState.Error(e.message.toString())
            }
        }
    }

    fun fetchMagazine(magazineID: String, limit: Int, offset: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val data = repo.getMagazine(magazineID, limit, offset)
                if (_magazine.value is ResultState.Empty || chosenMagazine != magazineID) {
                    _magazine.value = ResultState.Loading
                    chosenMagazine = magazineID
                    _isInitial.value = false
                    _magazine.value = ResultState.Success(data = data.clips)
                    return@launch
                }
                val mergedData =
                    ((_magazine.value as ResultState.Success<List<Clip>>).data + data.clips).toSet()
                        .toList()
                _magazine.value = ResultState.Success(data = mergedData)
            } catch (e: Exceptions.UnknownErrorException) {
                _magazine.value = ResultState.Error(e.message.toString())
            }
        }
    }
}