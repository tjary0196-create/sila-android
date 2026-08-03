package com.sila.messaging.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.sila.messaging.data.UserRepository
import com.sila.messaging.ui.components.SilaAvatar
import com.sila.messaging.ui.components.SilaEmptyState
import com.sila.messaging.ui.components.SilaErrorState
import com.sila.messaging.ui.components.SilaSearchBar
import com.sila.messaging.ui.components.SilaTopBar
import com.sila.messaging.ui.theme.SilaSpacing
import kotlinx.coroutines.delay

/**
 * "Find people" screen — pill search bar over a live username lookup.
 * Shows a friendly prompt before typing, a spinner while searching, result
 * cards once found, and an empty state when nothing matches.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchUsersScreen(onStartChat: (String) -> Unit, onBack: () -> Unit) {
    var query by remember { mutableStateOf("") }
    val results = remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    val repo = remember { UserRepository() }
    val myUid = FirebaseAuth.getInstance().currentUser?.uid

    var isSearching by remember { mutableStateOf(false) }
    var searchError by remember { mutableStateOf<String?>(null) }
    var hasSearchedOnce by remember { mutableStateOf(false) }
    var retryTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(query, retryTrigger) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            results.value = emptyList()
            isSearching = false
            searchError = null
            return@LaunchedEffect
        }
        delay(300) // debounce so we don't hit Firestore on every keystroke
        isSearching = true
        searchError = null
        try {
            val found = repo.searchByPrefix(trimmed)
            results.value = found.filter { (_, uid) -> uid != myUid }
        } catch (e: Exception) {
            searchError = e.localizedMessage ?: "تعذر إتمام البحث"
        } finally {
            isSearching = false
            hasSearchedOnce = true
        }
    }

    Scaffold(
        topBar = {
            SilaTopBar(
                title = "بحث عن مستخدمين",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                containerColor = MaterialTheme.colorScheme.background
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = SilaSpacing.md)
        ) {
            Spacer(modifier = Modifier.height(SilaSpacing.xs))

            SilaSearchBar(
                value = query,
                onValueChange = { query = it },
                placeholder = "ابحث بالـ username"
            )

            Spacer(modifier = Modifier.height(SilaSpacing.sm))

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    query.isBlank() -> SilaEmptyState(
                        icon = Icons.Filled.PersonSearch,
                        title = "ابحث عن أصدقائك",
                        subtitle = "اكتب اسم المستخدم (@username) للبدء بمحادثة جديدة"
                    )

                    searchError != null -> SilaErrorState(
                        message = searchError!!,
                        onRetry = { retryTrigger++ }
                    )

                    isSearching -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(top = SilaSpacing.xxl).size(28.dp),
                            strokeWidth = 3.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    hasSearchedOnce && results.value.isEmpty() -> SilaEmptyState(
                        icon = Icons.Filled.SearchOff,
                        title = "لا يوجد نتائج",
                        subtitle = "ما لقينا حد باسم \"$query\"، جرب اسم مستخدم مختلف"
                    )

                    else -> LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(SilaSpacing.xs)
                    ) {
                        items(results.value, key = { it.second }) { (uname, uid) ->
                            SearchResultCard(
                                username = uname,
                                onClick = { onStartChat(uid) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(SilaSpacing.md)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultCard(username: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .clickable(onClick = onClick)
            .padding(horizontal = SilaSpacing.md, vertical = SilaSpacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SilaAvatar(name = username, photoUrl = null, seed = username, size = 46.dp)

        Spacer(modifier = Modifier.width(SilaSpacing.sm))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                username,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "اضغط لبدء محادثة",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}
