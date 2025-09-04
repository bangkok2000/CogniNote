package com.cogninote.app.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cogninote.app.presentation.ui.screens.NotesListScreen
import com.cogninote.app.presentation.ui.screens.NoteEditScreen
import com.cogninote.app.presentation.ui.screens.SearchScreen
import com.cogninote.app.presentation.ui.screens.SettingsScreen
import com.cogninote.app.presentation.ui.screens.TemplateSelectionScreen
import com.cogninote.app.presentation.ui.screens.FolderManagementScreen
import com.cogninote.app.presentation.ui.screens.FolderContentScreen
import com.cogninote.app.presentation.ui.components.CogniNoteBottomBar
import com.cogninote.app.presentation.ui.components.CogniNoteTopBar
import com.cogninote.app.presentation.ui.components.NavigationDrawer
import com.cogninote.app.presentation.ui.components.OverflowMenu
import com.cogninote.app.presentation.ui.components.rememberOverflowMenuState
import com.cogninote.app.presentation.ui.components.FloatingGradientFAB
import com.cogninote.app.presentation.viewmodel.NotesViewModel
import com.cogninote.app.presentation.viewmodel.FolderManagementViewModel
import com.cogninote.app.presentation.viewmodel.NoteEditViewModel
import com.cogninote.app.presentation.ui.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CogniNoteApp(
    navController: NavHostController = rememberNavController()
) {
    val notesViewModel: NotesViewModel = hiltViewModel()
    val noteEditViewModel: NoteEditViewModel = hiltViewModel()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination?.route
    
    // UI state management
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val overflowMenuState = rememberOverflowMenuState()
    var showOverflowMenu by remember { mutableStateOf(false) }
    
    // Note editing state
    val isNewNote by noteEditViewModel.isNewNote.collectAsStateWithLifecycle()
    val hasUnsavedChanges by noteEditViewModel.hasUnsavedChanges.collectAsStateWithLifecycle()
    val isLoading by noteEditViewModel.isLoading.collectAsStateWithLifecycle()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            NavigationDrawer(
                onNavigateToNotes = {
                    navController.navigate("notes") {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                    scope.launch { drawerState.close() }
                },
                onNavigateToSearch = {
                    navController.navigate("search")
                    scope.launch { drawerState.close() }
                },
                onNavigateToSettings = {
                    navController.navigate("settings")
                    scope.launch { drawerState.close() }
                },
                onNavigateToTemplates = {
                    navController.navigate("templates")
                    scope.launch { drawerState.close() }
                },
                onCreateFolder = {
                    // TODO: Implement folder creation
                    scope.launch { drawerState.close() }
                },
                onShowStatistics = {
                    overflowMenuState.showStatisticsDialog = true
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
        topBar = {
            Box {
                CogniNoteTopBar(
                    currentRoute = currentDestination,
                    onNavigationClick = {
                        if (currentDestination == "notes" || currentDestination == null) {
                            scope.launch { drawerState.open() }
                        } else {
                            navController.popBackStack()
                        }
                    },
                    onSearchClick = { navController.navigate("search") },
                    onMenuClick = { showOverflowMenu = true },
                    onSaveNote = if (currentDestination?.startsWith("note/") == true) {
                        { noteEditViewModel.saveNote() }
                    } else null,
                    onDeleteNote = if (currentDestination?.startsWith("note/") == true && !isNewNote) {
                        { 
                            // TODO: Show delete confirmation dialog
                            navController.popBackStack()
                        }
                    } else null,
                    onClearSearch = if (currentDestination == "search") {
                        {
                            notesViewModel.clearSearch()
                            notesViewModel.clearSelectedTags()
                        }
                    } else null,
                    isNewNote = isNewNote,
                    hasUnsavedChanges = hasUnsavedChanges,
                    isLoading = isLoading,
                    hasSearchQuery = currentDestination == "search" && (
                        notesViewModel.searchQuery.value.isNotBlank() || 
                        notesViewModel.selectedTags.value.isNotEmpty()
                    )
                )
                
                OverflowMenu(
                    expanded = showOverflowMenu,
                    onDismiss = { showOverflowMenu = false },
                    onImportNotes = { overflowMenuState.showImportDialog = true },
                    onExportNotes = { overflowMenuState.showExportDialog = true },
                    onBackupData = { overflowMenuState.showBackupDialog = true },
                    onRestoreData = { overflowMenuState.showRestoreDialog = true },
                    onShowStatistics = { overflowMenuState.showStatisticsDialog = true },
                    onShowHelp = { overflowMenuState.showHelpDialog = true },
                    onShowAbout = { overflowMenuState.showAboutDialog = true },
                    currentRoute = currentDestination
                )
            }
        },
        bottomBar = {
            CogniNoteBottomBar(
                currentRoute = currentDestination,
                onNavigate = { route ->
                    navController.navigate(route) {
                        // Pop up to the start destination to avoid building up a large stack
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                }
            )
        },
        floatingActionButton = {
            when (currentDestination) {
                "notes", null -> {
                    ExtendedFloatingActionButton(
                        onClick = { 
                            noteEditViewModel.resetForNewNote()
                            navController.navigate("note/new")
                        },
                        containerColor = PrimaryBlue,
                        contentColor = Color.White,
                        text = { Text("New Note") },
                        icon = { 
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Create Note"
                            )
                        }
                    )
                }
                "folders" -> {
                    // FAB is handled by FolderManagementScreen itself
                    null
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "notes",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("notes") {
                NotesListScreen(
                    viewModel = notesViewModel,
                    onNoteClick = { noteId ->
                        navController.navigate("note/$noteId")
                    }
                )
            }
            
            composable("note/{noteId}?templateId={templateId}") { backStackEntry ->
                val noteId = backStackEntry.arguments?.getString("noteId")
                val templateId = backStackEntry.arguments?.getString("templateId")
                NoteEditScreen(
                    noteId = if (noteId == "new") null else noteId,
                    templateId = templateId,
                    onNavigateBack = { navController.popBackStack() },
                    onDeleteNote = { 
                        navController.popBackStack()
                    },
                    viewModel = noteEditViewModel
                )
            }
            
            composable("search") {
                SearchScreen(
                    viewModel = notesViewModel,
                    onNoteClick = { noteId ->
                        navController.navigate("note/$noteId")
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            composable("settings") {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            composable("templates") {
                TemplateSelectionScreen(
                    onTemplateSelected = { template ->
                        // Create note from template and navigate to edit
                        navController.navigate("note/new?templateId=${template.id}")
                    },
                    onNavigateBack = { navController.popBackStack() },
                    onCreateCustomTemplate = { 
                        navController.navigate("note/new")
                    }
                )
            }
            
            composable("folders") {
                val folderViewModel: FolderManagementViewModel = hiltViewModel()
                FolderManagementScreen(
                    folders = folderViewModel.folders.collectAsStateWithLifecycle().value,
                    notes = notesViewModel.notes.collectAsStateWithLifecycle().value,
                    onFolderClick = { folder ->
                        // Navigate to folder content screen
                        navController.navigate("folder/${folder.id}")
                    },
                    onCreateFolder = { name, parentId ->
                        folderViewModel.createFolder(name, parentId)
                    },
                    onEditFolder = { folder ->
                        folderViewModel.updateFolder(folder)
                    },
                    onDeleteFolder = { folder ->
                        folderViewModel.deleteFolder(folder)
                    },
                    onMoveFolder = { folder, newParentId ->
                        folderViewModel.moveFolder(folder, newParentId)
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            composable("folder/{folderId}") { backStackEntry ->
                val folderId = backStackEntry.arguments?.getString("folderId")
                val folderViewModel: FolderManagementViewModel = hiltViewModel()
                val folder = folderViewModel.folders.collectAsStateWithLifecycle().value.find { it.id == folderId }
                
                if (folder != null) {
                    FolderContentScreen(
                        folder = folder,
                        onNavigateBack = { navController.popBackStack() },
                        onNoteClick = { noteId ->
                            navController.navigate("note/$noteId")
                        },
                        onMoveNotesToFolder = { noteIds, targetFolderId ->
                            // Move notes to the target folder
                            scope.launch {
                                try {
                                    for (noteId in noteIds) {
                                        val note = notesViewModel.notes.value.find { it.id == noteId }
                                        if (note != null) {
                                            val updatedNote = note.copy(folderId = targetFolderId)
                                            notesViewModel.updateNote(updatedNote)
                                        }
                                    }
                                } catch (e: Exception) {
                                    // Handle error if needed
                                }
                            }
                        },
                        onCreateNote = {
                            // Create a new note in this folder
                            noteEditViewModel.resetForNewNote()
                            noteEditViewModel.setFolderId(folder.id)
                            navController.navigate("note/new")
                        }
                    )
                } else {
                    // Folder not found, go back
                    LaunchedEffect(Unit) {
                        navController.popBackStack()
                    }
                }
            }
        }
    }
    }
}
