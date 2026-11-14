package com.mpi.klass

import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.mpi.klass.theme.CloseIcon
import com.mpi.klass.theme.SparkleIcon
import klass.shared.generated.resources.Res
import klass.shared.generated.resources.home
import klass.shared.generated.resources.menu
import klass.shared.generated.resources.notification
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

data class ActiveSwipeIconDrag(
    val id: String,
    val title: String,
    val accentColor: Color,
    val originCenterInRoot: Offset,
    val originSizePx: IntSize,
    val isLeftSide: Boolean,
    val iconContent: @Composable () -> Unit,
    val onTrigger: () -> Unit,
)

class SwipeIconDragController(
    private val scope: CoroutineScope,
) {
    var rootOffsetInWindow by mutableStateOf(Offset.Zero)
    var rootSize by mutableStateOf(IntSize.Zero)
    var activeDrag by mutableStateOf<ActiveSwipeIconDrag?>(null)
        private set

    var currentCenterInRoot by mutableStateOf(Offset.Zero)
        private set
    private var settleJob: Job? = null

    fun startDrag(
        id: String,
        title: String,
        accentColor: Color,
        originCenterInRoot: Offset,
        originSizePx: IntSize,
        isLeftSide: Boolean,
        iconContent: @Composable () -> Unit,
        onTrigger: () -> Unit,
    ) {
        settleJob?.cancel()
        currentCenterInRoot = originCenterInRoot
        activeDrag = ActiveSwipeIconDrag(
            id = id,
            title = title,
            accentColor = accentColor,
            originCenterInRoot = originCenterInRoot,
            originSizePx = originSizePx,
            isLeftSide = isLeftSide,
            iconContent = iconContent,
            onTrigger = onTrigger,
        )
    }

    fun updateDrag(dragAmount: Offset) {
        if (activeDrag == null) return
        val maxW = rootSize.width.toFloat().coerceAtLeast(1f)
        val maxH = rootSize.height.toFloat().coerceAtLeast(1f)
        currentCenterInRoot = Offset(
            x = (currentCenterInRoot.x + dragAmount.x).coerceIn(24f, maxW - 24f),
            y = (currentCenterInRoot.y + dragAmount.y).coerceIn(24f, maxH - 24f),
        )
    }

    fun computeProgress(): Float {
        val drag = activeDrag ?: return 0f
        if (rootSize.width <= 0 || rootSize.height <= 0) return 0f

        val screenCenter = Offset(rootSize.width / 2f, rootSize.height / 2f)
        val origin = drag.originCenterInRoot
        val current = currentCenterInRoot

        val totalDistToCenter = hypot(screenCenter.x - origin.x, screenCenter.y - origin.y).coerceAtLeast(120f)
        val currentDistToCenter = hypot(screenCenter.x - current.x, screenCenter.y - current.y)

        return ((totalDistToCenter - currentDistToCenter) / totalDistToCenter).coerceIn(0f, 1f)
    }

    fun isReadyToOpen(): Boolean {
        if (activeDrag == null) return false
        return computeProgress() >= 0.48f
    }

    fun endDrag() {
        val drag = activeDrag ?: return
        settleJob?.cancel()
        if (isReadyToOpen()) {
            // Only open on finger release! Clear overlay and navigate immediately.
            activeDrag = null
            drag.onTrigger()
        } else {
            val startPos = currentCenterInRoot
            val endPos = drag.originCenterInRoot
            settleJob = scope.launch {
                animate(
                    typeConverter = Offset.VectorConverter,
                    initialValue = startPos,
                    targetValue = endPos,
                    animationSpec = spring(dampingRatio = 0.80f, stiffness = 460f),
                ) { value, _ ->
                    currentCenterInRoot = value
                }
                activeDrag = null
            }
        }
    }

    fun cancelDrag() {
        val drag = activeDrag ?: return
        settleJob?.cancel()
        val startPos = currentCenterInRoot
        val endPos = drag.originCenterInRoot
        settleJob = scope.launch {
            animate(
                typeConverter = Offset.VectorConverter,
                initialValue = startPos,
                targetValue = endPos,
                animationSpec = spring(dampingRatio = 0.80f, stiffness = 460f),
            ) { value, _ ->
                currentCenterInRoot = value
            }
            activeDrag = null
        }
    }

    fun forceClearIfMatches(id: String) {
        if (activeDrag?.id == id) {
            settleJob?.cancel()
            activeDrag = null
        }
    }
}

val LocalSwipeIconDragController = compositionLocalOf<SwipeIconDragController?> { null }

@Composable
fun rememberSwipeIconDragController(): SwipeIconDragController {
    val scope = rememberCoroutineScope()
    return remember(scope) { SwipeIconDragController(scope) }
}

/**
 * Root-level overlay rendered above ALL headers, buttons, tables, and bars (zIndex 100f).
 * Smoothly morphs the dragged icon directly under the touch point into an expanding floating
 * screen window as it approaches center, and ONLY opens the destination when touch is released.
 */
@Composable
fun SwipeDragWindowOverlay(
    controller: SwipeIconDragController,
    isDark: Boolean,
    hindSiliguri: FontFamily,
) {
    val drag = controller.activeDrag ?: return
    val density = LocalDensity.current
    val rootW = controller.rootSize.width.toFloat().coerceAtLeast(1f)
    val rootH = controller.rootSize.height.toFloat().coerceAtLeast(1f)

    val rawProgress = controller.computeProgress()
    val isReady = controller.isReadyToOpen()

    val rootWDp = with(density) { rootW.toDp() }
    val rootHDp = with(density) { rootH.toDp() }

    val startW = with(density) { drag.originSizePx.width.toDp().coerceAtLeast(48.dp) }
    val startH = with(density) { drag.originSizePx.height.toDp().coerceAtLeast(48.dp) }
    val targetWinW = (rootWDp * 0.72f).coerceIn(220.dp, 300.dp)
    val targetWinH = (rootHDp * 0.48f).coerceIn(280.dp, 420.dp)

    // Strictly proportional to distance toward center so growth is smooth and gradual
    val cardW = lerp(startW, targetWinW, rawProgress)
    val cardH = lerp(startH, targetWinH, rawProgress)
    val cardCorner = lerp(startW / 2f, 24.dp, rawProgress)

    // Anchor window center directly at the touch/finger position without shifting away
    val fingerCenter = controller.currentCenterInRoot
    val cardWPx = with(density) { cardW.toPx() }
    val cardHPx = with(density) { cardH.toPx() }

    val cardBg = if (isDark) Color(0xFF1E293B) else Color.White
    val subSurface = if (isDark) Color(0xFF0F172A).copy(alpha = 0.7f) else Color(0xFFF1F5F9)
    val textColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(100f)
            .background(Color.Black.copy(alpha = (0.40f * rawProgress).coerceIn(0f, 0.40f))),
    ) {
        Surface(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = (fingerCenter.x - cardWPx / 2f).roundToInt(),
                        y = (fingerCenter.y - cardHPx / 2f).roundToInt(),
                    )
                }
                .size(width = cardW, height = cardH),
            shape = RoundedCornerShape(cardCorner),
            color = cardBg,
            border = BorderStroke(
                width = if (isReady) 2.2.dp else 1.2.dp,
                color = if (isReady) drag.accentColor else drag.accentColor.copy(alpha = 0.45f),
            ),
            shadowElevation = lerp(6.dp, 24.dp, rawProgress),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(cardCorner)),
            ) {
                // Phase 1: Pure circular icon gradually inflating during early drag
                val iconOnlyAlpha = (1f - (rawProgress / 0.42f)).coerceIn(0f, 1f)
                if (iconOnlyAlpha > 0.01f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(iconOnlyAlpha),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = startW, height = startH)
                                .clip(CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            drag.iconContent()
                        }
                    }
                }

                // Phase 2: Expanding Floating Screen Window Preview fading in gradually
                val windowAlpha = ((rawProgress - 0.26f) / 0.44f).coerceIn(0f, 1f)
                if (windowAlpha > 0.01f) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(windowAlpha)
                            .padding(14.dp),
                    ) {
                        // Simulated Top Window Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f),
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = drag.accentColor.copy(alpha = 0.14f),
                                    border = BorderStroke(1.dp, drag.accentColor.copy(alpha = 0.4f)),
                                    modifier = Modifier.size(34.dp).clip(CircleShape),
                                ) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        drag.iconContent()
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = drag.title.localized(),
                                    fontSize = 14.sp,
                                    fontFamily = hindSiliguri,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (isReady) drag.accentColor else subSurface,
                            ) {
                                Text(
                                    text = if (isReady) "খুলতে ছেড়ে দিন".localized() else "মাঝখানে আনুন".localized(),
                                    fontSize = 10.sp,
                                    fontFamily = hindSiliguri,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isReady) Color.White else textColor.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    maxLines = 1,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = drag.accentColor.copy(alpha = 0.18f), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Simulated Mini Window Cards matching a real screen preview
                        Surface(
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = drag.accentColor.copy(alpha = 0.10f),
                            border = BorderStroke(1.dp, drag.accentColor.copy(alpha = 0.25f)),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(drag.accentColor.copy(alpha = 0.25f)),
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .width(100.dp)
                                            .height(10.dp)
                                            .clip(RoundedCornerShape(5.dp))
                                            .background(drag.accentColor.copy(alpha = 0.35f)),
                                    )
                                    Box(
                                        modifier = Modifier
                                            .width(65.dp)
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(textColor.copy(alpha = 0.15f)),
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        repeat(2) { idx ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                color = subSurface,
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(12.dp),
                                    verticalArrangement = Arrangement.Center,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(if (idx == 0) 0.72f else 0.56f)
                                            .height(10.dp)
                                            .clip(RoundedCornerShape(5.dp))
                                            .background(textColor.copy(alpha = 0.16f)),
                                    )
                                    Spacer(modifier = Modifier.height(7.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.88f)
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(textColor.copy(alpha = 0.09f)),
                                    )
                                }
                            }
                            if (idx == 0) Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * A circular/rounded interactive button Surface that supports normal click AND
 * dragging/swiping the icon toward the center of the screen via the root-level SwipeDragWindowOverlay.
 * Guaranteed to stay on the top-most visual layer and open ONLY when touch is released.
 */
@Composable
fun SwipeToCenterIconSurface(
    dragId: String,
    previewTitle: String,
    accentColor: Color,
    onTrigger: () -> Unit,
    isLeftSide: Boolean,
    shape: Shape,
    color: Color,
    border: BorderStroke?,
    shadowElevation: Dp = 4.dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val controller = LocalSwipeIconDragController.current
    val currentOnTrigger by rememberUpdatedState(onTrigger)
    val currentTitle by rememberUpdatedState(previewTitle)
    val currentContent by rememberUpdatedState(content)

    var myCenterInWindow by remember { mutableStateOf(Offset.Zero) }
    var mySizePx by remember { mutableStateOf(IntSize.Zero) }
    val isBeingDragged = controller?.activeDrag?.id == dragId

    DisposableEffect(dragId) {
        onDispose {
            controller?.forceClearIfMatches(dragId)
        }
    }

    Box(
        modifier = modifier
            .onGloballyPositioned { coords ->
                val pos = coords.positionInWindow()
                mySizePx = coords.size
                myCenterInWindow = Offset(pos.x + coords.size.width / 2f, pos.y + coords.size.height / 2f)
            }
            .graphicsLayer {
                alpha = if (isBeingDragged) 0f else 1f
            }
            .pointerInput(dragId, isLeftSide, controller) {
                if (controller == null) return@pointerInput
                detectDragGestures(
                    onDragStart = {
                        val originInRoot = myCenterInWindow - controller.rootOffsetInWindow
                        controller.startDrag(
                            id = dragId,
                            title = currentTitle,
                            accentColor = accentColor,
                            originCenterInRoot = originInRoot,
                            originSizePx = mySizePx,
                            isLeftSide = isLeftSide,
                            iconContent = { currentContent() },
                            onTrigger = { currentOnTrigger() },
                        )
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        controller.updateDrag(dragAmount)
                    },
                    onDragEnd = {
                        controller.endDrag()
                    },
                    onDragCancel = {
                        controller.cancelDrag()
                    },
                )
            },
    ) {
        Surface(
            onClick = { currentOnTrigger() },
            shape = shape,
            color = color,
            border = border,
            shadowElevation = shadowElevation,
            modifier = Modifier.fillMaxSize(),
            content = content,
        )
    }
}

/**
 * Home Routine header with sky-blue gradient and center-swipeable circular icons.
 */
@Composable
fun HomeRoutineHeader(
    isDark: Boolean,
    cardBg: Color,
    photoUrl: String?,
    avatarSeed: String,
    onProfileClick: () -> Unit,
    onAiClick: () -> Unit,
    onNotificationClick: () -> Unit,
) {
    val darkerSkyBlue = Color(0xFF5B9BD5)
    val headerGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            darkerSkyBlue,
        ),
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent,
        shadowElevation = 6.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(headerGradient),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SwipeToCenterIconSurface(
                        dragId = "home_profile",
                        previewTitle = "প্রোফাইল",
                        accentColor = Color(0xFF0284C7),
                        onTrigger = onProfileClick,
                        isLeftSide = true,
                        shape = CircleShape,
                        color = cardBg,
                        border = BorderStroke(1.2.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                        modifier = Modifier.size(50.dp).clip(CircleShape),
                        shadowElevation = 5.dp,
                    ) {
                        if (!photoUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = resolveProfileImageModel(photoUrl),
                                contentDescription = "Profile Photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                            )
                        } else {
                            DynamicAvatar(
                                seed = avatarSeed,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        onClick = { },
                        shape = CircleShape,
                        color = cardBg,
                        border = BorderStroke(1.2.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .height(40.dp)
                            .width(160.dp),
                        shadowElevation = 2.dp,
                    ) {}
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SwipeToCenterIconSurface(
                        dragId = "home_ai",
                        previewTitle = "Gemini AI সহকারী",
                        accentColor = Color(0xFF8B5CF6),
                        onTrigger = onAiClick,
                        isLeftSide = false,
                        shape = CircleShape,
                        color = cardBg,
                        border = BorderStroke(1.2.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                        modifier = Modifier.size(50.dp),
                        shadowElevation = 4.dp,
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = SparkleIcon,
                                contentDescription = "AI সহকারী".localized(),
                                tint = Color(0xFF8B5CF6),
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }

                    SwipeToCenterIconSurface(
                        dragId = "home_notification",
                        previewTitle = "নোটিফিকেশন",
                        accentColor = Color(0xFFEF4444),
                        onTrigger = onNotificationClick,
                        isLeftSide = false,
                        shape = CircleShape,
                        color = cardBg,
                        border = BorderStroke(1.2.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                        modifier = Modifier.size(50.dp),
                        shadowElevation = 4.dp,
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.notification),
                                contentDescription = "নোটিফিকেশন".localized(),
                                tint = if (isDark) Color.White else Color.Black,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Floating top-to-bottom transparent fading header for Menu, Profile, and Notification screens.
 * Opaque at the top edge and smoothly fades to Color.Transparent at the bottom edge.
 */
@Composable
fun FloatingFadeTopHeader(
    title: String,
    isDark: Boolean,
    cardBg: Color,
    textColor: Color,
    hindSiliguri: FontFamily,
    showLeftClose: Boolean = true,
    showRightClose: Boolean = false,
    onCloseClick: () -> Unit,
    onAiClick: () -> Unit,
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bgColor = MaterialTheme.colorScheme.background
    val topToBottomTransparentBrush = Brush.verticalGradient(
        0.0f to bgColor,
        0.48f to bgColor.copy(alpha = 0.88f),
        0.80f to bgColor.copy(alpha = 0.35f),
        1.0f to Color.Transparent,
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(topToBottomTransparentBrush)
            .zIndex(15f),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 14.dp)
                .padding(top = 8.dp, bottom = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (showLeftClose) {
                    SwipeToCenterIconSurface(
                        dragId = "header_left_close_$title",
                        previewTitle = "ফিরে যান",
                        accentColor = Color(0xFF0EA5E9),
                        onTrigger = onCloseClick,
                        isLeftSide = true,
                        shape = CircleShape,
                        color = cardBg,
                        border = BorderStroke(1.2.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                        modifier = Modifier.size(50.dp),
                        shadowElevation = 4.dp,
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = CloseIcon,
                                contentDescription = "বন্ধ করুন".localized(),
                                tint = if (isDark) Color.White else Color.Black,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontFamily = hindSiliguri,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SwipeToCenterIconSurface(
                    dragId = "header_ai_$title",
                    previewTitle = "Gemini AI সহকারী",
                    accentColor = Color(0xFF8B5CF6),
                    onTrigger = onAiClick,
                    isLeftSide = false,
                    shape = CircleShape,
                    color = cardBg,
                    border = BorderStroke(1.2.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                    modifier = Modifier.size(50.dp),
                    shadowElevation = 4.dp,
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = SparkleIcon,
                            contentDescription = "AI সহকারী".localized(),
                            tint = Color(0xFF8B5CF6),
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }

                if (showRightClose) {
                    SwipeToCenterIconSurface(
                        dragId = "header_right_close_$title",
                        previewTitle = "ফিরে যান",
                        accentColor = Color(0xFF0EA5E9),
                        onTrigger = onCloseClick,
                        isLeftSide = false,
                        shape = CircleShape,
                        color = cardBg,
                        border = BorderStroke(1.2.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                        modifier = Modifier.size(50.dp),
                        shadowElevation = 4.dp,
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = CloseIcon,
                                contentDescription = "বন্ধ করুন".localized(),
                                tint = if (isDark) Color.White else Color.Black,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                } else {
                    SwipeToCenterIconSurface(
                        dragId = "header_notification_$title",
                        previewTitle = "নোটিফিকেশন",
                        accentColor = Color(0xFFEF4444),
                        onTrigger = onNotificationClick,
                        isLeftSide = false,
                        shape = CircleShape,
                        color = cardBg,
                        border = BorderStroke(1.2.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                        modifier = Modifier.size(50.dp),
                        shadowElevation = 4.dp,
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.notification),
                                contentDescription = "নোটিফিকেশন".localized(),
                                tint = if (isDark) Color.White else Color.Black,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Bottom navigation bar rendered inside each page of the Home <-> Menu HorizontalPager
 * so swiping left/right moves the header, content, and bottom bar together as one full screen.
 */
@Composable
fun HomeMenuBottomBar(
    isHomeSelected: Boolean,
    isDark: Boolean,
    cardBg: Color,
    hindSiliguri: FontFamily,
    homeShape: Shape,
    menuShape: Shape,
    onHomeClick: () -> Unit,
    onMenuClick: () -> Unit,
) {
    val isMenuSelected = !isHomeSelected

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = cardBg,
        shadowElevation = 8.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(if (isDark) Color.White else Color.Black),
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .padding(horizontal = 2.dp, vertical = 2.dp),
            ) {
                val homeInteractionSource = remember { MutableInteractionSource() }
                val isHomePressed by homeInteractionSource.collectIsPressedAsState()

                val menuInteractionSource = remember { MutableInteractionSource() }
                val isMenuPressed by menuInteractionSource.collectIsPressedAsState()

                Row(
                    modifier = Modifier
                        .offset(y = 3.dp)
                        .fillMaxWidth()
                        .height(63.dp),
                    horizontalArrangement = Arrangement.spacedBy(1.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(63.dp)
                            .clip(homeShape)
                            .background(if (isHomeSelected) Color(0xFF8E7864) else Color(0xFF94A3B8)),
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(63.dp)
                            .clip(menuShape)
                            .background(if (isMenuSelected) Color(0xFF8E7864) else Color(0xFF94A3B8)),
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(63.dp),
                    horizontalArrangement = Arrangement.spacedBy(1.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Button(
                        onClick = onHomeClick,
                        interactionSource = homeInteractionSource,
                        modifier = Modifier
                            .weight(1f)
                            .height(63.dp)
                            .offset(y = if (isHomePressed) 3.dp else 0.dp),
                        shape = homeShape,
                        border = BorderStroke(0.5.dp, if (isHomeSelected) Color(0xFF8E7864) else Color(0xFF94A3B8)),
                        contentPadding = PaddingValues(2.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isHomeSelected) Color(0xFF8CB9D9) else (if (isDark) Color(0xFF1E293B) else Color.White),
                            contentColor = if (isDark) Color.White else Color.Black,
                        ),
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterVertically),
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.home),
                                contentDescription = "রুটিন".localized(),
                                tint = if (isDark) Color.White else Color.Black,
                                modifier = Modifier.size(24.dp),
                            )
                            Text(
                                text = "রুটিন".localized(),
                                fontSize = 11.5.sp,
                                fontFamily = hindSiliguri,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color.Black,
                            )
                        }
                    }

                    Button(
                        onClick = onMenuClick,
                        interactionSource = menuInteractionSource,
                        modifier = Modifier
                            .weight(1f)
                            .height(63.dp)
                            .offset(y = if (isMenuPressed) 3.dp else 0.dp),
                        shape = menuShape,
                        border = BorderStroke(0.5.dp, if (isMenuSelected) Color(0xFF8E7864) else Color(0xFF94A3B8)),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isMenuSelected) Color(0xFF8CB9D9) else (if (isDark) Color(0xFF1E293B) else Color.White),
                            contentColor = if (isDark) Color.White else Color.Black,
                        ),
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterVertically),
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.menu),
                                contentDescription = "মেনু".localized(),
                                tint = if (isDark) Color.White else Color.Black,
                                modifier = Modifier.size(22.dp),
                            )
                            Text(
                                text = "মেনু".localized(),
                                fontSize = 12.sp,
                                fontFamily = hindSiliguri,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color.Black,
                            )
                        }
                    }
                }
            }
        }
    }
}
