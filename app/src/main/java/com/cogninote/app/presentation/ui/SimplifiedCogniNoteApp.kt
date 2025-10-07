package com.cogninote.app.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cogninote.app.presentation.ui.screens.simplified.SimpleNotesListScreen
import com.cogninote.app.presentation.ui.screens.simplified.SimpleNoteEditScreen
import com.cogninote.app.presentation.ui.screens.simplified.SimpleSearchScreen
import com.cogninote.app.presentation.viewmodel.SimpleNotesViewModel
import com.cogninote.app.presentation.viewmodel.SimpleNoteEditViewModel
import com.cogninote.app.presentation.viewmodel.SimpleSearchViewModel
import com.cogninote.app.data.entities.SimpleNoteType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimplifiedCogniNoteApp(
    navController: NavHostController = rememberNavController()
) {
    val notesViewModel: SimpleNotesViewModel = hiltViewModel()
    val noteEditViewModel: SimpleNoteEditViewModel = hiltViewModel()
    val searchViewModel: SimpleSearchViewModel = hiltViewModel()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination?.route
    
    // Determine FAB visibility and action based on current screen
    val showFAB = currentDestination == "notes" || currentDestination == "search"
    val fabAction: () -> Unit = {
        navController.navigate("write/new")
    }

    Scaffold(
        topBar = {
            // Simple top bar - just title, no complex navigation
            when (currentDestination) {
                "notes" -> {
                    TopAppBar(
                        title = { Text("CogniNote") }
                    )
                }
                "search" -> {
                    TopAppBar(
                        title = { Text("Search") }
                    )
                }
                else -> {
                    TopAppBar(
                        title = { Text("Edit Note") },
                        navigationIcon = {
                            IconButton(onClick = { navController.navigateUp() }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                            }
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            if (showFAB) {
                ExtendedFloatingActionButton(
                    onClick = fabAction,
                    icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                    text = { Text("Write") }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "notes",
            modifier = Modifier.padding(innerPadding)
        ) {
            // Notes list - the main screen
            composable("notes") {
                SimpleNotesListScreen(
                    viewModel = notesViewModel,
                    onNoteClick = { noteId ->
                        navController.navigate("write/$noteId")
                    },
                    onNewNote = {
                        navController.navigate("write/new")
                    },
                    onSearchClick = {
                        navController.navigate("search")
                    }
                )
            }
            
            // Write/Edit note
            composable("write/{noteId}") { backStackEntry ->
                val noteId = backStackEntry.arguments?.getString("noteId")
                SimpleNoteEditScreen(
                    viewModel = noteEditViewModel,
                    noteId = if (noteId == "new") null else noteId,
                    onSaveComplete = {
                        navController.navigateUp()
                    }
                )
            }
            
            // Search
            composable("search") {
                SimpleSearchScreen(
                    viewModel = searchViewModel,
                    onNoteClick = { noteId ->
                        navController.navigate("write/$noteId")
                    },
                    onBackClick = {
                        navController.navigateUp()
                    }
                )
            }
        }
    }
}

// Simple navigation routes
sealed class SimpleScreen(val route: String) {
    object NotesList : SimpleScreen("notes")
    object WriteNote : SimpleScreen("write/{noteId}")
    object Search : SimpleScreen("search")
}