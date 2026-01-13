package com.example.grandchroniclerapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.grandchroniclerapp.R

// --- DEFINISI FONT ---

// 1. Font untuk JUDUL BESAR (Nuansa Roman/Epic)
val Cinzel = FontFamily(
    Font(R.font.cinzel_regular, FontWeight.Normal),
    Font(R.font.cinzel_bold, FontWeight.Bold)
)

// 2. Font untuk Body/Teks Biasa (Bersih & Modern)
val Lato = FontFamily(
    Font(R.font.lato_regular, FontWeight.Normal),
    Font(R.font.lato_bold, FontWeight.Bold)
)

// --- SETTING TYPOGRAPHY MATERIAL 3 ---
val Typography = Typography(

    // JUDUL UTAMA (Halaman Login, Header Home)
    headlineLarge = TextStyle(
        fontFamily = Cinzel, // Pake Cinzel biar "GRAND"
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 2.sp // Kasih spasi antar huruf biar makin mewah
    ),

    // JUDUL HALAMAN (TopAppBar, Nama User)
    titleLarge = TextStyle(
        fontFamily = Cinzel,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.5.sp
    ),

    // SUB-JUDUL (Kategori, Tanggal)
    titleMedium = TextStyle(
        fontFamily = Lato,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),

    // ISI KONTEN (Artikel)
    bodyLarge = TextStyle(
        fontFamily = Lato,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.5.sp
    ),

    // TOMBOL
    labelLarge = TextStyle(
        fontFamily = Lato,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 1.sp
    )
)