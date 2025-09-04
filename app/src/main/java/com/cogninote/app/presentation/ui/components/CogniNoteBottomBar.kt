package com.cogninote.app.presentation.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun CogniNoteBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        val items = listOf(
            BottomNavItem("notes", "Notes", Icons.Default.Note),
            BottomNavItem("search", "Search", Icons.Default.Search),
            BottomNavItem("folders", "Folders", Icons.Default.Folder),
            BottomNavItem("settings", "Settings", Icons.Default.Settings)
        )

        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = { onNavigate(item.route) }
            )
        }
    }
}

private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

@Preview
@Composable
fun CogniNoteBottomBarPreview() {
    MaterialTheme {
        CogniNoteBottomBar(
            currentRoute = "notes",
            onNavigate = {}
        )
    }
}
