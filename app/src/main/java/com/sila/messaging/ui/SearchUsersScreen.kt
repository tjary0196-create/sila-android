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
import com.sila.messaging.ui.screens.search.SearchUsersViewModel
import com.sila.messaging.ui.components.SilaAvatar
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sila.messaging.data.user.FirestoreUserRepository
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
    val viewModel: SearchUsersViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return SearchUsersViewModel(FirestoreUserRepository()) as T
        }
    })

    val uiState by viewModel.ui.collectAsState()
    val myUid = FirebaseAuth.getInstance().currentUser?.uid
    val results = uiState.results.filter { it.second != myUid }

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
                value = uiState.query,
                onValueChange = { viewModel.onQueryChange(it) },
                placeholder = "ابحث بالـ username"
            )

            Spacer(modifier = Modifier.height(SilaSpacing.sm))

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.query.isBlank() -> SilaEmptyState(
                        icon = Icons.Filled.PersonSearch,
                        title = "ابحث عن أصدقائك",
                        subtitle = "اكتب اسم المستخدم (@username) للبدء بمحادثة جديدة"
                    )

                    uiState.error != null -> SilaErrorState(
                        message = uiState.error!!,
                        onRetry = { viewModel.onQueryChange(uiState.query) }
                    )

                    uiState.isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(top = SilaSpacing.xxl).size(28.dp),
                            strokeWidth = 3.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    results.isEmpty() && uiState.query.isNotBlank() -> SilaEmptyState(
                        icon = Icons.Filled.SearchOff,
                        title = "لا يوجد نتائج",
                        subtitle = "ما لقينا حد باسم \"${uiState.query}\"، جرب اسم مستخدم مختلف"
                    )

                    else -> LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(SilaSpacing.xs)
                    ) {
                        items(results, key = { it.second }) { (uname, uid) ->
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
