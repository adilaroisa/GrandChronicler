package com.example.grandchroniclerapp.uicontroller.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.grandchroniclerapp.R
import com.example.grandchroniclerapp.data.UserPreferences
import com.example.grandchroniclerapp.ui.theme.PastelBluePrimary
import com.example.grandchroniclerapp.uicontroller.view.profile.ProfileScreen
import com.example.grandchroniclerapp.uicontroller.view.profile.EditProfileScreen
import com.example.grandchroniclerapp.uicontroller.view.profile.AboutScreen
import com.example.grandchroniclerapp.uicontroller.view.search.SearchScreen
import com.example.grandchroniclerapp.uicontroller.view.article.DetailArticleScreen
import com.example.grandchroniclerapp.uicontroller.view.article.EditArticleScreen
import com.example.grandchroniclerapp.uicontroller.view.article.InsertArticleScreen
import com.example.grandchroniclerapp.uicontroller.view.auth.LoginScreen
import com.example.grandchroniclerapp.uicontroller.view.auth.RegisterScreen
import com.example.grandchroniclerapp.uicontroller.view.auth.TermsOfServiceScreen
import com.example.grandchroniclerapp.uicontroller.view.home.HomeScreen

@Composable
fun PengelolaHalaman(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    // --- [LOGIKA CEK SESI USER] ---
    val context = LocalContext.current
    val userPreferences = remember { UserPreferences(context) }
    val userIdState by userPreferences.getUserId.collectAsState(initial = null)

    if (userIdState == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PastelBluePrimary)
        }
        return
    }

    // Start Destination
    val startDestination = if (userIdState != -1) DestinasiHome.route else DestinasiLogin.route

    // --- [NAVIGASI UTAMA] ---
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Daftar rute saat Bottom Bar HARUS DISEMBUNYIKAN
    val excludedRoutes = listOf(
        DestinasiLogin.route,
        DestinasiRegister.route,
        DestinasiTerms.route,
        DestinasiDetail.routeWithArg,
        DestinasiEditArticle.routeWithArgs,
        "edit_profile",
        DestinasiUpload.route,
        DestinasiAbout.route
    )

    val showBottomBar = currentRoute != null &&
            !excludedRoutes.contains(currentRoute) &&
            !currentRoute.startsWith(DestinasiDetail.route) &&
            !currentRoute.startsWith(DestinasiEditArticle.route)

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomBarGrandChronicler(navController)
            }
        },
        modifier = modifier
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            // --- AUTHENTICATION ---
            composable(DestinasiLogin.route) {
                LoginScreen(
                    onLoginSuccess = {
                        // Navigasi ke Home dan hapus Login dari backstack
                        navController.navigate(DestinasiHome.route) {
                            popUpTo(DestinasiLogin.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(DestinasiRegister.route)
                    }
                )
            }

            composable(DestinasiRegister.route) {
                RegisterScreen(
                    onRegisterSuccess = { navController.popBackStack() },
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToTerms = { navController.navigate(DestinasiTerms.route) }
                )
            }

            composable(DestinasiTerms.route) {
                TermsOfServiceScreen(
                    navigateBack = { navController.popBackStack() }
                )
            }

            // --- MENU UTAMA ---
            composable(DestinasiHome.route) {
                HomeScreen(
                    onDetailClick = { articleId ->
                        navController.navigate("${DestinasiDetail.route}/$articleId")
                    }
                )
            }

            // --- SEARCH SCREEN ---
            composable(
                route = "${DestinasiSearch.route}?query={query}",
                arguments = listOf(
                    navArgument("query") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val query = backStackEntry.arguments?.getString("query")
                SearchScreen(
                    initialQuery = query,
                    onDetailClick = { articleId ->
                        navController.navigate("${DestinasiDetail.route}/$articleId")
                    }
                )
            }

            // --- HALAMAN UPLOAD ---
            composable(DestinasiUpload.route) {
                InsertArticleScreen(
                    navigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // --- PROFILE ---
            composable(DestinasiProfile.route) {
                ProfileScreen(
                    onEditProfile = {
                        navController.navigate("edit_profile")
                    },
                    onEditArticle = { articleId ->
                        navController.navigate("${DestinasiEditArticle.route}/$articleId")
                    },
                    onAddArticle = {
                        navController.navigate(DestinasiUpload.route)
                    },
                    onAboutClick = {
                        navController.navigate(DestinasiAbout.route)
                    },
                    onLogout = {
                        // Saat logout, kembalikan ke Login dan hapus semua history
                        navController.navigate(DestinasiLogin.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // --- EDIT PROFILE ---
            composable("edit_profile") {
                EditProfileScreen(
                    navigateBack = { navController.popBackStack() },
                    onDeleteAccountSuccess = {
                        navController.navigate(DestinasiLogin.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(DestinasiAbout.route) {
                AboutScreen(
                    navigateBack = { navController.popBackStack() }
                )
            }

            // --- DETAIL ARTIKEL ---
            composable(
                route = DestinasiDetail.routeWithArg,
                arguments = listOf(
                    navArgument(DestinasiDetail.articleIdArg) {
                        type = NavType.IntType
                    }
                )
            ) {
                DetailArticleScreen(
                    navigateBack = { navController.popBackStack() },
                    onTagClick = { tag ->
                        val cleanTag = tag.replace("#", "")
                        navController.navigate("${DestinasiSearch.route}?query=$cleanTag") {
                            launchSingleTop = true
                        }
                    }
                )
            }

            // --- EDIT ARTIKEL ---
            composable(
                route = DestinasiEditArticle.routeWithArgs,
                arguments = listOf(
                    navArgument(DestinasiEditArticle.articleId) { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val articleId = backStackEntry.arguments?.getInt(DestinasiEditArticle.articleId) ?: 0
                EditArticleScreen(
                    articleId = articleId,
                    navigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

// --- LOGIKA BOTTOM BAR ---
data class BottomNavItem(
    val label: Int,
    val icon: ImageVector,
    val route: String
)

@Composable
fun BottomBarGrandChronicler(navController: NavHostController) {
    val items = listOf(
        BottomNavItem(R.string.menu_home, Icons.Default.Home, DestinasiHome.route),
        BottomNavItem(R.string.menu_search, Icons.Default.Search, "${DestinasiSearch.route}?query="),
        BottomNavItem(R.string.menu_upload, Icons.Default.AddCircle, DestinasiUpload.route),
        BottomNavItem(R.string.menu_profile, Icons.Default.AccountCircle, DestinasiProfile.route),
    )

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        items.forEach { item ->
            val selected = currentDestination?.route?.startsWith(item.route.substringBefore("?")) == true

            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = stringResource(item.label)) },
                label = { Text(stringResource(item.label)) },
                selected = selected,
                onClick = {
                    val targetRoute = if(item.route.contains("search")) "${DestinasiSearch.route}?query=" else item.route

                    navController.navigate(targetRoute) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}