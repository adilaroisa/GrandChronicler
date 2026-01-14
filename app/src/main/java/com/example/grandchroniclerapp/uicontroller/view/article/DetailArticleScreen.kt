package com.example.grandchroniclerapp.uicontroller.view.article

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
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
                Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PastelBluePrimary)
                }
            }
            is DetailUiState.Error -> {
                Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
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
                    // --- 1. HEADER GAMBAR SLIDER ---
                    if (article.images.isNotEmpty()) {
                        val pagerState = rememberPagerState(pageCount = { article.images.size })

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .background(Color.LightGray)
                        ) {
                            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                                // Cek URL, tambahkan base url jika perlu
                                val rawUrl = article.images[page]
                                val fullUrl = if (rawUrl.startsWith("http")) rawUrl else "http://10.0.2.2:3000/uploads/$rawUrl"

                                AsyncImage(
                                    model = fullUrl,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            // Tombol Navigasi Slider (Kiri/Kanan)
                            if (article.images.size > 1) {
                                if (pagerState.currentPage > 0) {
                                    IconButton(
                                        onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
                                        modifier = Modifier.align(Alignment.CenterStart).padding(8.dp).background(Color.Black.copy(0.3f), CircleShape)
                                    ) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null, tint = Color.White) }
                                }
                                if (pagerState.currentPage < article.images.size - 1) {
                                    IconButton(
                                        onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                                        modifier = Modifier.align(Alignment.CenterEnd).padding(8.dp).background(Color.Black.copy(0.3f), CircleShape)
                                    ) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.White) }
                                }
                                // Indikator Halaman (1/3)
                                Box(
                                    modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp).background(Color.Black.copy(0.6f), RoundedCornerShape(16.dp)).padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("${pagerState.currentPage + 1}/${article.images.size}", color = Color.White, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp).background(Color(0xFFEEEEEE)), contentAlignment = Alignment.Center) {
                            Text("Tidak ada gambar", color = Color.Gray)
                        }
                    }

                    // --- 2. KONTEN ARTIKEL ---
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Kategori
                        Text(article.category_name ?: "Tanpa Kategori", color = PastelBluePrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)

                        Spacer(Modifier.height(8.dp))

                        // Judul
                        Text(article.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

                        Spacer(Modifier.height(16.dp))

                        // Metadata Row (Penulis | Tanggal | Views)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Penulis
                            Icon(Icons.Default.Person, null, Modifier.size(16.dp), tint = Color.Gray)
                            Spacer(Modifier.width(4.dp))
                            Text(article.author_name ?: "Unknown", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                            Spacer(Modifier.width(16.dp))

                            // Tanggal
                            Icon(Icons.Default.CalendarToday, null, Modifier.size(16.dp), tint = Color.Gray)
                            Spacer(Modifier.width(4.dp))
                            Text(article.published_at?.take(10) ?: "Draft", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                            // --- VIEW COUNT (DITAMBAHKAN DI SINI) ---
                            Spacer(Modifier.width(16.dp))
                            Icon(Icons.Default.Visibility, null, Modifier.size(16.dp), tint = Color.Gray)
                            Spacer(Modifier.width(4.dp))
                            // Mengambil views_count dari objek article
                            Text("${article.views_count} x Dilihat", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }

                        Divider(Modifier.padding(vertical = 20.dp))

                        // Isi Artikel
                        Text(article.content, style = MaterialTheme.typography.bodyLarge, lineHeight = 28.sp)
                    }
                }
            }
        }
    }
}