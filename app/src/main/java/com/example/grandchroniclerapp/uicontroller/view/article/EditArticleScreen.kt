package com.example.grandchroniclerapp.uicontroller.view.article

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import coil.compose.rememberAsyncImagePainter
import com.example.grandchroniclerapp.ui.theme.SoftError
import com.example.grandchroniclerapp.ui.theme.PastelBluePrimary
import com.example.grandchroniclerapp.ui.theme.PastelPinkSecondary
import com.example.grandchroniclerapp.viewmodel.article.EditArticleViewModel
import com.example.grandchroniclerapp.viewmodel.article.UploadUiState
import com.example.grandchroniclerapp.viewmodel.provider.PenyediaViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditArticleScreen(
    articleId: Int,
    navigateBack: () -> Unit,
    viewModel: EditArticleViewModel = viewModel(factory = PenyediaViewModel.Factory)
) {
    LaunchedEffect(articleId) { viewModel.loadArticleData(articleId) }

    val uiState = viewModel.uiState
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showDiscardDialog by remember { mutableStateOf(false) }
    var showDraftConfirmDialog by remember { mutableStateOf(false) }
    var showPublishConfirmDialog by remember { mutableStateOf(false) }

    // State Dialog Hapus
    var imageToDeleteUrl by remember { mutableStateOf<String?>(null) }
    var newImageToDeleteUri by remember { mutableStateOf<Uri?>(null) }
    var expanded by remember { mutableStateOf(false) }

    val multipleImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> -> viewModel.updateImages(uris) }

    fun onBackAttempt() {
        if (viewModel.hasChanges()) showDiscardDialog = true else navigateBack()
    }

    BackHandler { onBackAttempt() }

    LaunchedEffect(uiState) {
        if (uiState is UploadUiState.Success) {
            this.launch { snackbarHostState.showSnackbar("Artikel Berhasil Diperbarui!") }
            delay(1500)
            navigateBack()
        } else if (uiState is UploadUiState.Error) {
            snackbarHostState.showSnackbar(uiState.message)
        }
    }

    // --- DIALOGS (Sama seperti sebelumnya) ---
    if (imageToDeleteUrl != null) {
        AlertDialog(
            onDismissRequest = { imageToDeleteUrl = null },
            title = { Text("Hapus Gambar?", color = SoftError, fontWeight = FontWeight.Bold) },
            text = { Text("Gambar ini akan dihapus permanen saat disimpan.") },
            confirmButton = { Button(onClick = { viewModel.deleteOldImage(imageToDeleteUrl!!); imageToDeleteUrl = null }, colors = ButtonDefaults.buttonColors(containerColor = SoftError)) { Text("Hapus", color = Color.White) } },
            dismissButton = { OutlinedButton(onClick = { imageToDeleteUrl = null }) { Text("Batal") } }, containerColor = Color.White
        )
    }
    if (newImageToDeleteUri != null) {
        AlertDialog(
            onDismissRequest = { newImageToDeleteUri = null },
            title = { Text("Batalkan Upload?", color = SoftError) }, text = { Text("Hapus gambar ini dari daftar?") },
            confirmButton = { Button(onClick = { viewModel.removeNewImage(newImageToDeleteUri!!); newImageToDeleteUri = null }, colors = ButtonDefaults.buttonColors(containerColor = SoftError)) { Text("Hapus") } },
            dismissButton = { OutlinedButton(onClick = { newImageToDeleteUri = null }) { Text("Batal") } }, containerColor = Color.White
        )
    }
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Keluar?", color = SoftError) }, text = { Text("Perubahan belum disimpan.") },
            confirmButton = { Button(onClick = { showDiscardDialog = false; navigateBack() }, colors = ButtonDefaults.buttonColors(containerColor = SoftError)) { Text("Keluar", color = Color.White) } },
            dismissButton = { OutlinedButton(onClick = { showDiscardDialog = false }) { Text("Lanjut") } }, containerColor = Color.White
        )
    }
    if (showDraftConfirmDialog) {
        AlertDialog(onDismissRequest = { showDraftConfirmDialog = false }, title = { Text("Simpan Draf?") }, text = { Text("Simpan sebagai draf.") }, confirmButton = { Button(onClick = { showDraftConfirmDialog = false; viewModel.submitUpdate(context, articleId, "Draft") }) { Text("Simpan") } }, dismissButton = { TextButton(onClick = { showDraftConfirmDialog = false }) { Text("Batal") } }, containerColor = Color.White)
    }
    if (showPublishConfirmDialog) {
        AlertDialog(onDismissRequest = { showPublishConfirmDialog = false }, title = { Text("Update Artikel?") }, text = { Text("Perbarui ke publik.") }, confirmButton = { Button(onClick = { showPublishConfirmDialog = false; viewModel.submitUpdate(context, articleId, "Published") }) { Text("Update") } }, dismissButton = { TextButton(onClick = { showPublishConfirmDialog = false }) { Text("Batal") } }, containerColor = Color.White)
    }

    // --- MAIN LAYOUT (SHEET DESIGN) ---
    Box(
        modifier = Modifier.fillMaxSize().background(PastelBluePrimary)
    ) {
        // 1. KONTEN (LAYER BAWAH - KERTAS PUTIH)
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(80.dp)) // Ruang untuk Header

            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(10.dp))

                    // --- AREA GAMBAR ---
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Gambar Artikel", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.width(8.dp))
                            if (viewModel.totalImagesCount == 0) Text("(Opsional)", fontSize = 12.sp, color = Color.Gray)
                            else Text("(${viewModel.totalImagesCount} Total)", fontSize = 12.sp, color = PastelBluePrimary)
                        }
                        Spacer(Modifier.height(8.dp))

                        // Gambar Lama
                        if (viewModel.oldImageUrls.isNotEmpty()) {
                            Text("Tersimpan:", fontSize = 12.sp, color = Color.Gray)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().height(100.dp).padding(vertical = 4.dp)) {
                                items(viewModel.oldImageUrls) { imageUrl ->
                                    Box(modifier = Modifier.width(100.dp).fillMaxHeight()) {
                                        Image(painter = rememberAsyncImagePainter(imageUrl), contentDescription = null, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)).border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                                        IconButton(onClick = { imageToDeleteUrl = imageUrl }, modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(24.dp).background(Color.White.copy(0.9f), CircleShape)) { Icon(Icons.Default.Delete, null, tint = SoftError, modifier = Modifier.size(14.dp)) }
                                    }
                                }
                            }
                        }

                        // Gambar Baru + Tombol Tambah
                        Text("Tambah Baru:", fontSize = 12.sp, color = Color.Gray)
                        Row(modifier = Modifier.fillMaxWidth().height(120.dp).padding(top=4.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(12.dp)).background(Color(0xFFF5F7FA)).border(1.dp, Color.LightGray, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                if (viewModel.newImageUris.isNotEmpty()) {
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize().padding(8.dp)) {
                                        items(viewModel.newImageUris) { uri ->
                                            Box(modifier = Modifier.width(90.dp).fillMaxHeight()) {
                                                Image(painter = rememberAsyncImagePainter(uri), contentDescription = null, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
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
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // --- FORM INPUT ---
                    OutlinedTextField(value = viewModel.title, onValueChange = { viewModel.updateTitle(it) }, label = { Text("Judul") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    Spacer(modifier = Modifier.height(16.dp))

                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                        OutlinedTextField(value = viewModel.selectedCategory?.category_name ?: "Pilih Kategori", onValueChange = {}, readOnly = true, label = { Text("Kategori") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.menuAnchor().fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(Color.White)) {
                            viewModel.categories.forEach { category -> DropdownMenuItem(text = { Text(category.category_name) }, onClick = { viewModel.updateCategory(category); expanded = false }) }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(value = viewModel.content, onValueChange = { viewModel.updateContent(it) }, label = { Text("Isi Artikel") }, modifier = Modifier.fillMaxWidth().height(300.dp), shape = RoundedCornerShape(12.dp))
                    Spacer(modifier = Modifier.height(24.dp))

                    // --- TOMBOL ---
                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 50.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = { showDraftConfirmDialog = true }, modifier = Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(12.dp), border = androidx.compose.foundation.BorderStroke(1.dp, PastelBluePrimary), enabled = uiState !is UploadUiState.Loading) { Text("Simpan Draf", color = PastelBluePrimary, fontWeight = FontWeight.Bold) }
                        Button(onClick = { showPublishConfirmDialog = true }, modifier = Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = PastelBluePrimary), enabled = uiState !is UploadUiState.Loading) { if (uiState is UploadUiState.Loading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp)) else Text("Update", fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }

        // 2. HEADER CUSTOM (LAYER ATAS)
        Row(modifier = Modifier.fillMaxWidth().height(80.dp).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onBackAttempt() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = Color.White) }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Edit Artikel", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
        }

        // 3. SNACKBAR
        Box(modifier = Modifier.fillMaxSize().padding(bottom = 20.dp), contentAlignment = Alignment.BottomCenter) {
            SnackbarHost(hostState = snackbarHostState) { data ->
                val isSuccess = data.visuals.message.contains("Berhasil", true)
                if (isSuccess) {
                    Box(modifier = Modifier.padding(16.dp).fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Brush.horizontalGradient(listOf(PastelBluePrimary, PastelPinkSecondary))).padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.CheckCircle, null, tint = Color.White); Spacer(Modifier.width(12.dp)); Text(text = data.visuals.message, color = Color.White, fontWeight = FontWeight.Bold) }
                    }
                } else { Snackbar(containerColor = SoftError, contentColor = Color.White, snackbarData = data) }
            }
        }
    }
}