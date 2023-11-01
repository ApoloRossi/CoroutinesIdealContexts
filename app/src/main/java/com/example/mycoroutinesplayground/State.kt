package com.example.mycoroutinesplayground

import androidx.compose.ui.graphics.ImageBitmap

sealed interface State {
    object Initial : State
    object Loading : State
    data class Success(val image: ImageBitmap, val isBlack : Boolean = false) : State
    data class Error(val message: String) : State
}