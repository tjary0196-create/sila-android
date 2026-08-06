package com.sila.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sila.di.ServiceLocator
import com.sila.messaging.core.result.AppResult
import com.sila.messaging.domain.user.UserRepository
import com.sila.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel(
    private val userRepository: UserRepository = ServiceLocator.userRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _results = MutableStateFlow<List<User>>(emptyList())
    val results: StateFlow<List<User>> = _results.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var searchJob: kotlinx.coroutines.Job? = null

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
        searchJob?.cancel()
        val prefix = newQuery.trim()
        if (prefix.isEmpty()) {
            _results.value = emptyList()
            _isLoading.value = false
            return
        }
        searchJob = viewModelScope.launch {
            _isLoading.value = true
            // Firestore prefix queries are cheap but this still avoids hammering it on every keystroke.
            kotlinx.coroutines.delay(250)
            when (val result = userRepository.searchUsers(prefix)) {
                is AppResult.Success -> _results.value = result.data.map { (username, uid) ->
                    User(id = uid, name = "@$username", handle = "@$username")
                }
                is AppResult.Error -> _results.value = emptyList()
            }
            _isLoading.value = false
        }
    }
}
