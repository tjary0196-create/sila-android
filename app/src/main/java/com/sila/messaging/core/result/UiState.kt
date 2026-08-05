package com.sila.messaging.core.result

data class UiState<T>(
    val loading: Boolean = false,
    val data: T? = null,
    val error: String? = null
)
