package com.example.grandchroniclerapp.uicontroller.view.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.CachePolicy
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

    // State Grid i
    val gridState = rememberLazyStaggeredGridState()

    // Infinite Scroll
    val isAtBottom by remember {
        derivedStateOf {
            val layoutInfo = gridState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisibleItemIndex >= (totalItems - 4)
        }
    }

    LaunchedEffect(isAtBottom) {
        if (isAtBottom) {
            viewModel.loadArticles(reset = false)
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
                            LazyVerticalStaggeredGrid(
                                state = gridState,
                                columns = StaggeredGridCells.Fixed(2),
                                contentPadding = PaddingValues(top = 24.dp, bottom = 120.dp, start = 16.dp, end = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalItemSpacing = 16.dp,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                // Menjaga kestabilan item saat scroll/update
                                items(viewModel.articles, key = { it.article_id }) { article ->

                                    val isTokohDunia = article.category_id == 2

                                    HybridArticleCard(
                                        article = article,
                                        isPinterestStyle = isTokohDunia,
                                        onClick = { onDetailClick(article.article_id) }
                                    )
                                }

                                if (viewModel.isLoadingMore) {
                                    item(span = StaggeredGridItemSpan.FullLine) {
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
fun HybridArticleCard(
    article: Article,
    isPinterestStyle: Boolean,
    onClick: () -> Unit
) {
    val thumbnailImage = article.images.firstOrNull() ?: article.image

    // 1. Modifier Kartu Utama
    val sizeModifier = if (isPinterestStyle) {
        Modifier.fillMaxWidth().wrapContentHeight()
    } else {
        Modifier.fillMaxWidth().height(280.dp)
    }

    val badgeColor = if (isPinterestStyle) Color(0xFFFFF9C4) else PastelPinkContainer
    val badgeBorder = if (isPinterestStyle) BorderStroke(1.dp, Color(0xFFFBC02D)) else null

    Card(
        modifier = sizeModifier.clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            if (thumbnailImage != null) {
                val imgUrl = if (thumbnailImage.startsWith("http")) thumbnailImage else "http://10.0.2.2:3000/uploads/$thumbnailImage"

                // 2. Modifier Gambar
                val imageModifier = if (isPinterestStyle) {

                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .heightIn(min = 120.dp)
                } else {
                    Modifier.fillMaxWidth().height(140.dp)
                }


                val contentScale = if (isPinterestStyle) ContentScale.FillWidth else ContentScale.Crop

                val request = ImageRequest.Builder(LocalContext.current)
                    .data(imgUrl)
                    .crossfade(true)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .build()

                AsyncImage(
                    model = request,
                    contentDescription = null,
                    contentScale = contentScale,
                    modifier = imageModifier.background(Color(0xFFF5F5F5)) // Background abu tipis saat loading
                )
            } else {
                // Placeholder Teks jika tidak ada gambar
                Box(modifier = Modifier.fillMaxWidth().height(140.dp).background(Color(0xFFEEEEEE)), contentAlignment = Alignment.Center) {
                    Text("No Image", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                }
            }

            // Konten Teks
            Column(modifier = Modifier.padding(12.dp)) {
                Surface(
                    color = badgeColor,
                    shape = RoundedCornerShape(6.dp),
                    border = badgeBorder
                ) {
                    Text(
                        text = article.category_name ?: "Umum",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = BlackText
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                Text(text = article.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = BlackText, maxLines = 2, overflow = TextOverflow.Ellipsis)

                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "Oleh: ${article.author_name ?: "Sejarawan"}", style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)

                // menampilkan cuplikan teks hanya untuk Pinterest Style
                if (isPinterestStyle && article.content.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = article.content.take(60) + "...", style = MaterialTheme.typography.bodySmall, color = Color.Gray, lineHeight = 14.sp)
                }
            }
        }
    }
}