package me.diegomelo.minesweeper

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import kotlinx.coroutines.delay

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AnimationScreen()
        }
    }
}

@Composable
fun AnimationScreen() {
    val context = LocalContext.current // Get the context here
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.explosion)) // Replace with your actual file name
    val progress by animateLottieCompositionAsState(composition)

    LaunchedEffect(progress) {
        if (progress == 1f) { // Check if the animation has completed
            delay(1000) // Wait for a short duration before starting MainActivity
            context.startActivity(Intent(context, MainActivity::class.java))
            (context as SplashActivity).finish() // Close AnimationActivity
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LottieAnimation(
            composition = composition,
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp) // Adjust height as needed
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Loading...",
            color = Color.Black // Adjust color as needed
        )
    }
}
