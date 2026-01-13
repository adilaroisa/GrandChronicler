package com.example.grandchroniclerapp.uicontroller.view.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.grandchroniclerapp.ui.theme.PastelBluePrimary
import com.example.grandchroniclerapp.ui.theme.WhiteBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    navigateBack: () -> Unit
) {
    Scaffold(
        containerColor = WhiteBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Tentang & Bantuan", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- LOGO / ICON BESAR ---
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(PastelBluePrimary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = PastelBluePrimary
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "The Grand Chronicler",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = PastelBluePrimary
            )
            Text(
                text = "Versi 1.0.0 (Beta)",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(Modifier.height(32.dp))

            // --- BAGIAN 1: PUSAT BANTUAN ---
            SectionHeader("Pusat Bantuan")
            InfoItem(
                icon = Icons.Default.QuestionAnswer,
                title = "Cara Menulis Artikel",
                desc = "Buka halaman Home atau Profile, lalu tekan tombol '+' di pojok kanan bawah. Isi judul, konten, dan tambahkan foto sejarah."
            )
            Spacer(Modifier.height(12.dp))
            InfoItem(
                icon = Icons.Default.Info,
                title = "Tentang Aplikasi",
                desc = "Platform digital untuk mendokumentasikan dan membagikan kisah sejarah lokal maupun dunia agar tidak terlupakan."
            )

            Spacer(Modifier.height(24.dp))

            // --- BAGIAN 2: PENGEMBANG ---
            SectionHeader("Pengembang")
            InfoItem(
                icon = Icons.Default.Person,
                title = "Dibuat Oleh",
                desc = "Adila Roisa Santosa."
            )
            Spacer(Modifier.height(12.dp))
            InfoItem(
                icon = Icons.Default.Email,
                title = "Kontak Support",
                desc = "admin@grandchronicler.com"
            )

            Spacer(Modifier.height(48.dp))
            Text(
                text = "© 2025 The Grand Chronicler",
                style = MaterialTheme.typography.labelSmall,
                color = Color.LightGray
            )
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = Color.Black
    )
}

@Composable
fun InfoItem(icon: ImageVector, title: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF5F7FA))
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = PastelBluePrimary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Column {
            Text(text = title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(4.dp))
            Text(text = desc, style = MaterialTheme.typography.bodySmall, color = Color.Gray, lineHeight = 18.sp)
        }
    }
}