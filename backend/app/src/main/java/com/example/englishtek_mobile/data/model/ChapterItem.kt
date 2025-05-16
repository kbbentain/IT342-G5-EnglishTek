package com.example.englishtek_mobile.data.model

import com.google.gson.annotations.SerializedName

/**
 * Represents an item (lesson or quiz) within a chapter
 */
data class ChapterItem(
    val id: String,
    val title: String,
    val order: Int,
    @SerializedName("type")
    val type: ChapterItemType,
    @SerializedName("completed")
    val completed: Boolean
)

enum class ChapterItemType {
    @SerializedName("lesson")
    LESSON,
    
    @SerializedName("quiz")
    QUIZ
}
