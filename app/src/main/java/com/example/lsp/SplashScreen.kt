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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
    var startAnimation by remember { mutableStateOf(false) }

    // Animasi arc oranye (kecil, 90 derajat)
    val orangeSweep by animateFloatAsState(
        targetValue = if (startAnimation) 90f else 0f,
        animationSpec = tween(1000, delayMillis = 0),
        label = "orangeSweep"
    )

    // Animasi arc biru (besar, 180 derajat)
    val blueSweep by animateFloatAsState(
        targetValue = if (startAnimation) 180f else 0f,
        animationSpec = tween(1200, delayMillis = 1000),
        label = "blueSweep"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(3000)
        onFinish()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(250.dp)) {
            val strokeWidth = 40f

            // Arc Oranye (kecil di atas kiri)
            drawArc(
                color = Color(0xFFFF6F00),
                startAngle = 360f,
                sweepAngle = orangeSweep,
                useCenter = false,
                topLeft = Offset(size.width / 2f - 210f, size.height / 2f - 240f),
                size = Size(240f, 240f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
            )

            // Arc Biru (besar di kanan bawah)
            drawArc(
                color = Color(0xFF0288D1),
                startAngle = 90f,
                sweepAngle = blueSweep,
                useCenter = false,
                topLeft = Offset(size.width / 2f - 10f, size.height / 2f - 10f),
                size = Size(240f, 240f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
            )
        }
    }
}


