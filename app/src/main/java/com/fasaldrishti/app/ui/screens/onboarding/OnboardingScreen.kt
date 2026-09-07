package com.fasaldrishti.app.ui.screens.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fasaldrishti.app.ui.components.GradientButton
import com.fasaldrishti.app.ui.theme.PrimaryGradientEnd
import com.fasaldrishti.app.ui.theme.PrimaryGradientStart
import kotlinx.coroutines.launch

data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val gradient: List<Color>
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    val pages = listOf(
        OnboardingPage(
            title = "Apne crop ki photo kheenchiye",
            description = "Point your camera at affected crop leaves. Capture high-clarity photos effortlessly with our guided viewfinder.",
            icon = Icons.Default.CameraAlt,
            gradient = listOf(PrimaryGradientStart, PrimaryGradientEnd)
        ),
        OnboardingPage(
            title = "AI se turant disease pehchaniye",
            description = "Our fine-tuned MobileNet deep learning model recognizes 38 crop disease types in under 1 second with 94%+ accuracy.",
            icon = Icons.Default.Psychology,
            gradient = listOf(Color(0xFF00796B), Color(0xFF26A69A))
        ),
        OnboardingPage(
            title = "Sahi ilaaj, behtar fasal",
            description = "Get instant chemical and organic spray dosages, preventative care, and chat directly with our AI Agronomist.",
            icon = Icons.Default.CheckCircle,
            gradient = listOf(Color(0xFF2E7D32), Color(0xFF66BB6A))
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                if (pagerState.currentPage < pages.size - 1) {
                    TextButton(onClick = onFinish) {
                        Text(
                            text = "Skip",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Page Indicator Dots (Pill Elongation)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(pages.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        val width by animateDpAsState(
                            targetValue = if (isSelected) 28.dp else 8.dp,
                            animationSpec = tween(durationMillis = 300),
                            label = "DotWidth"
                        )
                        val color by animateColorAsState(
                            targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                            label = "DotColor"
                        )

                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(width)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                val isLastPage = pagerState.currentPage == pages.size - 1
                GradientButton(
                    text = if (isLastPage) "Get Started" else "Next",
                    onClick = {
                        if (isLastPage) {
                            onFinish()
                        } else {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { pageIndex ->
            val page = pages[pageIndex]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Illustration Card
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .clip(RoundedCornerShape(36.dp))
                        .background(
                            brush = Brush.linearGradient(page.gradient)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = page.icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(96.dp)
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = page.title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = page.description,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        lineHeight = 24.sp
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
