package com.example.grandchroniclerapp.uicontroller.view.article

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.grandchroniclerapp.ui.theme.PastelBluePrimary
import com.example.grandchroniclerapp.ui.theme.PastelPinkSecondary
import com.example.grandchroniclerapp.ui.theme.SoftError
import com.example.grandchroniclerapp.viewmodel.article.EditArticleViewModel
import com.example.grandchroniclerapp.viewmodel.article.UploadUiState
import com.example.grandchroniclerapp.viewmodel.provider.PenyediaViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditArticleScreen(
    articleId: Int,
    navigateBack: () -> Unit,
    viewModel: EditArticleViewModel = viewModel(factory = PenyediaViewModel.Factory)
) {
    // Load Data saat pertama kali dibuka
    LaunchedEffect(articleId) { viewModel.loadArticleData(articleId) }

    val uiState = viewModel.uiState
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showDiscardDialog by remember { mutableStateOf(false) }
    var showDraftConfirmDialog by remember { mutableStateOf(false) }
    var showPublishConfirmDialog by remember { mutableStateOf(false) }
    var imageToDeleteUrl by remember { mutableStateOf<String?>(null) }
    var newImageToDeleteUri by remember { mutableStateOf<Uri?>(null) }
    var expanded by remember { mutableStateOf(false) }

    val multipleImagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { viewModel.updateImages(it) }

    fun onBackAttempt() { if (viewModel.hasChanges()) showDiscardDialog = true else navigateBack() }
    BackHandler { onBackAttempt() }

    LaunchedEffect(Unit) {
        viewModel.snackbarEvent.collectLatest { message -> snackbarHostState.showSnackbar(message) }
    }

    LaunchedEffect(uiState) {
        if (uiState is UploadUiState.Success) {
            delay(1000)
            navigateBack()
        }
    }

    // --- DIALOGS ---
    if (imageToDeleteUrl != null) {
        AlertDialog(onDismissRequest = { imageToDeleteUrl = null },
            title = { Text("Hapus Gambar?", color = SoftError) },
            confirmButton = { Button(onClick = { viewModel.deleteOldImage(imageToDeleteUrl!!); imageToDeleteUrl = null }, colors = ButtonDefaults.buttonColors(containerColor = SoftError)) { Text("Hapus") } },
            dismissButton = { OutlinedButton(onClick = { imageToDeleteUrl = null }) { Text("Batal") } })
    }
    if (newImageToDeleteUri != null) {
        AlertDialog(onDismissRequest = { newImageToDeleteUri = null },
            title = { Text("Hapus Upload?", color = SoftError) },
            confirmButton = { Button(onClick = { viewModel.removeNewImage(newImageToDeleteUri!!); newImageToDeleteUri = null }, colors = ButtonDefaults.buttonColors(containerColor = SoftError)) { Text("Hapus") } },
            dismissButton = { OutlinedButton(onClick = { newImageToDeleteUri = null }) { Text("Batal") } })
    }
    if (showDiscardDialog) {
        AlertDialog(onDismissRequest = { showDiscardDialog = false },
            title = { Text("Keluar?", color = SoftError) },
            text = { Text("Perubahan belum disimpan.") },
            confirmButton = { Button(onClick = { showDiscardDialog = false; navigateBack() }, colors = ButtonDefaults.buttonColors(containerColor = SoftError)) { Text("Keluar") } },
            dismissButton = { OutlinedButton(onClick = { showDiscardDialog = false }) { Text("Lanjut") } })
    }
    if (showPublishConfirmDialog) {
        AlertDialog(onDismissRequest = { showPublishConfirmDialog = false },
            title = { Text("Update Artikel?") },
            text = { Text("Artikel akan diperbarui sesuai perubahan terbaru.") },
            confirmButton = { Button(onClick = { showPublishConfirmDialog = false; viewModel.submitUpdate(context, articleId, "Published") }) { Text("Update") } },
            dismissButton = { TextButton(onClick = { showPublishConfirmDialog = false }) { Text("Batal") } })
    }
    if (showDraftConfirmDialog) {
        AlertDialog(onDismissRequest = { showDraftConfirmDialog = false },
            title = { Text("Simpan Draf?") },
            text = { Text("Simpan perubahan sebagai draf.") },
            confirmButton = { Button(onClick = { showDraftConfirmDialog = false; viewModel.submitUpdate(context, articleId, "Draft") }) { Text("Simpan") } },
            dismissButton = { TextButton(onClick = { showDraftConfirmDialog = false }) { Text("Batal") } })
    }

    // MAIN UI
    Box(modifier = Modifier.fillMaxSize().background(PastelBluePrimary)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(80.dp))
            Surface(modifier = Modifier.fillMaxSize(), shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp), color = Color.White) {
                Column(modifier = Modifier.fillMaxSize().padding(20.dp).verticalScroll(scrollState)) {

                    // 1. GAMBAR (LAMA & BARU)
                    if (viewModel.oldImageUrls.isNotEmpty()) {
                        Text("Gambar Lama:", fontSize = 12.sp, color = Color.Gray)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.height(100.dp)) {
                            items(viewModel.oldImageUrls) { url ->
                                val finalUrl = if (url.startsWith("http")) url else "http://10.0.2.2:3000/uploads/$url"
                                Box(modifier = Modifier.width(100.dp)) {
                                    AsyncImage(model = finalUrl, contentDescription = null, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                                    IconButton(onClick = { imageToDeleteUrl = url }, modifier = Modifier.align(Alignment.TopEnd).background(Color.White, CircleShape).size(24.dp)) { Icon(Icons.Default.Delete, null, tint = SoftError, modifier = Modifier.size(14.dp)) }
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }

                    Text("Tambah Baru:", fontSize = 12.sp, color = Color.Gray)
                    Row(modifier = Modifier.height(120.dp).padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(12.dp)).background(Color(0xFFF5F7FA)).border(1.dp, Color.LightGray, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                            if (viewModel.newImageUris.isNotEmpty()) {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize().padding(8.dp)) {
                                    items(viewModel.newImageUris) { uri ->
                                        Box(modifier = Modifier.width(90.dp)) {
                                            AsyncImage(model = uri, contentDescription = null, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                                            IconButton(onClick = { newImageToDeleteUri = uri }, modifier = Modifier.align(Alignment.TopEnd).padding(2.dp).size(20.dp).background(Color.White, CircleShape)) { Icon(Icons.Default.Close, null, tint = SoftError, modifier = Modifier.size(12.dp)) }
                                        }
                                    }
                                }
                            } else { Text("Belum ada foto baru", color = Color.Gray, fontSize = 12.sp) }
                        }
                        Box(modifier = Modifier.size(120.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFE0E0E0)).clickable { multipleImagePicker.launch("image/*") }, contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.AddPhotoAlternate, null, tint = PastelBluePrimary); Text("Add", color = PastelBluePrimary, fontSize = 12.sp) }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // 2. FORM JUDUL
                    OutlinedTextField(
                        value = viewModel.title, onValueChange = { viewModel.updateTitle(it) },
                        label = { Text("Judul") }, modifier = Modifier.fillMaxWidth(),
                        isError = viewModel.title.isBlank() && uiState is UploadUiState.Error
                    )
                    Spacer(Modifier.height(16.dp))

                    // 3. FORM KATEGORI
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                        OutlinedTextField(
                            value = viewModel.selectedCategory?.category_name ?: "Pilih Kategori",
                            onValueChange = {}, readOnly = true, label = { Text("Kategori") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.menuAnchor().fillMaxWidth(),
                            isError = viewModel.selectedCategory == null && uiState is UploadUiState.Error
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            viewModel.categories.forEach { category -> DropdownMenuItem(text = { Text(category.category_name) }, onClick = { viewModel.updateCategory(category); expanded = false }) }
                        }
                    }
                    Spacer(Modifier.height(16.dp))

                    // 4. FORM TAGS (DITAMBAHKAN DI SINI)
                    OutlinedTextField(
                        value = viewModel.tags, // Bind ke ViewModel
                        onValueChange = { viewModel.updateTags(it) }, // Update ViewModel
                        label = { Text("Tags / Hashtag (Opsional)") },
                        placeholder = { Text("Contoh: #Sejarah #Budaya") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(16.dp))

                    // 5. FORM KONTEN
                    OutlinedTextField(
                        value = viewModel.content, onValueChange = { viewModel.updateContent(it) },
                        label = { Text("Isi Artikel") }, modifier = Modifier.fillMaxWidth().height(300.dp),
                        isError = viewModel.content.isBlank() && uiState is UploadUiState.Error
                    )
                    Spacer(Modifier.height(24.dp))

                    // 6. TOMBOL AKSI
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = { if (viewModel.title.isNotBlank()) showDraftConfirmDialog = true else viewModel.submitUpdate(context, articleId, "Draft") },
                            modifier = Modifier.weight(1f).height(50.dp), enabled = uiState !is UploadUiState.Loading
                        ) { Text("Simpan Draf") }

                        Button(
                            onClick = { if (viewModel.title.isNotBlank() && viewModel.content.isNotBlank() && viewModel.selectedCategory != null) showPublishConfirmDialog = true else viewModel.submitUpdate(context, articleId, "Published") },
                            modifier = Modifier.weight(1f).height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = PastelBluePrimary), enabled = uiState !is UploadUiState.Loading
                        ) { if (uiState is UploadUiState.Loading) CircularProgressIndicator(color = Color.White) else Text("Update") }
                    }
                    Spacer(modifier = Modifier.height(50.dp))
                }
            }
        }

        // HEADER
        Row(Modifier.fillMaxWidth().height(80.dp).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onBackAttempt() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White) }
            Text("Edit Artikel", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        // SNACKBAR
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            SnackbarHost(hostState = snackbarHostState) { data ->
                val isSuccess = data.visuals.message.contains("Berhasil", true) || data.visuals.message.contains("Update", true)
                val bgColor = if (isSuccess) Brush.horizontalGradient(listOf(PastelBluePrimary, PastelPinkSecondary)) else Brush.linearGradient(listOf(SoftError, SoftError))

                Box(modifier = Modifier.padding(16.dp).fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(bgColor).padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Close, null, tint = Color.White)
                        Spacer(Modifier.width(12.dp))
                        Text(data.visuals.message, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}