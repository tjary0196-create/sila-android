package com.sila.messaging.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sila.messaging.data.UserRepository
import kotlinx.coroutines.launch

@Composable
fun SearchUsersScreen(onStartChat: (String) -> Unit) {
    var query by remember { mutableStateOf("") }
    val results = remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    val repo = remember { UserRepository() }
    val scope = rememberCoroutineScope()

    Column {
        OutlinedTextField(value = query, onValueChange = { query = it }, label = { Text("search username") })
        LazyColumn {
            items(results.value) { (uname, uid) ->
                Text(
                    text = uname,
                    modifier = Modifier
                        .clickable { onStartChat(uid) }
                        .padding(12.dp)
                )
            }
        }
    }

    LaunchedEffect(query) {
        if (query.length < 1) {
            results.value = emptyList()
            return@LaunchedEffect
        }
        scope.launch {
            val res = repo.searchByPrefix(query)
            results.value = res
        }
    }
}
