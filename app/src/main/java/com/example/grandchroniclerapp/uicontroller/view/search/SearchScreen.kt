package com.example.grandchroniclerapp.uicontroller.view.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.grandchroniclerapp.model.Article
import com.example.grandchroniclerapp.ui.theme.PastelBluePrimary
import com.example.grandchroniclerapp.viewmodel.provider.PenyediaViewModel
import com.example.grandchroniclerapp.viewmodel.search.SearchUiState
import com.example.grandchroniclerapp.viewmodel.search.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    initialQuery: String? = null,
    onDetailClick: (Int) -> Unit,
    viewModel: SearchViewModel = viewModel(factory = PenyediaViewModel.Factory)
) {
    val focusManager = LocalFocusManager.current
    val uiState = viewModel.searchUiState

    // Handle initial query (dari Tag)
    LaunchedEffect(initialQuery) {
        if (!initialQuery.isNullOrBlank()) {
            viewModel.updateQuery(initialQuery)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // --- SEARCH BAR ---
        OutlinedTextField(
            value = viewModel.searchQuery,
            onValueChange = { viewModel.updateQuery(it) },
            label = {
                if (viewModel.selectedCategory != null)
                    Text("Kategori: ${viewModel.selectedCategory!!.category_name}")
                else
                    Text("Cari Artikel Sejarah...")
            },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (viewModel.searchQuery.isNotEmpty() || viewModel.selectedCategory != null) {
                    IconButton(onClick = {
                        viewModel.clearSearch()
                        focusManager.clearFocus()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Hapus")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- KONTEN UTAMA ---
        when (uiState) {
            is SearchUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PastelBluePrimary)
                }
            }
            is SearchUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = uiState.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is SearchUiState.Success -> {
                // HEADER HASIL
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                    val headerText = if (viewModel.selectedCategory != null)
                        "Kategori: ${viewModel.selectedCategory!!.category_name}"
                    else
                        "Hasil Pencarian:"

                    Text(text = headerText, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                if (uiState.articles.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Search, null, tint = Color.Gray, modifier = Modifier.size(50.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("Tidak ada artikel ditemukan.", color = Color.Gray)
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.articles) { article ->
                            SearchArticleCard(
                                article = article,
                                searchQuery = viewModel.searchQuery,
                                onClick = { onDetailClick(article.article_id) }
                            )
                        }
                    }
                }
            }
            is SearchUiState.Idle -> {
                // --- KATEGORI GRID ---
                Text(text = "Jelajahi Kategori", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                if (viewModel.categories.isEmpty()) {
                    Text("Memuat kategori...", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // FIX: Filter ID 7 (Tanpa Kategori) agar tidak muncul di sini
                        items(viewModel.categories.filter { it.category_id != 7 }) { category ->
                            CategoryCard(categoryName = category.category_name) {
                                viewModel.selectCategory(category)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- KOMPONEN PENDUKUNG ---
@Composable
fun CategoryCard(categoryName: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE0E0)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.height(60.dp).fillMaxWidth()
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = categoryName, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = Color(0xFF4A4A4A))
        }
    }
}

@Composable
fun SearchArticleCard(article: Article, searchQuery: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            if (article.images.isNotEmpty()) {
                val imgUrl = if (article.images[0].startsWith("http")) article.images[0] else "http://10.0.2.2:3000/uploads/${article.images[0]}"
                AsyncImage(model = imgUrl, contentDescription = null, modifier = Modifier.fillMaxWidth().height(100.dp), contentScale = ContentScale.Crop)
            } else {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp).background(Color.LightGray))
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = article.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)

                if (searchQuery.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    val snippet = remember(article.content, searchQuery) { generateSearchSnippet(article.content, searchQuery) }
                    Text(text = snippet, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = Color.Gray, maxLines = 3, overflow = TextOverflow.Ellipsis, lineHeight = 14.sp)
                }
            }
        }
    }
}

fun generateSearchSnippet(content: String, query: String): androidx.compose.ui.text.AnnotatedString {
    if (query.isBlank()) return buildAnnotatedString { append(content.take(80) + "...") }
    val index = content.indexOf(query, ignoreCase = true)
    if (index == -1) return buildAnnotatedString { append(content.take(80) + "...") }
    val start = maxOf(0, index - 30)
    val end = minOf(content.length, index + query.length + 50)
    val snippetRaw = content.substring(start, end)
    return buildAnnotatedString {
        if (start > 0) append("... ")
        val relativeIndex = snippetRaw.indexOf(query, ignoreCase = true)
        if (relativeIndex != -1) {
            append(snippetRaw.substring(0, relativeIndex))
            withStyle(style = SpanStyle(fontWeight = FontWeight.ExtraBold, color = PastelBluePrimary, background = Color(0xFFE3F2FD))) {
                append(snippetRaw.substring(relativeIndex, relativeIndex + query.length))
            }
            append(snippetRaw.substring(relativeIndex + query.length))
        } else {
            append(snippetRaw)
        }
        if (end < content.length) append("...")
    }
}