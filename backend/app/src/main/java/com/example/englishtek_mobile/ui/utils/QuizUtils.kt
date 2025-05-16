package com.example.englishtek_mobile.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Utility functions for the Quiz screens
 */

/**
 * Get text representation of difficulty level
 */
@Composable
fun getDifficultyText(level: Int): String {
    return when (level) {
        1 -> "Easy"
        2 -> "Medium"
        3 -> "Hard"
        4 -> "Expert"
        5 -> "Master"
        else -> "Unknown"
    }
}

/**
 * Get color representation of difficulty level
 */
@Composable
fun getDifficultyColor(level: Int): Color {
    return when (level) {
        1 -> Color(0xFF4CAF50) // Green
        2 -> Color(0xFF8BC34A) // Light Green
        3 -> Color(0xFFFFC107) // Amber
        4 -> Color(0xFFFF9800) // Orange
        5 -> Color(0xFFF44336) // Red
        else -> Color(0xFF9E9E9E) // Grey
    }
}
