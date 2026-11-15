package com.mpi.klass

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.geometry.lerp as lerpOffset
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
    var isAtCenter by mutableStateOf(false)
        private set
    var axisFraction by mutableStateOf(0f)
        private set
    var isOpeningDestination by mutableStateOf(false)
        private set
    var openExpandProgress by mutableStateOf(0f)
        private set
    private var rawAccumulatedDelta by mutableStateOf(Offset.Zero)
    private var hasVibratedLeaveOrigin = false
    private var hasVibratedCenterArrival = false
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
        if (isOpeningDestination) return
        settleJob?.cancel()
        openExpandProgress = 0f
        axisFraction = 0f
        isAtCenter = false
        rawAccumulatedDelta = Offset.Zero
        hasVibratedLeaveOrigin = false
        hasVibratedCenterArrival = false
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
        val drag = activeDrag ?: return
        if (isOpeningDestination || rootSize.width <= 0 || rootSize.height <= 0) return

        rawAccumulatedDelta += dragAmount
        val screenCenter = Offset(rootSize.width / 2f, rootSize.height / 2f)
        val toCenter = screenCenter - drag.originCenterInRoot
        val totalDist = hypot(toCenter.x, toCenter.y).coerceAtLeast(120f)
        val unitX = toCenter.x / totalDist
        val unitY = toCenter.y / totalDist

        // Project movement strictly onto the straight line between Origin and Screen Center
        val projectedPx = rawAccumulatedDelta.x * unitX + rawAccumulatedDelta.y * unitY
        val rawT = (projectedPx / totalDist).coerceIn(0f, 1f)
        axisFraction = rawT

        // Subtle haptic feedback when shifting away from resting position
        if (!hasVibratedLeaveOrigin && rawT >= 0.04f) {
            hasVibratedLeaveOrigin = true
            triggerLightHaptic(isCenterArrival = false)
        }

        // Two-position snap: Origin <-> Equal Top/Side inset preview near source button + light vibration
        if (!isAtCenter && rawT >= 0.15f) {
            isAtCenter = true
            if (!hasVibratedCenterArrival) {
                hasVibratedCenterArrival = true
                triggerLightHaptic(isCenterArrival = true)
            }
        } else if (isAtCenter && rawT <= 0.08f) {
            isAtCenter = false
            hasVibratedCenterArrival = false
            triggerLightHaptic(isCenterArrival = false)
        }

        val constrainedT = if (isAtCenter) 1f else (rawT * 0.25f).coerceIn(0f, 0.06f)
        val anchorNearButton = lerpOffset(drag.originCenterInRoot, screenCenter, 0.28f)
        currentCenterInRoot = lerpOffset(drag.originCenterInRoot, anchorNearButton, constrainedT)
    }

    fun computeProgress(): Float {
        if (activeDrag == null) return 0f
        return if (isAtCenter) 1f else (axisFraction * 0.25f).coerceIn(0f, 0.06f)
    }

    fun isReadyToOpen(): Boolean = activeDrag != null && isAtCenter

    fun endDrag() {
        val drag = activeDrag ?: return
        if (isOpeningDestination) return
        settleJob?.cancel()
        if (isAtCenter) {
            // Smoothly glide toward center while expanding to fill the entire screen on release
            isOpeningDestination = true
            openExpandProgress = 0f
            drag.onTrigger()
            settleJob = scope.launch {
                animate(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
                ) { value, _ ->
                    openExpandProgress = value
                }
                activeDrag = null
                isAtCenter = false
                axisFraction = 0f
                isOpeningDestination = false
                openExpandProgress = 0f
            }
        } else {
            val startT = axisFraction
            isAtCenter = false
            settleJob = scope.launch {
                animate(
                    initialValue = startT,
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 130, easing = FastOutSlowInEasing),
                ) { value, _ ->
                    axisFraction = value
                    val screenCenter = Offset(rootSize.width / 2f, rootSize.height / 2f)
                    val anchorNearButton = lerpOffset(drag.originCenterInRoot, screenCenter, 0.28f)
                    currentCenterInRoot = lerpOffset(
                        drag.originCenterInRoot,
                        anchorNearButton,
                        (value * 0.25f).coerceIn(0f, 0.06f),
                    )
                }
                activeDrag = null
                axisFraction = 0f
            }
        }
    }

    fun cancelDrag() {
        val drag = activeDrag ?: return
        if (isOpeningDestination) return
        settleJob?.cancel()
        val wasAtCenter = isAtCenter
        isAtCenter = false
        val startT = if (wasAtCenter) 1f else axisFraction
        settleJob = scope.launch {
            animate(
                initialValue = startT,
                targetValue = 0f,
                animationSpec = tween(durationMillis = 160, easing = FastOutSlowInEasing),
            ) { value, _ ->
                axisFraction = value
                val screenCenter = Offset(rootSize.width / 2f, rootSize.height / 2f)
                val anchorNearButton = lerpOffset(drag.originCenterInRoot, screenCenter, 0.28f)
                currentCenterInRoot = lerpOffset(
                    drag.originCenterInRoot,
                    anchorNearButton,
                    value,
                )
            }
            activeDrag = null
            axisFraction = 0f
        }
    }

    fun forceClearIfMatches(id: String) {
        if (activeDrag?.id == id && !isOpeningDestination) {
            settleJob?.cancel()
            activeDrag = null
            isAtCenter = false
            axisFraction = 0f
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
 * Morphs into a miniature portrait phone-shaped window with generous rounded corners (30.dp)
 * anchored at an equal distance (36.dp) from the screen's top and side edges, then glides
 * toward screen center while expanding to full screen on release.
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

    val targetMorphProgress = if (controller.isAtCenter) 1f else (controller.axisFraction * 0.25f).coerceIn(0f, 0.06f)
    val smoothProgress by animateFloatAsState(
        targetValue = targetMorphProgress,
        animationSpec = spring(dampingRatio = 0.78f, stiffness = 480f),
        label = "SwipeWindowSmoothProgress",
    )
    val isReady = controller.isReadyToOpen()
    val expandT = if (controller.isOpeningDestination) controller.openExpandProgress.coerceIn(0f, 1f) else 0f
    // Move to center & spread out to 100% fullscreen (0f..0.78f), then dissolve into live screen (0.68f..1f)
    val expandGeometryT = (expandT / 0.78f).coerceIn(0f, 1f)
    val openingOverlayAlpha = if (controller.isOpeningDestination) {
        (1f - ((expandT - 0.68f) / 0.32f)).coerceIn(0f, 1f)
    } else 1f

    val rootWDp = with(density) { rootW.toDp() }
    val rootHDp = with(density) { rootH.toDp() }

    val startW = with(density) { drag.originSizePx.width.toDp().coerceAtLeast(48.dp) }
    val startH = with(density) { drag.originSizePx.height.toDp().coerceAtLeast(48.dp) }

    // Standard balanced miniature portrait phone-proportioned window (~1:1.48 ratio)
    val dragWinW = (rootWDp * 0.37f).coerceIn(136.dp, 176.dp)
    val dragWinH = (dragWinW * 1.48f)
        .coerceIn(196.dp, 256.dp)
        .coerceAtMost((rootHDp * 0.28f).coerceAtLeast(190.dp))

    val previewW = lerp(startW, dragWinW, smoothProgress)
    val previewH = lerp(startH, dragWinH, smoothProgress)
    val previewCorner = lerp(startW / 2f, 30.dp, smoothProgress)

    val cardW = lerp(previewW, rootWDp, expandGeometryT)
    val cardH = lerp(previewH, rootHDp, expandGeometryT)
    val cardCorner = lerp(previewCorner, 0.dp, expandGeometryT)

    // Preview sits at equal distance (36.dp) from screen top edge and corresponding side edge,
    // then glides smoothly toward exact screen center while spreading to full screen on release.
    val edgeGapPx = with(density) { 36.dp.toPx() }
    val dragWinWPx = with(density) { dragWinW.toPx() }
    val dragWinHPx = with(density) { dragWinH.toPx() }

    val targetAnchorX = if (drag.isLeftSide) {
        edgeGapPx + dragWinWPx / 2f
    } else {
        rootW - edgeGapPx - dragWinWPx / 2f
    }
    val targetAnchorY = edgeGapPx + dragWinHPx / 2f
    val equalEdgeAnchor = Offset(targetAnchorX, targetAnchorY)

    val screenCenter = Offset(rootW / 2f, rootH / 2f)
    val morphCenter = lerpOffset(drag.originCenterInRoot, equalEdgeAnchor, smoothProgress.coerceIn(0f, 1f))
    val cardCenter = lerpOffset(morphCenter, screenCenter, expandGeometryT)
    val cardWPx = with(density) { cardW.toPx() }
    val cardHPx = with(density) { cardH.toPx() }

    // Clamp strictly within visible screen bounds so neither icon nor window ever clips off-screen
    val safeMarginPx = with(density) { 8.dp.toPx() } * (1f - expandGeometryT)
    val rawLeft = cardCenter.x - cardWPx / 2f
    val rawTop = cardCenter.y - cardHPx / 2f
    val maxLeft = (rootW - cardWPx - safeMarginPx).coerceAtLeast(safeMarginPx)
    val maxTop = (rootH - cardHPx - safeMarginPx).coerceAtLeast(safeMarginPx)
    val clampedLeft = rawLeft.coerceIn(safeMarginPx.coerceAtMost(maxLeft), maxLeft)
    val clampedTop = rawTop.coerceIn(safeMarginPx.coerceAtMost(maxTop), maxTop)

    val cardBg = if (isDark) Color(0xFF1E293B) else Color.White
    val subSurface = if (isDark) Color(0xFF0F172A).copy(alpha = 0.7f) else Color(0xFFF1F5F9)
    val textColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val scrimAlpha = if (controller.isOpeningDestination) {
        (0.32f + 0.16f * expandGeometryT) * openingOverlayAlpha
    } else {
        (0.28f * smoothProgress).coerceIn(0f, 0.28f)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(100f)
            .background(Color.Black.copy(alpha = scrimAlpha.coerceIn(0f, 0.48f))),
    ) {
        Surface(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = clampedLeft.roundToInt(),
                        y = clampedTop.roundToInt(),
                    )
                }
                .size(width = cardW, height = cardH)
                .alpha(openingOverlayAlpha),
            shape = RoundedCornerShape(cardCorner),
            color = cardBg,
            border = BorderStroke(
                width = 0.6.dp, // Fine hairline border
                color = (if (isReady) drag.accentColor.copy(alpha = 0.55f) else drag.accentColor.copy(alpha = 0.28f))
                    .let { c -> c.copy(alpha = c.alpha * (1f - expandGeometryT * 1.35f).coerceIn(0f, 1f)) },
            ),
            shadowElevation = lerp(6.dp, 20.dp, smoothProgress) * (1f - expandGeometryT),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(cardCorner)),
            ) {
                // Phase 1: Pure circular icon fading cleanly as it leaves origin
                val iconOnlyAlpha = if (controller.isOpeningDestination) {
                    0f
                } else {
                    (1f - (smoothProgress / 0.40f)).coerceIn(0f, 1f)
                }
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

                // Phase 2: Portrait Phone-Proportioned Mini Window Preview
                val baseWindowAlpha = ((smoothProgress - 0.24f) / 0.46f).coerceIn(0f, 1f)
                val windowAlpha = if (controller.isOpeningDestination) {
                    (baseWindowAlpha + expandT * 1.5f).coerceIn(0f, 1f) * (1f - ((expandT - 0.45f) / 0.45f).coerceIn(0f, 1f))
                } else {
                    baseWindowAlpha
                }
                if (windowAlpha > 0.01f) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(windowAlpha)
                            .padding(horizontal = 11.dp, vertical = 10.dp),
                    ) {
                        // Mini Phone Top Status Pill + App Header Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = drag.accentColor.copy(alpha = 0.14f),
                                border = BorderStroke(0.6.dp, drag.accentColor.copy(alpha = 0.35f)),
                                modifier = Modifier.size(22.dp).clip(CircleShape),
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    drag.iconContent()
                                }
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = drag.title.localized(),
                                fontSize = 11.sp,
                                fontFamily = hindSiliguri,
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider(color = drag.accentColor.copy(alpha = 0.15f), thickness = 0.6.dp)
                        Spacer(modifier = Modifier.height(6.dp))

                        // Portrait Phone Hero Banner Card
                        Surface(
                            modifier = Modifier.fillMaxWidth().height(38.dp),
                            shape = RoundedCornerShape(11.dp),
                            color = drag.accentColor.copy(alpha = 0.10f),
                            border = BorderStroke(0.6.dp, drag.accentColor.copy(alpha = 0.22f)),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(drag.accentColor.copy(alpha = 0.28f)),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.75f)
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(drag.accentColor.copy(alpha = 0.36f)),
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.48f)
                                            .height(5.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(textColor.copy(alpha = 0.15f)),
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Stacked Portrait Screen Skeleton Cards
                        repeat(2) { idx ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                shape = RoundedCornerShape(11.dp),
                                color = subSurface,
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalArrangement = Arrangement.Center,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(if (idx == 0) 0.72f else 0.54f)
                                            .height(5.5.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(textColor.copy(alpha = 0.16f)),
                                    )
                                    Spacer(modifier = Modifier.height(3.5.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.88f)
                                            .height(4.5.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(textColor.copy(alpha = 0.09f)),
                                    )
                                }
                            }
                            if (idx == 0) Spacer(modifier = Modifier.height(5.dp))
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Bottom Release Action Pill inside Portrait Phone Window
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (isReady) drag.accentColor else subSurface,
                            ) {
                                Text(
                                    text = if (isReady) "ছেড়ে দিন".localized() else "মাঝখানে".localized(),
                                    fontSize = 8.5.sp,
                                    fontFamily = hindSiliguri,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isReady) Color.White else textColor.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                    maxLines = 1,
                                )
                            }
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
