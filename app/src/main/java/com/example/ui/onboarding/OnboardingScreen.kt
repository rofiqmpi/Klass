package com.example.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.KlassDarkBackground
import com.example.ui.theme.KlassDarkSurface
import com.example.ui.theme.KlassPrimary
import com.example.ui.theme.KlassSecondary
import com.example.ui.theme.KlassTertiary
import kotlinx.coroutines.launch

data class OnboardingPageData(
    val imageRes: Int,
    val stepBadge: String,
    val titleEn: String,
    val titleBn: String,
    val descEn: String,
    val descBn: String,
    val highlights: List<Pair<String, String>>
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pages = remember {
        listOf(
            OnboardingPageData(
                imageRes = R.drawable.img_welcome_classroom,
                stepBadge = "Step 1 of 3 • ধাপ ১",
                titleEn = "Welcome to Klass",
                titleBn = "ক্লাসে স্বাগতম",
                descEn = "Experience seamless smart classroom management, digitized courses, and live interactive lectures all in one unified platform.",
                descBn = "একটি আধুনিক প্ল্যাটফর্মে আপনার ক্লাস, পাঠ্যবিষয় এবং প্রয়োজনীয় শিক্ষা উপকরণ সহজে পরিচালনা করুন।",
                highlights = listOf(
                    "Smart Classroom" to "স্মার্ট ক্লাসরুম",
                    "Digital Resources" to "ডিজিটাল পাঠ্যবই"
                )
            ),
            OnboardingPageData(
                imageRes = R.drawable.img_welcome_schedule,
                stepBadge = "Step 2 of 3 • ধাপ ২",
                titleEn = "Routine & Attendance",
                titleBn = "রুটিন ও উপস্থিতি ট্র্যাকিং",
                descEn = "Keep track of daily class routines, exam schedules, and record student attendance effortlessly with timely push alerts.",
                descBn = "দৈনন্দিন ক্লাসের সময়সূচি, পরীক্ষার রুটিন এবং উপস্থিতির হিসাব রাখুন সহজে ও নিখুঁতভাবে।",
                highlights = listOf(
                    "Live Schedule" to "সরাসরি রুটিন",
                    "Easy Attendance" to "সহজ উপস্থিতি"
                )
            ),
            OnboardingPageData(
                imageRes = R.drawable.img_welcome_community,
                stepBadge = "Step 3 of 3 • ধাপ ৩",
                titleEn = "Connect & Collaborate",
                titleBn = "সহযোগিতা ও সাফল্য",
                descEn = "Connect with teachers and peers, submit assignments on time, and track your academic progress together.",
                descBn = "শিক্ষক ও সহপাঠীদের সাথে সার্বক্ষণিক যোগাযোগ রাখুন, অ্যাসাইনমেন্ট ও ফলাফল শেয়ার করুন।",
                highlights = listOf(
                    "Study Groups" to "স্টাডি গ্রুপ ও চ্যাট",
                    "Academic Growth" to "ফলাফল ও মূল্যায়ন"
                )
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()
    var isBanglaPrimary by remember { mutableStateOf(true) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("onboarding_screen"),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Language toggle button
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clip(RoundedCornerShape(20.dp))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .testTag("language_toggle_button")
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        TextButton(
                            onClick = { isBanglaPrimary = !isBanglaPrimary },
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = "Switch Language",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBanglaPrimary) "বাংলা (BN)" else "English (EN)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Skip button
                TextButton(
                    onClick = onFinished,
                    modifier = Modifier.testTag("skip_button")
                ) {
                    Text(
                        text = if (isBanglaPrimary) "এড়িয়ে যান (Skip)" else "Skip",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Page Indicator Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.testTag("pager_indicator")
                ) {
                    repeat(pages.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        val width by animateDpAsState(
                            targetValue = if (isSelected) 28.dp else 8.dp,
                            label = "indicator_width"
                        )
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(width)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Bottom Buttons
                if (pagerState.currentPage == pages.size - 1) {
                    // Last page: Get Started
                    Button(
                        onClick = onFinished,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("get_started_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = if (isBanglaPrimary) "শুরু করুন (Get Started)" else "Get Started",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                } else {
                    // Pages 1 & 2: Skip and Next
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onFinished,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("skip_bottom_button")
                        ) {
                            Text(
                                text = if (isBanglaPrimary) "বাদ দিন" else "Skip",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1.3f)
                                .height(50.dp)
                                .testTag("next_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                text = if (isBanglaPrimary) "পরবর্তী" else "Next",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("onboarding_horizontal_pager")
        ) { pageIndex ->
            val page = pages[pageIndex]
            OnboardingPageContent(page = page, isBanglaPrimary = isBanglaPrimary)
        }
    }
}

@Composable
private fun OnboardingPageContent(
    page: OnboardingPageData,
    isBanglaPrimary: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Step Badge
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text(
                text = page.stepBadge,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
            )
        }

        // Illustration Card
        Card(
            modifier = Modifier
                .size(260.dp)
                .shadow(elevation = 12.dp, shape = RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp)),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Image(
                painter = painterResource(id = page.imageRes),
                contentDescription = page.titleEn,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Title
        Text(
            text = if (isBanglaPrimary) page.titleBn else page.titleEn,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            lineHeight = 32.sp
        )

        // Sub-title in alternate language
        Text(
            text = if (isBanglaPrimary) page.titleEn else page.titleBn,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Description
        Text(
            text = if (isBanglaPrimary) page.descBn else page.descEn,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Feature Highlight Pills
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            page.highlights.forEach { (en, bn) ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    border = null
                ) {
                    Text(
                        text = if (isBanglaPrimary) "✦ $bn" else "✦ $en",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}
