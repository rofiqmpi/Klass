package com.mpi.klass

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.mpi.klass.theme.AppTheme
import com.mpi.klass.theme.CheckIcon
import com.mpi.klass.theme.DownloadIcon
import com.mpi.klass.theme.KlassFeaturePalette
import com.mpi.klass.theme.RefreshIcon
import com.mpi.klass.theme.ShieldIcon
import com.mpi.klass.theme.ThemeMode
import com.mpi.klass.theme.WarningIcon
import com.mpi.klass.theme.loadThemeMode

internal data class RemoteReleaseInfo(
    val version: String,
    val apkUrl: String,
    val notes: String = "",
    val force: Boolean = false,
)

internal sealed interface InAppUpdateUiState {
    data object Hidden : InAppUpdateUiState
    data class Available(
        val release: RemoteReleaseInfo,
        val installedVersion: String,
    ) : InAppUpdateUiState
    data class PermissionRequired(
        val release: RemoteReleaseInfo,
    ) : InAppUpdateUiState
    data class Downloading(
        val release: RemoteReleaseInfo,
        val downloadedBytes: Long,
        val totalBytes: Long,
    ) : InAppUpdateUiState
    data class ReadyToInstall(
        val release: RemoteReleaseInfo,
    ) : InAppUpdateUiState
    data class Error(
        val release: RemoteReleaseInfo,
        val message: String,
    ) : InAppUpdateUiState
}

private fun formatMegabytes(bytes: Long): String {
    if (bytes <= 0L) return "0.0 MB"
    val tenths = ((bytes * 10L) / (1024L * 1024L)).coerceAtLeast(1L)
    val whole = tenths / 10L
    val frac = tenths % 10L
    return "$whole.$frac MB"
}

@Composable
internal fun InAppUpdateOverlay(
    state: InAppUpdateUiState,
    onStartDownload: (RemoteReleaseInfo) -> Unit,
    onOpenInstallSettings: () -> Unit,
    onInstallNow: (RemoteReleaseInfo) -> Unit,
) {
    val visible = state !is InAppUpdateUiState.Hidden
    val themeMode = remember(visible) { loadThemeMode() }
    val isDark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.DEFAULT -> isSystemInDarkTheme()
    }
    AppTheme(themeMode = themeMode) {
        val cs = MaterialTheme.colorScheme
        val font = FontFamily.Default
        val profileAccent = KlassFeaturePalette.profile(isDark)
        val routineAccent = KlassFeaturePalette.routine(isDark)
        val successAccent = KlassFeaturePalette.success(isDark)
        val errorAccent = KlassFeaturePalette.error(isDark)

        BackHandler(enabled = visible) {
        }

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(200)) + scaleIn(tween(220), initialScale = 0.94f),
            exit = fadeOut(tween(160)) + scaleOut(tween(160), targetScale = 0.96f),
            modifier = Modifier
                .fillMaxSize()
                .zIndex(200f),
        ) {
            val cardSurface = if (isDark) Color(0xFF131D31) else Color.White
            val cardBorder = if (isDark) profileAccent.borderTint else Color(0xFFE2E8F0)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.58f))
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                    }
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    shape = RoundedCornerShape(26.dp),
                    color = cardSurface,
                    border = BorderStroke(1.dp, cardBorder),
                    shadowElevation = 20.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 360.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {},
                        )
                        .testTag("in_app_update_dialog"),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        val headerIcon = when (state) {
                            is InAppUpdateUiState.ReadyToInstall -> CheckIcon
                            is InAppUpdateUiState.PermissionRequired -> ShieldIcon
                            is InAppUpdateUiState.Error -> WarningIcon
                            else -> DownloadIcon
                        }
                        val headerAccent = when (state) {
                            is InAppUpdateUiState.ReadyToInstall -> successAccent.primary
                            is InAppUpdateUiState.Error -> errorAccent.primary
                            else -> profileAccent.primary
                        }

                        Surface(
                            shape = CircleShape,
                            color = headerAccent.copy(alpha = if (isDark) 0.22f else 0.12f),
                            border = BorderStroke(1.dp, headerAccent.copy(alpha = 0.35f)),
                            modifier = Modifier.size(46.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = headerIcon,
                                    contentDescription = null,
                                    tint = headerAccent,
                                    modifier = Modifier.size(21.dp),
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        when (state) {
                            is InAppUpdateUiState.Available -> {
                                Text(
                                    text = "Update Available",
                                    color = cs.onSurface,
                                    fontFamily = font,
                                    fontSize = 18.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = profileAccent.softTint,
                                    border = BorderStroke(0.75.dp, profileAccent.borderTint),
                                ) {
                                    Text(
                                        text = "v${state.installedVersion}  →  ${state.release.version}",
                                        color = profileAccent.primary,
                                        fontFamily = font,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "A newer version of Klass is ready. Update now to continue.",
                                    color = cs.onSurfaceVariant,
                                    fontFamily = font,
                                    fontSize = 13.sp,
                                    lineHeight = 18.5.sp,
                                    textAlign = TextAlign.Center,
                                )
                                if (state.release.notes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = cs.surfaceContainerLow.copy(alpha = if (isDark) 0.7f else 0.9f),
                                        border = BorderStroke(0.75.dp, cs.outlineVariant.copy(alpha = 0.5f)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(68.dp),
                                    ) {
                                        Text(
                                            text = state.release.notes.take(400),
                                            color = cs.onSurface,
                                            fontFamily = font,
                                            fontSize = 12.sp,
                                            lineHeight = 17.sp,
                                            modifier = Modifier
                                                .verticalScroll(rememberScrollState())
                                                .padding(10.dp),
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                GradientActionButton(
                                    label = "Update Now",
                                    icon = DownloadIcon,
                                    colors = listOf(profileAccent.primary, routineAccent.primary),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp)
                                        .testTag("in_app_update_confirm_button"),
                                    onClick = { onStartDownload(state.release) },
                                )
                            }

                            is InAppUpdateUiState.PermissionRequired -> {
                                Text(
                                    text = "Install Permission Required",
                                    color = cs.onSurface,
                                    fontFamily = font,
                                    fontSize = 18.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Enable \"Allow from this source\" once so Klass can install updates directly.",
                                    color = cs.onSurfaceVariant,
                                    fontFamily = font,
                                    fontSize = 13.5.sp,
                                    lineHeight = 19.sp,
                                    textAlign = TextAlign.Center,
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                GradientActionButton(
                                    label = "Allow & Update",
                                    icon = ShieldIcon,
                                    colors = listOf(profileAccent.primary, routineAccent.primary),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp)
                                        .testTag("in_app_update_permission_button"),
                                    onClick = onOpenInstallSettings,
                                )
                            }

                            is InAppUpdateUiState.Downloading -> {
                                val knownTotal = state.totalBytes > 0L
                                val fraction = if (knownTotal) {
                                    (state.downloadedBytes.toFloat() / state.totalBytes.toFloat()).coerceIn(0f, 1f)
                                } else {
                                    null
                                }
                                val percent = fraction?.let { (it * 100f).toInt().coerceIn(0, 100) }
                                Text(
                                    text = "Downloading Update…",
                                    color = cs.onSurface,
                                    fontFamily = font,
                                    fontSize = 18.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Version ${state.release.version}",
                                    color = cs.onSurfaceVariant,
                                    fontFamily = font,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center,
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                if (fraction != null) {
                                    LinearProgressIndicator(
                                        progress = { fraction },
                                        color = profileAccent.primary,
                                        trackColor = cs.surfaceVariant,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(7.dp)
                                            .clip(CircleShape)
                                            .testTag("in_app_update_progress"),
                                    )
                                } else {
                                    LinearProgressIndicator(
                                        color = profileAccent.primary,
                                        trackColor = cs.surfaceVariant,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(7.dp)
                                            .clip(CircleShape)
                                            .testTag("in_app_update_progress"),
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        text = if (knownTotal) {
                                            "${formatMegabytes(state.downloadedBytes)} / ${formatMegabytes(state.totalBytes)}"
                                        } else {
                                            formatMegabytes(state.downloadedBytes)
                                        },
                                        color = cs.onSurfaceVariant,
                                        fontFamily = font,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Medium,
                                    )
                                    Text(
                                        text = percent?.let { "$it%" } ?: "…",
                                        color = profileAccent.primary,
                                        fontFamily = font,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }

                            is InAppUpdateUiState.ReadyToInstall -> {
                                Text(
                                    text = "Ready to Install",
                                    color = cs.onSurface,
                                    fontFamily = font,
                                    fontSize = 18.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Version ${state.release.version} downloaded. Tap below to finish updating.",
                                    color = cs.onSurfaceVariant,
                                    fontFamily = font,
                                    fontSize = 13.5.sp,
                                    lineHeight = 19.sp,
                                    textAlign = TextAlign.Center,
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                GradientActionButton(
                                    label = "Install Now",
                                    icon = CheckIcon,
                                    colors = listOf(successAccent.primary, routineAccent.primary),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp)
                                        .testTag("in_app_update_install_button"),
                                    onClick = { onInstallNow(state.release) },
                                )
                            }

                            is InAppUpdateUiState.Error -> {
                                Text(
                                    text = "Update Failed",
                                    color = cs.onSurface,
                                    fontFamily = font,
                                    fontSize = 18.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = state.message,
                                    color = cs.onSurfaceVariant,
                                    fontFamily = font,
                                    fontSize = 13.sp,
                                    lineHeight = 18.5.sp,
                                    textAlign = TextAlign.Center,
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                GradientActionButton(
                                    label = "Try Again",
                                    icon = RefreshIcon,
                                    colors = listOf(profileAccent.primary, routineAccent.primary),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp)
                                        .testTag("in_app_update_retry_button"),
                                    onClick = { onStartDownload(state.release) },
                                )
                            }

                            InAppUpdateUiState.Hidden -> Unit
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GradientActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    colors: List<Color>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        shadowElevation = 0.dp,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.horizontalGradient(colors))
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(17.dp),
            )
            Spacer(modifier = Modifier.width(7.dp))
            Text(
                text = label,
                color = Color.White,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
        }
    }
}
