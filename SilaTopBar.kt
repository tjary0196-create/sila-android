package com.sila.messaging.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Reusable top bar used across Sila screens for a consistent header look.
 * Wraps Material3 [TopAppBar] with sensible defaults (bold title, transparent
 * container so it blends with the screen background).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SilaTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    containerColor: Color = Color.Transparent
) {
    TopAppBar(
        title = { Text(title, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
        navigationIcon = navigationIcon,
        actions = actions,
        modifier = modifier,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = containerColor,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}
