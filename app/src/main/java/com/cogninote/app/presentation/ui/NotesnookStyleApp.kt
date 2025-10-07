package com.cogninote.app.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cogninote.app.presentation.ui.screens.notesnook.NotesnookNotesListScreen
import com.cogninote.app.presentation.ui.screens.notesnook.NotesnookNoteEditScreen
import com.cogninote.app.presentation.ui.screens.notesnook.NotesnookTagsScreen
import com.cogninote.app.presentation.ui.screens.notesnook.NotesnookSettingsScreen
import com.cogninote.app.presentation.ui.screens.simplified.SimpleSearchScreen
import com.cogninote.app.presentation.viewmodel.SimpleNotesViewModel
import com.cogninote.app.presentation.viewmodel.SimpleNoteEditViewModel
import com.cogninote.app.presentation.viewmodel.SimpleSearchViewModel
import com.cogninote.app.presentation.ui.theme.NotesnookLightColorScheme
import com.cogninote.app.presentation.ui.theme.NotesnookDarkColorScheme
import kotlinx.coroutines.launch

data class NavigationItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: String,
    val badgeCount: Int? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesnookStyleApp(
    navController: NavHostController = rememberNavController()
) {
    val notesViewModel: SimpleNotesViewModel = hiltViewModel()
    val noteEditViewModel: SimpleNoteEditViewModel = hiltViewModel()
    val searchViewModel: SimpleSearchViewModel = hiltViewModel()
    
    val notes by notesViewModel.notes.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination?.route
    
    val navigationItems = listOf(
        NavigationItem(
            title = "All Notes",
            selectedIcon = Icons.Filled.Note,
            unselectedIcon = Icons.Outlined.Note,
            route = "notes",
            badgeCount = notes.size
        ),
        NavigationItem(
            title = "Search",
            selectedIcon = Icons.Filled.Search,
            unselectedIcon = Icons.Outlined.Search,
            route = "search"
        ),
        NavigationItem(
            title = "Tags",
            selectedIcon = Icons.Filled.Tag,
            unselectedIcon = Icons.Outlined.Tag,
            route = "tags"
        ),
        NavigationItem(
            title = "Settings",
            selectedIcon = Icons.Filled.Settings,
            unselectedIcon = Icons.Outlined.Settings,
            route = "settings"
        )
    )
    
    MaterialTheme(
        colorScheme = NotesnookLightColorScheme
    ) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                NotesnookDrawerContent(
                    navigationItems = navigationItems,
                    currentRoute = currentDestination,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.fillMaxHeight()
                )
            },
            content = {
                Scaffold(
                    topBar = {
                        NotesnookTopBar(
                            currentRoute = currentDestination,
                            onMenuClick = {
                                scope.launch { drawerState.open() }
                            },
                            onSearchClick = {
                                navController.navigate("search")
                            }
                        )
                    },
                    floatingActionButton = {
                        if (currentDestination == "notes" || currentDestination == "search") {
                            ExtendedFloatingActionButton(
                                onClick = { navController.navigate("write/new") },
                                icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                                text = { Text("New Note") },
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                ) { paddingValues ->
                    NavHost(
                        navController = navController,
                        startDestination = "notes",
                        modifier = Modifier.padding(paddingValues)
                    ) {
                        composable("notes") {
                            NotesnookNotesListScreen(
                                onWriteNoteClick = { 
                                    navController.navigate("write/new")
                                },
                                onNoteClick = { noteId ->
                                    navController.navigate("write/$noteId")
                                },
                                onTagClick = { tag ->
                                    navController.navigate("search/$tag")
                                },
                                notesViewModel = notesViewModel
                            )
                        }
                        
                        composable("search") {
                            SimpleSearchScreen(
                                viewModel = searchViewModel,
                                onNoteClick = { noteId ->
                                    navController.navigate("write/$noteId")
                                },
                                onBackClick = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        
                        composable("search/{tag}") { backStackEntry ->
                            val tag = backStackEntry.arguments?.getString("tag")
                            LaunchedEffect(tag) {
                                tag?.let { searchViewModel.searchForTag(it) }
                            }
                            SimpleSearchScreen(
                                viewModel = searchViewModel,
                                onNoteClick = { noteId ->
                                    navController.navigate("write/$noteId")
                                },
                                onBackClick = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        
                        composable("write/{noteId}") { backStackEntry ->
                            val noteId = backStackEntry.arguments?.getString("noteId")
                            NotesnookNoteEditScreen(
                                viewModel = noteEditViewModel,
                                noteId = if (noteId == "new") null else noteId,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        
                        composable("tags") {
                            NotesnookTagsScreen(
                                viewModel = notesViewModel,
                                onTagClick = { tag ->
                                    navController.navigate("search/$tag")
                                },
                                onSearchClick = {
                                    navController.navigate("search")
                                }
                            )
                        }
                        
                        composable("settings") {
                            NotesnookSettingsScreen(
                                viewModel = notesViewModel,
                                onExportNotes = {
                                    // TODO: Implement export functionality
                                },
                                onAbout = {
                                    // About dialog handled within screen
                                }
                            )
                        }
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesnookTopBar(
    currentRoute: String?,
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    val title = when (currentRoute) {
        "notes" -> "All Notes"
        "search" -> "Search"
        "tags" -> "Tags"
        "settings" -> "Settings"
        else -> "CogniNote"
    }
    
    TopAppBar(
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold
            )
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu"
                )
            }
        },
        actions = {
            if (currentRoute == "notes") {
                IconButton(onClick = onSearchClick) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
fun NotesnookDrawerContent(
    navigationItems: List<NavigationItem>,
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(
        modifier = modifier,
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerContentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // App branding
            Row(
                modifier = Modifier.padding(vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Note,
                            contentDescription = "CogniNote",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = "CogniNote",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Smart Notes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
            
            Divider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                modifier = Modifier.padding(vertical = 16.dp)
            )
            
            // Navigation items
            LazyColumn {
                items(navigationItems) { item ->
                    val isSelected = currentRoute == item.route
                    
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title
                            )
                        },
                        label = {
                            Text(
                                text = item.title,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        badge = {
                            item.badgeCount?.let { count ->
                                if (count > 0) {
                                    Badge {
                                        Text(text = count.toString())
                                    }
                                }
                            }
                        },
                        selected = isSelected,
                        onClick = { onNavigate(item.route) },
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    }
}