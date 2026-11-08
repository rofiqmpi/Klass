package com.mpi.klass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlin.math.abs

/**
 * Modern dynamic avatar generator using DiceBear API.
 * Focuses on character and animal-like styles (adventurer, bottts, lorelei)
 * to provide stunning, unique, and gender-neutral profile pictures.
 */
@Composable
fun DynamicAvatar(
    seed: String,
    modifier: Modifier = Modifier,
    borderWidth: Dp = 0.dp,
    borderColor: Color = Color.Transparent,
) {
    val cleanSeed = remember(seed) { seed.trim().lowercase().ifBlank { "klass_user_default" } }
    val hash = remember(cleanSeed) { abs(cleanSeed.hashCode()) }
    
    // DiceBear styles selection for variety and stunning visuals
    // adventurer: Beautiful characters
    // bottts: Cool robots
    // lorelei: Cute character designs
    // pixel-art: Retro animal/character style
    val diceBearUrl = remember(cleanSeed, hash) {
        val styles = listOf("adventurer", "bottts", "lorelei", "pixel-art")
        val selectedStyle = styles[hash % styles.size]
        
        // Using DiceBear v9 API
        val baseUrl = "https://api.dicebear.com/9.x/$selectedStyle/svg"
        val params = StringBuilder("?seed=${cleanSeed}")
        
        // Randomize background to ensure extreme uniqueness
        val bgTypes = listOf("solid", "gradientLinear")
        params.append("&backgroundType=${bgTypes[hash % bgTypes.size]}")
        params.append("&backgroundRotation=${hash % 360}")
        
        // Add more variability
        params.append("&flip=${hash % 2 == 0}")
        
        // Return full URL
        baseUrl + params.toString()
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Color.LightGray.copy(alpha = 0.05f)),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = diceBearUrl,
            contentDescription = "DiceBear Avatar",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        
        // Border if specified
        if (borderWidth > 0.dp) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent, CircleShape)
            )
        }
    }
}
