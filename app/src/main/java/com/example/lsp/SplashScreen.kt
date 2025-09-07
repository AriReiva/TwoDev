package com.example.lsp

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.lsp.ui.theme.LSPTheme
import kotlinx.coroutines.delay

class SplashScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LSPTheme {
                SplashContent {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
        }
    }
}

@Composable
fun SplashContent(onFinish: () -> Unit) {
    var startLogo by remember { mutableStateOf(false) }
    var fadeLogo by remember { mutableStateOf(false) }
    var mergeBalls by remember { mutableStateOf(false) }
    var expandBall by remember { mutableStateOf(false) }

    // Logo
    val logoScale by animateFloatAsState(
        targetValue = if (startLogo) 1f else 0f,
        animationSpec = tween(800, easing = OvershootEasing(2f)),
        label = "logoScale"
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (fadeLogo) 0f else 1f,
        animationSpec = tween(600),
        label = "logoAlpha"
    )

    // Posisi bola kiri & kanan menuju tengah
    val offsetAnim by animateFloatAsState(
        targetValue = if (mergeBalls) 0f else 60f,
        animationSpec = tween(900, easing = EaseInOut),
        label = "offsetAnim"
    )

//    // Scale kecil waktu merge → kasih efek “menyatu”
//    val mergeScale by animateFloatAsState(
//        targetValue = if (mergeBalls) 1.5f else 1f,
//        animationSpec = tween(00, easing = EaseInOut),
//        label = "mergeScale"
//    )

    // Scale bola final ketika expand
    val bigBallScale by animateFloatAsState(
        targetValue = if (expandBall) 80f else 1f,
        animationSpec = tween(1500, easing = FastOutSlowInEasing),
        label = "bigBallScale"
    )

    // Timeline
    LaunchedEffect(Unit) {
        startLogo = true
        delay(1200)
        fadeLogo = true
        delay(600)
        mergeBalls = true
        delay(1000)
        expandBall = true
        delay(2000)
        onFinish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color(0xFFff7e5f)
            ),
        contentAlignment = Alignment.Center
    ) {
        // Logo
        Image(
            painter = painterResource(id = R.drawable.logo_pth_lsp),
            contentDescription = "Logo LSP",
            modifier = Modifier
                .size(200.dp)
                .scale(logoScale)
                .alpha(logoAlpha)
        )

        // Bola
        if (!expandBall) {
            if (!mergeBalls) {
                // 3 bola sebelum merge
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 120.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
//                    Ball(xOffset = -offsetAnim, color = Color.White, scale = mergeScale)
//                    Ball(xOffset = 0f, color = Color.White, scale = mergeScale)
//                    Ball(xOffset = offsetAnim, color = Color.White, scale = mergeScale)
                }
            } else {
                // Sudah jadi 1 bola
                Ball(xOffset = 0f, color = Color.White, scale = bigBallScale)
            }
        } else {
            // Expand nutup layar
            Ball(xOffset = 0f, color = Color.White, scale = bigBallScale)
        }
    }
}

@Composable
fun Ball(xOffset: Float = 0f, color: Color, scale: Float) {
    Canvas(
        modifier = Modifier
            .size(20.dp)
            .offset(x = xOffset.dp, y = 0.dp)
            .scale(scale)
    ) {
        drawCircle(color = color)
    }
}

// Custom Overshoot Easing
fun OvershootEasing(tension: Float = 2f): Easing = Easing { fraction ->
    val t = fraction - 1f
    t * t * ((tension + 1) * t + tension) + 1f
}

