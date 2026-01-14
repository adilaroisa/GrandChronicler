package com.example.grandchroniclerapp.uicontroller.view.profile

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.grandchroniclerapp.ui.theme.PastelBluePrimary
import com.example.grandchroniclerapp.ui.theme.PastelPinkSecondary
import com.example.grandchroniclerapp.ui.theme.SoftError
import com.example.grandchroniclerapp.viewmodel.profile.EditProfileUiState
import com.example.grandchroniclerapp.viewmodel.profile.EditProfileViewModel
import com.example.grandchroniclerapp.viewmodel.provider.PenyediaViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navigateBack: () -> Unit,
    onDeleteAccountSuccess: () -> Unit,
    viewModel: EditProfileViewModel = viewModel(factory = PenyediaViewModel.Factory)
) {
    val uiState = viewModel.uiState
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> viewModel.selectedImageUri = uri }
    )

    var showDiscardDialog by remember { mutableStateOf(false) }
    var showSaveConfirmDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    var showErrors by remember { mutableStateOf(false) }

    // VALIDASI NAMA: HANYA HURUF, SPASI, TITIK
    val isNameValid = viewModel.fullName.isNotBlank() && viewModel.fullName.matches(Regex("^[a-zA-Z .]*$"))

    // VALIDASI PASSWORD (JIKA DIISI)
    val pass = viewModel.password
    val hasLetter = pass.any { it.isLetter() }
    val hasDigit = pass.any { it.isDigit() }
    val isPasswordValid = pass.isEmpty() || (pass.length >= 8 && hasLetter && hasDigit)

    fun onBackAttempt() {
        if (viewModel.hasChanges()) showDiscardDialog = true else navigateBack()
    }
    BackHandler { onBackAttempt() }

    LaunchedEffect(uiState) {
        when (uiState) {
            is EditProfileUiState.Success -> {
                scope.launch { snackbarHostState.showSnackbar("Profil Berhasil Diperbarui!") }
                delay(1500)
                navigateBack()
            }
            is EditProfileUiState.DeleteSuccess -> {
                scope.launch { snackbarHostState.showSnackbar("Akun Berhasil Dihapus") }
                delay(1500)
                onDeleteAccountSuccess()
            }
            is EditProfileUiState.Error -> {
                snackbarHostState.showSnackbar(uiState.message)
            }
            else -> {}
        }
    }

    // --- DIALOGS ---
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Batalkan?", color = SoftError) },
            text = { Text("Perubahan belum disimpan. Yakin ingin keluar?") },
            confirmButton = { Button(onClick = { showDiscardDialog = false; navigateBack() }, colors = ButtonDefaults.buttonColors(containerColor = SoftError)) { Text("Keluar", color = Color.White) } },
            dismissButton = { OutlinedButton(onClick = { showDiscardDialog = false }) { Text("Batal") } }
        )
    }
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("Hapus Akun Permanen?") },
            text = { Text("Tindakan ini tidak dapat dibatalkan.") },
            confirmButton = { Button(onClick = { showDeleteAccountDialog = false; viewModel.deleteAccount() }, colors = ButtonDefaults.buttonColors(containerColor = SoftError)) { Text("Hapus Akun", color = Color.White) } },
            dismissButton = { OutlinedButton(onClick = { showDeleteAccountDialog = false }) { Text("Batal") } }
        )
    }
    if (showSaveConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showSaveConfirmDialog = false },
            title = { Text("Simpan Profil?") },
            text = { Text("Yakin ingin memperbarui data profil Anda?") },
            confirmButton = { Button(onClick = { showSaveConfirmDialog = false; viewModel.submitUpdate(context) }, colors = ButtonDefaults.buttonColors(containerColor = PastelBluePrimary)) { Text("Ya, Simpan") } },
            dismissButton = { TextButton(onClick = { showSaveConfirmDialog = false }) { Text("Batal") } }
        )
    }

    // LAYOUT
    Box(modifier = Modifier.fillMaxSize().background(PastelBluePrimary)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(80.dp))
            Surface(modifier = Modifier.fillMaxSize(), shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp), color = Color.White) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // --- FOTO PROFIL ---
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Box(modifier = Modifier.size(110.dp).clip(CircleShape).background(Color.LightGray.copy(alpha = 0.3f)).clickable { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, contentAlignment = Alignment.Center) {
                            if (viewModel.selectedImageUri != null) {
                                AsyncImage(model = viewModel.selectedImageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                            } else if (viewModel.currentPhotoUrl.isNotEmpty()) {
                                AsyncImage(model = "http://10.0.2.2:3000/uploads/${viewModel.currentPhotoUrl}", contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                            } else {
                                Icon(Icons.Default.Person, null, modifier = Modifier.size(60.dp), tint = Color.Gray)
                            }
                        }
                        Surface(shape = CircleShape, color = PastelBluePrimary, modifier = Modifier.size(32.dp).padding(4.dp)) { Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.padding(4.dp)) }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Fields
                    OutlinedTextField(value = viewModel.role, onValueChange = {}, label = { Text("Status Akun") }, leadingIcon = { Icon(Icons.Default.Badge, null) }, modifier = Modifier.fillMaxWidth(), readOnly = true, enabled = false, shape = RoundedCornerShape(12.dp))

                    Spacer(modifier = Modifier.height(16.dp))

                    // NAMA
                    OutlinedTextField(
                        value = viewModel.fullName,
                        onValueChange = { viewModel.fullName = it },
                        label = { Text("Nama Lengkap") },
                        leadingIcon = { Icon(Icons.Default.Person, null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        isError = showErrors && !isNameValid
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(value = viewModel.email, onValueChange = { viewModel.email = it }, label = { Text("Email") }, leadingIcon = { Icon(Icons.Default.Email, null) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))

                    Spacer(modifier = Modifier.height(16.dp))

                    // PASSWORD FIELD
                    OutlinedTextField(
                        value = viewModel.password,
                        onValueChange = { viewModel.password = it },
                        label = { Text("Password Baru (Opsional)") },
                        leadingIcon = { Icon(Icons.Default.Lock, null) },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(12.dp),
                        isError = showErrors && !isPasswordValid,
                        trailingIcon = { IconButton(onClick = { passwordVisible = !passwordVisible }) { Icon(if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, null) } }
                    )

                    // Helper Text
                    if (showErrors && !isPasswordValid) {
                        Text("Min. 8 karakter, huruf & angka", style = MaterialTheme.typography.labelSmall, color = SoftError, modifier = Modifier.align(Alignment.Start))
                    } else {
                        Text("Min. 8 karakter, huruf & angka (Jika diisi)", style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.align(Alignment.Start))
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(value = viewModel.bio, onValueChange = { viewModel.bio = it }, label = { Text("Bio Singkat") }, modifier = Modifier.fillMaxWidth().height(120.dp), shape = RoundedCornerShape(12.dp))

                    Spacer(modifier = Modifier.height(32.dp))

                    // --- TOMBOL SIMPAN ---
                    Button(
                        onClick = {
                            showErrors = true
                            if (viewModel.fullName.isBlank() || viewModel.email.isBlank()) {
                                scope.launch { snackbarHostState.showSnackbar("Nama dan Email wajib diisi") }
                            } else if (!isNameValid) {
                                scope.launch { snackbarHostState.showSnackbar("Nama hanya boleh huruf dan titik (.)") }
                            } else if (!isPasswordValid) {
                                scope.launch { snackbarHostState.showSnackbar("Password tidak memenuhi syarat keamanan") }
                            } else {
                                showSaveConfirmDialog = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PastelBluePrimary),
                        enabled = uiState !is EditProfileUiState.Loading
                    ) {
                        if (uiState is EditProfileUiState.Loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Icon(Icons.Default.Save, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Simpan Perubahan", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Divider(color = Color.LightGray.copy(alpha = 0.5f))

                    // --- TOMBOL HAPUS AKUN DI BAWAH DIVIDER ---
                    TextButton(
                        onClick = { showDeleteAccountDialog = true },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        colors = ButtonDefaults.textButtonColors(contentColor = SoftError)
                    ) {
                        Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Hapus Akun Permanen")
                    }

                    Spacer(modifier = Modifier.height(50.dp))
                }
            }
        }

        // Header
        Row(modifier = Modifier.fillMaxWidth().height(80.dp).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onBackAttempt() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White) }
            Text("Edit Profil", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
        }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            SnackbarHost(hostState = snackbarHostState) { data ->
                val isSuccess = data.visuals.message.contains("Berhasil", true)
                val bgColor = if (isSuccess) Brush.horizontalGradient(listOf(PastelBluePrimary, PastelPinkSecondary)) else Brush.linearGradient(listOf(SoftError, SoftError))
                Box(modifier = Modifier.padding(16.dp).fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(bgColor).padding(16.dp)) {
                    Text(data.visuals.message, color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                }
            }
        }
    }
}