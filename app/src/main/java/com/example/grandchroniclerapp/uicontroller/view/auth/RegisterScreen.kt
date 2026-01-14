package com.example.grandchroniclerapp.uicontroller.view.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
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
    onNavigateToTerms: () -> Unit,
    viewModel: RegisterViewModel = viewModel(factory = PenyediaViewModel.Factory)
) {
    val uiState = viewModel.registerUiState
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var passwordVisible by remember { mutableStateOf(false) }

    var isTermsAccepted by remember { mutableStateOf(false) }
    var showErrors by remember { mutableStateOf(false) }

    // --- LOGIKA VALIDASI ---

    // 1. Nama: HANYA Huruf, Spasi, dan Titik
    // Regex: ^[a-zA-Z .]*$
    val isNameValid = viewModel.fullName.isNotBlank() && viewModel.fullName.matches(Regex("^[a-zA-Z .]*$"))

    // 2. Email
    val isEmailValid = viewModel.email.contains("@") && viewModel.email.contains(".")

    // 3. Password
    val hasLetter = viewModel.password.any { it.isLetter() }
    val hasDigit = viewModel.password.any { it.isDigit() }
    val isPasswordValid = viewModel.password.length >= 8 && hasLetter && hasDigit

    LaunchedEffect(uiState) {
        when (uiState) {
            is RegisterUiState.Success -> {
                scope.launch { snackbarHostState.showSnackbar("Registrasi Berhasil!") }
                delay(2000)
                viewModel.resetState()
                onRegisterSuccess()
            }
            is RegisterUiState.Error -> {
                snackbarHostState.showSnackbar(uiState.message)
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PastelBluePrimary)
    ) {
        // HEADER
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

        // FORM SHEET
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

                    // --- INPUT NAMA ---
                    OutlinedTextField(
                        value = viewModel.fullName,
                        onValueChange = { viewModel.updateFullName(it) },
                        label = { Text("Nama Lengkap") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        isError = showErrors && !isNameValid,
                        trailingIcon = {
                            if (showErrors && !isNameValid) Icon(Icons.Default.Warning, null, tint = SoftError)
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // --- INPUT EMAIL ---
                    OutlinedTextField(
                        value = viewModel.email,
                        onValueChange = { viewModel.updateEmail(it) },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        isError = showErrors && !isEmailValid,
                        trailingIcon = {
                            if (showErrors && !isEmailValid) Icon(Icons.Default.Warning, null, tint = SoftError)
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // --- INPUT PASSWORD ---
                    OutlinedTextField(
                        value = viewModel.password,
                        onValueChange = { viewModel.updatePassword(it) },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        isError = showErrors && !isPasswordValid,
                        trailingIcon = {
                            val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = image, contentDescription = null)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // --- CHECKBOX TERMS ---
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = isTermsAccepted,
                            onCheckedChange = { isTermsAccepted = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = PastelBluePrimary,
                                uncheckedColor = if (showErrors && !isTermsAccepted) SoftError else Color.Gray
                            )
                        )

                        val annotatedString = buildAnnotatedString {
                            append("Saya menyetujui ")
                            withStyle(style = SpanStyle(color = PastelBluePrimary, fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline)) {
                                append("Syarat & Ketentuan")
                            }
                            append(" yang berlaku.")
                        }

                        Text(
                            text = annotatedString,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.clickable { onNavigateToTerms() },
                            color = if (showErrors && !isTermsAccepted) SoftError else Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // --- BUTTON DAFTAR ---
                    Button(
                        onClick = {
                            showErrors = true

                            if (viewModel.fullName.isBlank() || viewModel.email.isBlank() || viewModel.password.isBlank()) {
                                scope.launch { snackbarHostState.showSnackbar("Semua kolom wajib diisi!") }
                            } else if (!isNameValid) {
                                scope.launch { snackbarHostState.showSnackbar("Nama hanya boleh huruf dan titik (.)") }
                            } else if (!isEmailValid) {
                                scope.launch { snackbarHostState.showSnackbar("Format email salah") }
                            } else if (!isPasswordValid) {
                                scope.launch { snackbarHostState.showSnackbar("Password min. 8 karakter, kombinasi huruf & angka") }
                            } else if (!isTermsAccepted) {
                                scope.launch { snackbarHostState.showSnackbar("Anda wajib menyetujui Syarat & Ketentuan") }
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

                // SNACKBAR
                Box(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp)
                ) {
                    SnackbarHost(hostState = snackbarHostState) { data ->
                        val isSuccess = data.visuals.message.contains("Berhasil", true)
                        val bgColor = if (isSuccess) Brush.horizontalGradient(listOf(PastelBluePrimary, PastelPinkSecondary)) else Brush.linearGradient(listOf(SoftError, SoftError))

                        Box(
                            modifier = Modifier.padding(16.dp).fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(bgColor).padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(if(isSuccess) Icons.Default.CheckCircle else Icons.Default.Warning, null, tint = Color.White)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(data.visuals.message, color = Color.White, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            }
        }
    }
}