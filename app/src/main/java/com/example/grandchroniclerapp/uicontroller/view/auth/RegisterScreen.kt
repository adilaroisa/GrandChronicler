package com.example.grandchroniclerapp.uicontroller.view.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import com.example.grandchroniclerapp.viewmodel.auth.RegisterUiState
import com.example.grandchroniclerapp.viewmodel.auth.RegisterViewModel
import com.example.grandchroniclerapp.viewmodel.provider.PenyediaViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: RegisterViewModel = viewModel(factory = PenyediaViewModel.Factory)
) {
    val uiState = viewModel.registerUiState
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var passwordVisible by remember { mutableStateOf(false) }

    // --- FUNGSI VALIDASI ---
    fun isNameValid(name: String): Boolean {
        // Hanya mengizinkan huruf, angka, dan spasi
        val regex = Regex("^[a-zA-Z0-9 ]*$")
        return name.matches(regex)
    }

    fun isEmailValid(email: String): Boolean {
        // Menggunakan pengecekan simpel agar tidak error karena typo spasi atau domain unik
        return email.contains("@") && email.contains(".")
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is RegisterUiState.Success -> {
                this.launch { snackbarHostState.showSnackbar("Registrasi Berhasil!") }
                delay(2000)
                viewModel.resetState()
                onRegisterSuccess()
            }
            is RegisterUiState.Error -> {
                snackbarHostState.showSnackbar(uiState.message)
                viewModel.resetState()
            }
            else -> { /* Idle */ }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PastelBluePrimary)
    ) {
        // 1. HEADER CENTER
        Box(
            modifier = Modifier
                .weight(0.25f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Bergabunglah Bersama Kami",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        // 2. SHEET FORM
        Surface(
            modifier = Modifier
                .weight(0.75f)
                .fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
            color = Color.White
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = stringResource(R.string.register_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = PastelBluePrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Input Nama
                    OutlinedTextField(
                        value = viewModel.fullName,
                        onValueChange = { viewModel.updateFullName(it) },
                        label = { Text(stringResource(R.string.full_name_label), style = MaterialTheme.typography.titleMedium) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        textStyle = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Input Email
                    OutlinedTextField(
                        value = viewModel.email,
                        onValueChange = { viewModel.updateEmail(it) },
                        label = { Text(stringResource(R.string.email_label), style = MaterialTheme.typography.titleMedium) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        textStyle = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Input Password
                    OutlinedTextField(
                        value = viewModel.password,
                        onValueChange = { viewModel.updatePassword(it) },
                        label = { Text(stringResource(R.string.password_label), style = MaterialTheme.typography.titleMedium) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        trailingIcon = {
                            val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = image, contentDescription = null)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Button Daftar
                    Button(
                        onClick = {
                            // Menggunakan trim() untuk mencegah error validasi karena spasi
                            val name = viewModel.fullName.trim()
                            val email = viewModel.email.trim()
                            val password = viewModel.password.trim()

                            if (name.isBlank() || email.isBlank() || password.isBlank()) {
                                scope.launch { snackbarHostState.showSnackbar("Semua kolom wajib diisi") }
                            } else if (!isNameValid(name)) {
                                scope.launch { snackbarHostState.showSnackbar("Nama hanya boleh huruf dan angka") }
                            } else if (!isEmailValid(email)) {
                                scope.launch { snackbarHostState.showSnackbar("Mohon masukkan format email yang benar (misal: nama@email.com)") }
                            } else if (password.length < 6) {
                                scope.launch { snackbarHostState.showSnackbar("Password minimal 6 karakter") }
                            } else {
                                viewModel.register()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        enabled = uiState !is RegisterUiState.Loading,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PastelBluePrimary)
                    ) {
                        if (uiState is RegisterUiState.Loading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        } else {
                            Text(stringResource(R.string.btn_register), style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(onClick = onNavigateBack) {
                        Text(stringResource(R.string.ask_login), style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                }

                // Snackbar Host (Floating)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 20.dp)
                ) {
                    SnackbarHost(hostState = snackbarHostState) { data ->
                        val isSuccess = data.visuals.message.contains("Berhasil", true)
                        if (isSuccess) {
                            Box(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Brush.horizontalGradient(listOf(PastelBluePrimary, PastelPinkSecondary)))
                                    .padding(16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = data.visuals.message,
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodyLarge,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        } else {
                            Snackbar(containerColor = SoftError, contentColor = Color.White, snackbarData = data)
                        }
                    }
                }
            }
        }
    }
}