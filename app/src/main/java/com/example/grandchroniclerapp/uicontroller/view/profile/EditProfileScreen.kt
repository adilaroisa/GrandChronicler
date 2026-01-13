package com.example.grandchroniclerapp.uicontroller.view.profile

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.grandchroniclerapp.R
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

    // State Dialog & Visibility
    var showDiscardDialog by remember { mutableStateOf(false) }
    var showSaveConfirmDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    // --- FUNGSI VALIDASI ---
    fun isNameValid(name: String): Boolean = Regex("^[a-zA-Z0-9 ]*$").matches(name)
    fun isEmailValid(email: String): Boolean = email.contains("@")

    // Fungsi Navigasi Aman
    fun onBackAttempt() {
        if (viewModel.hasChanges()) showDiscardDialog = true else navigateBack()
    }
    BackHandler { onBackAttempt() }

    // Handle UI State
    LaunchedEffect(uiState) {
        when (uiState) {
            is EditProfileUiState.Success -> {
                scope.launch { snackbarHostState.showSnackbar("Profil Berhasil Diperbarui!") }
                delay(1500)
                navigateBack()
            }
            // --- AKTIVASI LOGIKA HAPUS SUKSES ---
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
            icon = { Icon(Icons.Default.Warning, null, tint = SoftError) },
            title = { Text("Batalkan?", color = SoftError, style = MaterialTheme.typography.titleLarge) },
            text = { Text("Perubahan belum disimpan. Yakin ingin keluar?") },
            confirmButton = {
                Button(onClick = { showDiscardDialog = false; navigateBack() }, colors = ButtonDefaults.buttonColors(containerColor = SoftError)) {
                    Text("Keluar", color = Color.White)
                }
            },
            dismissButton = { OutlinedButton(onClick = { showDiscardDialog = false }) { Text("Batal") } },
            containerColor = Color.White
        )
    }

    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            icon = { Icon(Icons.Default.Delete, null, tint = SoftError) },
            title = { Text("Hapus Akun Permanen?", color = SoftError, style = MaterialTheme.typography.titleLarge) },
            text = { Text("Tindakan ini tidak dapat dibatalkan. Semua data artikel Anda akan ikut terhapus.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountDialog = false
                        viewModel.deleteAccount() // MEMANGGIL FUNGSI HAPUS DI VIEWMODEL
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SoftError)
                ) {
                    Text("Hapus Akun", color = Color.White)
                }
            },
            dismissButton = { OutlinedButton(onClick = { showDeleteAccountDialog = false }) { Text("Batal") } },
            containerColor = Color.White
        )
    }

    if (showSaveConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showSaveConfirmDialog = false },
            title = { Text("Simpan Profil?", style = MaterialTheme.typography.titleLarge) },
            text = { Text("Yakin ingin memperbarui data profil Anda?") },
            confirmButton = {
                Button(onClick = { showSaveConfirmDialog = false; viewModel.submitUpdate() }, colors = ButtonDefaults.buttonColors(containerColor = PastelBluePrimary)) {
                    Text("Ya, Simpan")
                }
            },
            dismissButton = { TextButton(onClick = { showSaveConfirmDialog = false }) { Text("Batal") } },
            containerColor = Color.White
        )
    }

    // --- LAYOUT UTAMA ---
    Box(modifier = Modifier.fillMaxSize().background(PastelBluePrimary)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(80.dp))

            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = viewModel.fullName,
                        onValueChange = { viewModel.fullName = it },
                        label = { Text(stringResource(R.string.full_name_label), style = MaterialTheme.typography.titleMedium) },
                        leadingIcon = { Icon(Icons.Default.Person, null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = viewModel.email,
                        onValueChange = { viewModel.email = it },
                        label = { Text(stringResource(R.string.email_label), style = MaterialTheme.typography.titleMedium) },
                        leadingIcon = { Icon(Icons.Default.Email, null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = viewModel.password,
                        onValueChange = { viewModel.password = it },
                        label = { Text("${stringResource(R.string.password_label)} Baru (Opsional)", style = MaterialTheme.typography.titleMedium) },
                        leadingIcon = { Icon(Icons.Default.Lock, null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, null)
                            }
                        }
                    )

                    Text(
                        text = "*Minimal 8 karakter jika ingin mengganti password.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = viewModel.bio,
                        onValueChange = { viewModel.bio = it },
                        label = { Text("Bio Singkat", style = MaterialTheme.typography.titleMedium) },
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        maxLines = 5,
                        textStyle = MaterialTheme.typography.bodyLarge,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // --- TOMBOL HAPUS AKUN ---
                    Divider(color = Color.LightGray.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = { showDeleteAccountDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColors(contentColor = SoftError)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Hapus Akun Permanen", style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().height(80.dp).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onBackAttempt() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
            }
            Spacer(Modifier.width(8.dp))
            Text("Edit Profil", style = MaterialTheme.typography.titleLarge, color = Color.White)
        }

        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.BottomEnd) {
            FloatingActionButton(
                onClick = {
                    val name = viewModel.fullName.trim()
                    val email = viewModel.email.trim()
                    val password = viewModel.password.trim()

                    if (name.isBlank() || email.isBlank()) {
                        scope.launch { snackbarHostState.showSnackbar("Nama dan Email wajib diisi") }
                    } else if (!isNameValid(name)) {
                        scope.launch { snackbarHostState.showSnackbar("Nama tidak boleh mengandung simbol") }
                    } else if (!isEmailValid(email)) {
                        scope.launch { snackbarHostState.showSnackbar("Format email salah (harus ada @)") }
                    } else if (password.isNotEmpty() && password.length < 8) {
                        scope.launch { snackbarHostState.showSnackbar("Password baru minimal 8 karakter") }
                    } else {
                        showSaveConfirmDialog = true
                    }
                },
                containerColor = PastelBluePrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                if (uiState is EditProfileUiState.Loading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Icon(Icons.Default.Check, "Simpan")
            }
        }

        Box(modifier = Modifier.fillMaxSize().padding(bottom = 20.dp), contentAlignment = Alignment.BottomCenter) {
            SnackbarHost(hostState = snackbarHostState) { data ->
                val isSuccess = data.visuals.message.contains("Berhasil", true)
                if (isSuccess) {
                    Box(modifier = Modifier.padding(16.dp).fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(Brush.horizontalGradient(listOf(PastelBluePrimary, PastelPinkSecondary))).padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null, tint = Color.White)
                            Spacer(Modifier.width(12.dp))
                            Text(text = data.visuals.message, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                } else {
                    Snackbar(containerColor = SoftError, contentColor = Color.White, snackbarData = data)
                }
            }
        }
    }
}