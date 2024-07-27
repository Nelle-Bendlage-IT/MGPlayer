package com.mgtvapi.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgtv.shared_core.core.ViewState
import com.mgtvapi.api.model.Clip
import com.mgtvapi.api.model.Magazine
import com.mgtvapi.api.repository.MGTVApiRepository
import com.mgtvapi.api.util.Exceptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MagazineOverviewViewModel(private val repo: MGTVApiRepository) : ViewModel() {
    private var _magazines = MutableStateFlow<ViewState<List<Magazine>>>(ViewState.Empty)
    val magazines: StateFlow<ViewState<List<Magazine>>> = _magazines.asStateFlow()
    private var _magazine = MutableStateFlow<ViewState<List<Clip>>>(ViewState.Empty)
    val magazineEpisodes: StateFlow<ViewState<List<Clip>>> = _magazine.asStateFlow()
    private var _isInitial = MutableStateFlow(true)
    val isInitial: StateFlow<Boolean> = _isInitial.asStateFlow()
    private var _chosenMagazine: Magazine? = null
    var chosenMagazine: Magazine?
        get() = _chosenMagazine
        set(value) {
            _chosenMagazine = value
        }

    fun fetchMagazines() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val data = repo.getMagazines()
                _magazines.value = ViewState.Success(data.magazines)
            } catch (e: Exceptions.UnknownErrorException) {
                _magazines.value = ViewState.Error(e.message.toString())
            }
        }
    }

    fun fetchMagazine(magazineID: String, magazine: Magazine, limit: Int, offset: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val data = repo.getMagazine(magazineID, limit, offset)

                if (_magazine.value is ViewState.Empty || chosenMagazine != magazine) {
                    _magazine.value = ViewState.Loading
                    chosenMagazine = magazine
                    _isInitial.value = false
                    _magazine.value = ViewState.Success(data = data.clips)
                    return@launch
                }
                val mergedData =
                    ((_magazine.value as ViewState.Success<List<Clip>>).data + data.clips).toSet()
                        .toList()
                _magazine.value = ViewState.Success(data = mergedData)
            } catch (e: Exceptions.UnknownErrorException) {
                _magazine.value = ViewState.Error(e.message.toString())
            }
        }
    }

    fun onExit() {
        _chosenMagazine = null
        _magazine.value = ViewState.Empty
    }
}