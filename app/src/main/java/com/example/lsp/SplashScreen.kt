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
        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                setImmersiveMode()
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startLockTask()
        }
        setContent {
            LSPTheme {
                SplashContent {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
        }
    }
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            setImmersiveMode()
        }
    }

    private fun setImmersiveMode() {
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    }
}

@Composable
fun SplashContent(onFinish: () -> Unit) {
    var startLogo by remember { mutableStateOf(false) }
    var fadeLogo by remember { mutableStateOf(false) }
    var mergeBalls by remember { mutableStateOf(false) }
    var liftBalls by remember { mutableStateOf(false) }
    var expandBall by remember { mutableStateOf(false) }

    // Logo muncul
    val logoScale by animateFloatAsState(
        targetValue = if (startLogo) 1f else 0f,
        animationSpec = tween(900, easing = OvershootEasing(2f)),
        label = "logoScale"
    )

    // Logo fade out
    val logoAlpha by animateFloatAsState(
        targetValue = if (fadeLogo) 0f else 1f,
        animationSpec = tween(700),
        label = "logoAlpha"
    )

    // Bounce awal
    val infinite = rememberInfiniteTransition(label = "bounce")
    val bounce by infinite.animateFloat(
        initialValue = 0f,
        targetValue = -25f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    // Offset horizontal → merge ke tengah
    val offsetAnim by animateFloatAsState(
        targetValue = if (mergeBalls) 0f else 50f,
        animationSpec = tween(600, easing = EaseInOut),
        label = "offsetAnim"
    )

    // Naik ke atas setelah merge
    val liftY by animateFloatAsState(
        targetValue = if (liftBalls) -250f else 0f,
        animationSpec = tween(700, easing = EaseInOut),
        label = "liftY"
    )

    // Bola expand (jadi background putih)
    val bigBallScale by animateFloatAsState(
        targetValue = if (expandBall) 80f else 1f,
        animationSpec = tween(1500, easing = FastOutSlowInEasing),
        label = "bigBallScale"
    )

    // scale bola saat merge
    val mergeBallScale by animateFloatAsState(
        targetValue = if (mergeBalls) 80f else 1f,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "mergeBallScale"
    )


    // Timeline animasi
    LaunchedEffect(Unit) {
        startLogo = true
        delay(1400) // logo masuk
        fadeLogo = true
        delay(400) // logo fade
        mergeBalls = true
        delay(1000) // kasih waktu merge scale animasi
        liftBalls = true
        delay(800)  // bola naik
        expandBall = true
        delay(2500) // bola expand nutup layar
        onFinish()
    }


    // Background transisi orange → putih
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (expandBall) {
                    Brush.verticalGradient(listOf(Color.White, Color.White))
                } else {
                    Brush.linearGradient(
                        listOf(Color(0xFFff7e5f), Color(0xFFfeb47b))
                    )
                }
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

        // Bola animasi
        if (!expandBall) {
            if (!mergeBalls) {
                // masih 3 bola sebelum merge
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 120.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Ball(yOffset = bounce, xOffset = -offsetAnim, color = Color.White, scale = 1f)
                    Ball(yOffset = bounce, xOffset = 0f, color = Color.White, scale = 1f)
                    Ball(yOffset = bounce, xOffset = offsetAnim, color = Color.White, scale = 1f)
                }
            } else {
                // sudah merge jadi 1 bola
                Ball(
                    yOffset = bounce + liftY,
                    xOffset = 0f,
                    color = Color.White,
                    scale = mergeBallScale
                )

            }
        } else {
            // expand bola jadi putih full
            Ball(yOffset = liftY, xOffset = 0f, color = Color.White, scale = bigBallScale)
        }
    }
}

@Composable
fun Ball(yOffset: Float, xOffset: Float = 0f, color: Color, scale: Float) {
    Canvas(
        modifier = Modifier
            .size(20.dp)
            .offset(x = xOffset.dp, y = yOffset.dp)
            .scale(scale)
    ) {
        drawCircle(color = color)
    }
}

// Custom Overshoot Easing
fun OvershootEasing(tension: Float = 2f): Easing = Easing { fraction ->
    val t = fraction - 1.0f
    t * t * ((tension + 1) * t + tension) + 1.0f
}

