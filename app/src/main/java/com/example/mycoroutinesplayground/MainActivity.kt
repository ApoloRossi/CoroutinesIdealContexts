package com.example.mycoroutinesplayground

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.example.mycoroutinesplayground.ui.theme.MyCoroutinesPlaygroundTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.net.URL

class MainActivity : ComponentActivity() {

    private val IMAGE_URL =
        "https://raw.githubusercontent.com/DevTides/JetpackDogsApp/master/app/src/main/res/drawable/dog.png"

    var state : MutableState<State> = mutableStateOf(State.Loading)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fetchImage(state)

        setContent {
            MyCoroutinesPlaygroundTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }

    @Composable
    fun MainScreen() {

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {

            AnimatedVisibility(visible = state.value is State.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.width(128.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            if(state.value is State.Success) {
                Image(
                    bitmap = (state.value as State.Success).image,
                    contentDescription = "",
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable {
                            Toast
                                .makeText(this@MainActivity, "Clicked", Toast.LENGTH_SHORT)
                                .show()
                            imageToBlackAndWhite(state)
                        }
                )
            }
        }
    }

    fun getOriginalBitmap() = URL(IMAGE_URL).openStream().use {
        BitmapFactory.decodeStream(it)
    }

    private fun fetchImage(imageToLoad: MutableState<State>) =
        CoroutineScope(Dispatchers.Main).launch {
            val originalDeferred = async(Dispatchers.IO) { getOriginalBitmap() }
            imageToLoad.value = State.Success(originalDeferred.await().asImageBitmap())
        }


    private fun imageToBlackAndWhite(state: MutableState<State>) {
        val image = (state.value as State.Success).image

        if((state.value as State.Success).isBlack) {
            state.value = State.Loading
            fetchImage(state)
        } else {
            state.value = State.Loading

            println("Thread: ${Thread.currentThread().name}")
            CoroutineScope(Dispatchers.Default).launch {
                val bitmapDeferred = async {
                    println("Thread: ${Thread.currentThread().name}")
                    Filter.apply(image.asAndroidBitmap()).asImageBitmap()
                }

                println("Thread: ${Thread.currentThread().name}")
                state.value = State.Success(bitmapDeferred.await(), true)
            }


        }
    }
}
