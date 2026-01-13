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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.grandchroniclerapp.ui.theme.PastelBluePrimary
import com.example.grandchroniclerapp.ui.theme.SoftError
import com.example.grandchroniclerapp.viewmodel.article.InsertUiState
import com.example.grandchroniclerapp.viewmodel.article.InsertViewModel
import com.example.grandchroniclerapp.viewmodel.provider.PenyediaViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsertArticleScreen(
    navigateBack: () -> Unit,
    viewModel: InsertViewModel = viewModel(factory = PenyediaViewModel.Factory)
) {
    val uiState = viewModel.uiState
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var showDraftConfirmDialog by remember { mutableStateOf(false) }
    var showPublishConfirmDialog by remember { mutableStateOf(false) }
    var imageToDeleteUri by remember { mutableStateOf<Uri?>(null) }
    var expanded by remember { mutableStateOf(false) }

    val multipleImagePicker = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetMultipleContents()) { uris -> viewModel.addImages(uris) }

    fun onBackAttempt() { if (viewModel.hasUnsavedChanges()) showDiscardDialog = true else navigateBack() }
    BackHandler { onBackAttempt() }

    LaunchedEffect(uiState) {
        if (uiState is InsertUiState.Success) {
            this.launch { snackbarHostState.showSnackbar("Berhasil Disimpan!") }
            delay(1500)
            viewModel.resetState()
            navigateBack()
        } else if (uiState is InsertUiState.Error) { snackbarHostState.showSnackbar(uiState.message) }
    }

    // Dialogs (Sama seperti sebelumnya, diringkas)
    if (showDiscardDialog) { AlertDialog(onDismissRequest = { showDiscardDialog = false }, title = { Text("Keluar?", color = SoftError) }, text = { Text("Tulisan belum disimpan.") }, confirmButton = { Button(onClick = { showDiscardDialog = false; navigateBack() }, colors = ButtonDefaults.buttonColors(containerColor = SoftError)) { Text("Keluar") } }, dismissButton = { OutlinedButton(onClick = { showDiscardDialog = false }) { Text("Batal") } }) }
    if (showPublishConfirmDialog) { AlertDialog(onDismissRequest = { showPublishConfirmDialog = false }, title = { Text("Terbitkan?") }, text = { Text("Publik bisa melihat ini.") }, confirmButton = { Button(onClick = { showPublishConfirmDialog = false; viewModel.submitArticle(context, "Published") }) { Text("Terbit") } }, dismissButton = { TextButton(onClick = { showPublishConfirmDialog = false }) { Text("Batal") } }) }

    // MAIN UI: SHEET DESIGN
    Box(modifier = Modifier.fillMaxSize().background(PastelBluePrimary)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(80.dp))
            Surface(modifier = Modifier.fillMaxSize(), shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp), color = Color.White) {
                Column(modifier = Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState())) {

                    // FOTO (Optional)
                    Text("Foto Artikel (Opsional)", fontWeight = FontWeight.Bold, color = if(viewModel.selectedImageUris.isEmpty()) Color.Gray else PastelBluePrimary)
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth().height(120.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(12.dp)).background(Color(0xFFF5F7FA)).border(1.dp, Color.LightGray, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                            if (viewModel.selectedImageUris.isEmpty()) Text("No Foto", color = Color.Gray)
                            else LazyRow(modifier = Modifier.fillMaxSize().padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(viewModel.selectedImageUris) { uri ->
                                    Box(modifier = Modifier.width(90.dp).fillMaxHeight()) {
                                        Image(painter = rememberAsyncImagePainter(uri), contentDescription = null, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                                        IconButton(onClick = { imageToDeleteUri = uri }, modifier = Modifier.align(Alignment.TopEnd).padding(2.dp).size(20.dp).background(Color.White, CircleShape)) { Icon(Icons.Default.Close, null, tint = SoftError, modifier = Modifier.size(12.dp)) }
                                    }
                                }
                            }
                        }
                        Box(modifier = Modifier.size(120.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFE0E0E0)).clickable { multipleImagePicker.launch("image/*") }, contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.AddPhotoAlternate, null, tint = PastelBluePrimary); Text("Add", color = PastelBluePrimary, fontSize = 12.sp) }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    OutlinedTextField(value = viewModel.title, onValueChange = { viewModel.updateTitle(it) }, label = { Text("Judul") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    Spacer(modifier = Modifier.height(16.dp))

                    // Category Dropdown
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                        OutlinedTextField(value = viewModel.selectedCategory?.category_name ?: "Pilih Kategori", onValueChange = {}, readOnly = true, label = { Text("Kategori") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.menuAnchor().fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(Color.White)) {
                            viewModel.categories.forEach { category -> DropdownMenuItem(text = { Text(category.category_name) }, onClick = { viewModel.updateCategory(category); expanded = false }) }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(value = viewModel.content, onValueChange = { viewModel.updateContent(it) }, label = { Text("Isi Artikel...") }, modifier = Modifier.fillMaxWidth().height(300.dp), shape = RoundedCornerShape(12.dp))
                    Spacer(modifier = Modifier.height(24.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = { viewModel.submitArticle(context, "Draft") }, modifier = Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(12.dp)) { Text("Draf") }
                        Button(onClick = { showPublishConfirmDialog = true }, modifier = Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = PastelBluePrimary)) { Text("Terbit") }
                    }
                    Spacer(modifier = Modifier.height(50.dp))
                }
            }
        }

        // CUSTOM TOP BAR
        Row(modifier = Modifier.fillMaxWidth().height(80.dp).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onBackAttempt() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White) }
            Text("Tulis Artikel", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(start = 16.dp))
        }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) { SnackbarHost(hostState = snackbarHostState) }
    }
}