package com.example.grandchroniclerapp.uicontroller.view.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.grandchroniclerapp.model.Article
import com.example.grandchroniclerapp.ui.theme.PastelBluePrimary
import com.example.grandchroniclerapp.ui.theme.PastelPinkSecondary
import com.example.grandchroniclerapp.ui.theme.SoftError
import com.example.grandchroniclerapp.viewmodel.profile.ProfileUiState
import com.example.grandchroniclerapp.viewmodel.profile.ProfileViewModel
import com.example.grandchroniclerapp.viewmodel.provider.PenyediaViewModel

@Composable
fun ProfileScreen(
    onEditProfile: () -> Unit,
    onEditArticle: (Int) -> Unit,
    onAddArticle: () -> Unit,
    onAboutClick: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = viewModel(factory = PenyediaViewModel.Factory)
) {
    LaunchedEffect(Unit) { viewModel.loadUserProfile() }
    val uiState = viewModel.profileUiState
    val snackbarHostState = remember { SnackbarHostState() }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var articleToDeleteId by remember { mutableStateOf<Int?>(null) }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Terbit", "Draf")

    // Handle Delete Message
    if (viewModel.deleteMessage != null) {
        val msg = viewModel.deleteMessage
        LaunchedEffect(msg) {
            snackbarHostState.showSnackbar(msg ?: "")
            viewModel.messageShown()
        }
    }

    // Dialogs (Logout & Delete) - Sama seperti sebelumnya (Saya ringkas)
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = SoftError) },
            title = { Text("Keluar Akun?", color = SoftError, fontWeight = FontWeight.Bold) },
            text = { Text("Anda harus login kembali untuk mengakses profil.") },
            confirmButton = { Button(onClick = { showLogoutDialog = false; viewModel.logout(); onLogout() }, colors = ButtonDefaults.buttonColors(containerColor = SoftError)) { Text("Ya, Keluar", color = Color.White) } },
            dismissButton = { OutlinedButton(onClick = { showLogoutDialog = false }) { Text("Batal") } }, containerColor = Color.White
        )
    }
    if (showDeleteDialog && articleToDeleteId != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hapus Artikel?", color = SoftError) }, text = { Text("Anda yakin ingin menghapus artikel ini?") },
            confirmButton = { Button(onClick = { viewModel.deleteArticle(articleToDeleteId!!); showDeleteDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = SoftError)) { Text("Hapus", color = Color.White) } },
            dismissButton = { OutlinedButton(onClick = { showDeleteDialog = false }) { Text("Batal") } }, containerColor = Color.White
        )
    }

    // --- MAIN UI (SHEET DESIGN) ---
    Box(modifier = Modifier.fillMaxSize().background(PastelBluePrimary)) {

        // 1. KONTEN (LAYER BAWAH)
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(80.dp)) // Ruang untuk Header

            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
                color = Color.White
            ) {
                // ISI PROFIL
                when (uiState) {
                    is ProfileUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = PastelBluePrimary) }
                    is ProfileUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(uiState.message, color = SoftError) }
                    is ProfileUiState.Success -> {
                        val user = uiState.user
                        val articles = if (selectedTabIndex == 0) uiState.articles.filter { it.status == "Published" } else uiState.articles.filter { it.status == "Draft" }

                        Column(modifier = Modifier.fillMaxSize()) {
                            // Info User (Scrollable bersama list atau fixed di atas list)
                            // Agar rapi, kita masukkan ke LazyColumn sebagai item pertama
                            LazyColumn(contentPadding = PaddingValues(bottom = 100.dp)) {
                                item {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier.size(90.dp).clip(CircleShape).background(PastelBluePrimary.copy(0.1f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(user.full_name.take(1).uppercase(), style = MaterialTheme.typography.displayMedium, color = PastelBluePrimary, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(Modifier.height(12.dp))
                                        Text(user.full_name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                        Text(user.email, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                                        if (!user.bio.isNullOrBlank()) {
                                            Spacer(Modifier.height(8.dp))
                                            Text(user.bio, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp))
                                        }
                                        Spacer(Modifier.height(16.dp))
                                        OutlinedButton(onClick = onEditProfile, shape = RoundedCornerShape(20.dp), border = androidx.compose.foundation.BorderStroke(1.dp, PastelBluePrimary)) {
                                            Icon(Icons.Default.Edit, null, Modifier.size(16.dp), tint = PastelBluePrimary)
                                            Spacer(Modifier.width(8.dp))
                                            Text("Edit Profil", color = PastelBluePrimary)
                                        }
                                    }
                                }

                                // Tabs
                                stickyHeader {
                                    TabRow(selectedTabIndex = selectedTabIndex, containerColor = Color.White, contentColor = PastelBluePrimary) {
                                        tabs.forEachIndexed { index, title ->
                                            Tab(selected = selectedTabIndex == index, onClick = { selectedTabIndex = index }, text = { Text(title, fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal) })
                                        }
                                    }
                                }

                                // List Items
                                if (articles.isEmpty()) {
                                    item {
                                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                            Text("Belum ada artikel.", color = Color.Gray)
                                        }
                                    }
                                } else {
                                    items(articles) { article ->
                                        MyArticleItem(article, { onEditArticle(article.article_id) }, { articleToDeleteId = article.article_id; showDeleteDialog = true })
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. HEADER (TOP BAR CUSTOM)
        Row(
            modifier = Modifier.fillMaxWidth().height(80.dp).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween // Kiri Kanan
        ) {
            // Icon About (Kiri)
            IconButton(onClick = onAboutClick) {
                Icon(Icons.Default.Info, contentDescription = "About", tint = Color.White)
            }

            // Judul Tengah
            Text("Profil Saya", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)

            // Icon Logout (Kanan)
            IconButton(onClick = { showLogoutDialog = true }) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = Color.White) // Putih biar kontras di background biru
            }
        }

        // 3. FAB & SNACKBAR
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.BottomEnd) {
            FloatingActionButton(onClick = onAddArticle, containerColor = PastelBluePrimary, contentColor = Color.White, shape = CircleShape) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            SnackbarHost(hostState = snackbarHostState)
        }
    }
}

// (Fungsi MyArticleItem sama seperti sebelumnya, tidak perlu diubah)
@Composable
fun MyArticleItem(article: Article, onEdit: () -> Unit, onDelete: () -> Unit) {
    // ... (Gunakan kode MyArticleItem yang terakhir kamu punya)
    val thumbnail = article.image ?: article.images.firstOrNull()
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            if (thumbnail != null) {
                Image(painter = rememberAsyncImagePainter(thumbnail), contentDescription = null, modifier = Modifier.size(70.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
            } else {
                Box(modifier = Modifier.size(70.dp).clip(RoundedCornerShape(8.dp)).background(Color.LightGray))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(article.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(if (article.status == "Draft") "DRAF" else "Terbit", style = MaterialTheme.typography.labelSmall, color = if (article.status == "Draft") Color(0xFFE65100) else Color.Gray)
            }
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, null, tint = PastelBluePrimary) }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = SoftError) }
        }
    }
}