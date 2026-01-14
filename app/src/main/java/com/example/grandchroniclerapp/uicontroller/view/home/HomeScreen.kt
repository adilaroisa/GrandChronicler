package com.example.grandchroniclerapp.uicontroller.view.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.grandchroniclerapp.R
import com.example.grandchroniclerapp.model.Article
import com.example.grandchroniclerapp.ui.theme.PastelBluePrimary
import com.example.grandchroniclerapp.ui.theme.PastelPinkContainer
import com.example.grandchroniclerapp.ui.theme.BlackText
import com.example.grandchroniclerapp.viewmodel.home.HomeUiState
import com.example.grandchroniclerapp.viewmodel.home.HomeViewModel
import com.example.grandchroniclerapp.viewmodel.provider.PenyediaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onDetailClick: (Int) -> Unit,
    viewModel: HomeViewModel = viewModel(factory = PenyediaViewModel.Factory)
) {
    val uiState = viewModel.homeUiState

    // UTAMA: Detektor Scroll
    val gridState = rememberLazyGridState()
    val isAtBottom by remember {
        derivedStateOf {
            val layoutInfo = gridState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            // Trigger load more jika sudah melihat item ke-(Total - 4)
            totalItems > 0 && lastVisibleItemIndex >= (totalItems - 4)
        }
    }

    LaunchedEffect(isAtBottom) {
        if (isAtBottom) {
            viewModel.loadArticles(reset = false) // Load Next Page
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(PastelBluePrimary)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(80.dp))
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
                color = Color.White
            ) {
                when (uiState) {
                    is HomeUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = PastelBluePrimary)
                        }
                    }
                    is HomeUiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text((uiState as HomeUiState.Error).message, color = MaterialTheme.colorScheme.error)
                                TextButton(onClick = { viewModel.loadArticles(true) }) { Text("Coba Lagi") }
                            }
                        }
                    }
                    is HomeUiState.Success -> {
                        if (viewModel.articles.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Belum ada kisah sejarah.", color = Color.Gray)
                            }
                        } else {
                            // TAMPILKAN GRID
                            LazyVerticalGrid(
                                state = gridState, // Hubungkan state scroll
                                columns = GridCells.Fixed(2),
                                contentPadding = PaddingValues(top = 24.dp, bottom = 120.dp, start = 16.dp, end = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(viewModel.articles) { article ->
                                    ArticleCard(article = article, onClick = { onDetailClick(article.article_id) })
                                }

                                // Loading Indicator di Bawah (Saat scroll)
                                if (viewModel.isLoadingMore) {
                                    item {
                                        Box(Modifier.fillMaxWidth().height(50.dp), contentAlignment = Alignment.Center) {
                                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = PastelBluePrimary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Header Judul
        Row(
            modifier = Modifier.fillMaxWidth().height(80.dp).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = stringResource(R.string.home_title), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun ArticleCard(article: Article, onClick: () -> Unit) {
    val thumbnailImage = article.images.firstOrNull() ?: article.image

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            if (thumbnailImage != null) {
                // Fix URL jika tidak ada http
                val imgUrl = if (thumbnailImage.startsWith("http")) thumbnailImage else "http://10.0.2.2:3000/uploads/$thumbnailImage"
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(imgUrl).crossfade(true).build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(150.dp)
                )
            } else {
                Box(modifier = Modifier.fillMaxWidth().height(150.dp).background(Color(0xFFEEEEEE)), contentAlignment = Alignment.Center) {
                    Text("No Image", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Surface(color = PastelPinkContainer, shape = RoundedCornerShape(6.dp)) {
                    Text(text = article.category_name ?: "Umum", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = BlackText)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = article.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = BlackText, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "Oleh: ${article.author_name ?: "Sejarawan"}", style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}