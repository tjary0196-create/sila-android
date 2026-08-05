package com.sila.messaging.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sila.messaging.domain.user.UserRepository
import com.sila.messaging.core.result.AppResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchUi(
    val query: String = "",
    val results: List<Pair<String, String>> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class SearchUsersViewModel(private val userRepo: UserRepository) : ViewModel() {

    private val _ui = MutableStateFlow(SearchUi())
    val ui: StateFlow<SearchUi> = _ui

    fun onQueryChange(newQuery: String) {
        _ui.update { it.copy(query = newQuery) }
        if (newQuery.isBlank()) {
            _ui.update { it.copy(results = emptyList(), isLoading = false) }
            return
        }
        
        search(newQuery)
    }

    private fun search(query: String) {
        _ui.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val result = userRepo.searchUsers(query)
            if (result is AppResult.Success) {
                _ui.update { it.copy(results = result.data, isLoading = false) }
            } else if (result is AppResult.Error) {
                _ui.update { it.copy(error = result.message, isLoading = false) }
            }
        }
    }
}
