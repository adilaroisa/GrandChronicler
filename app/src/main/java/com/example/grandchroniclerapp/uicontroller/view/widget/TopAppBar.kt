package com.example.grandchroniclerapp.uicontroller.widget

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import com.example.grandchroniclerapp.ui.theme.SoftError

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrandTopAppBar(
    title: String,
    canNavigateBack: Boolean,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
    navigateUp: () -> Unit = {},
    showLogout: Boolean = false,
    onLogoutClick: () -> Unit = {}
) {
    CenterAlignedTopAppBar(
        title = { Text(title) },
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali"
                    )
                }
            }
        },
        actions = {
            if (showLogout) {
                IconButton(onClick = onLogoutClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Logout",
                        tint = SoftError
                    )
                }
            }
        }
    )
}