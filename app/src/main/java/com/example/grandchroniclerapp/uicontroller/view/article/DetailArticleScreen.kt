package com.example.grandchroniclerapp.uicontroller.view.article

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.grandchroniclerapp.viewmodel.article.DetailArticleViewModel
import com.example.grandchroniclerapp.viewmodel.article.DetailUiState
import com.example.grandchroniclerapp.viewmodel.provider.PenyediaViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailArticleScreen(
    navigateBack: () -> Unit,
    viewModel: DetailArticleViewModel = viewModel(factory = PenyediaViewModel.Factory)
) {
    val uiState = viewModel.detailUiState
    // Scope untuk menjalankan animasi geser saat panah diklik
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Detail Artikel") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { innerPadding ->
        when (uiState) {
            is DetailUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is DetailUiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = uiState.message, color = Color.Red)
                }
            }
            is DetailUiState.Success -> {
                val article = uiState.article
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                ) {
                    // --- 1. HEADER GAMBAR (HORIZONTAL PAGER) ---
                    if (article.images.isNotEmpty()) {
                        // State untuk mengetahui halaman/gambar ke berapa saat ini
                        val pagerState = rememberPagerState(pageCount = { article.images.size })

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp) // Sedikit lebih tinggi agar lega
                                .background(Color.LightGray)
                        ) {
                            // KOMPONEN PAGER (CAROUSEL)
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.fillMaxSize()
                            ) { page ->
                                Image(
                                    painter = rememberAsyncImagePainter(article.images[page]),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            // JIKA GAMBAR LEBIH DARI 1, TAMPILKAN NAVIGASI
                            if (article.images.size > 1) {

                                // Panah KIRI (Hanya muncul jika bukan di halaman pertama)
                                if (pagerState.currentPage > 0) {
                                    IconButton(
                                        onClick = {
                                            scope.launch {
                                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                            }
                                        },
                                        modifier = Modifier
                                            .align(Alignment.CenterStart)
                                            .padding(8.dp)
                                            .background(Color.Black.copy(0.3f), CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                            contentDescription = "Sebelumnya",
                                            tint = Color.White
                                        )
                                    }
                                }

                                // Panah KANAN (Hanya muncul jika bukan di halaman terakhir)
                                if (pagerState.currentPage < article.images.size - 1) {
                                    IconButton(
                                        onClick = {
                                            scope.launch {
                                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                            }
                                        },
                                        modifier = Modifier
                                            .align(Alignment.CenterEnd)
                                            .padding(8.dp)
                                            .background(Color.Black.copy(0.3f), CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                            contentDescription = "Selanjutnya",
                                            tint = Color.White
                                        )
                                    }
                                }

                                // Indikator Angka (Contoh: 1/3) di Pojok Kanan Bawah
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(12.dp)
                                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${pagerState.currentPage + 1}/${article.images.size}",
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    } else {
                        // Placeholder jika tidak ada gambar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .background(Color.LightGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Tidak ada gambar", color = Color.DarkGray)
                        }
                    }

                    // --- 2. JUDUL & INFO ---
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = article.category_name,
                            color = PastelBluePrimary,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = article.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(16.dp))

                        // Info Penulis & Tanggal
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, null, Modifier.size(16.dp), tint = Color.Gray)
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = article.author_name ?: "Unknown",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                            Spacer(Modifier.width(16.dp))
                            Icon(Icons.Default.CalendarToday, null, Modifier.size(16.dp), tint = Color.Gray)
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = article.published_at?.take(10) ?: "Draft",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }

                        // Views Count
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Visibility, null, Modifier.size(16.dp), tint = Color.LightGray)
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "${article.views_count} x Dibaca",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.LightGray
                            )
                        }

                        Divider(Modifier.padding(vertical = 20.dp))

                        // --- 3. ISI KONTEN ---
                        Text(
                            text = article.content,
                            style = MaterialTheme.typography.bodyLarge,
                            lineHeight = 28.sp // Agar enak dibaca
                        )
                    }
                }
            }
        }
    }
}