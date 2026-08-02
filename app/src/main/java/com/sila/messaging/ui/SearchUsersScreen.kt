package com.sila.messaging.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sila.messaging.data.UserRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchUsersScreen(onStartChat: (String) -> Unit, onBack: () -> Unit) {
    var query by remember { mutableStateOf("") }
    val results = remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    val repo = remember { UserRepository() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("بحث عن مستخدمين", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("ابحث بالـ username") },
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )
            LazyColumn {
                items(results.value) { (uname, uid) ->
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clickable { onStartChat(uid) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(44.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(uname.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(uname, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }

    LaunchedEffect(query) {
        if (query.length < 1) { results.value = emptyList(); return@LaunchedEffect }
        scope.launch { results.value = repo.searchByPrefix(query) }
    }
}
