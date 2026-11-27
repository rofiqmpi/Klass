package com.mpi.klass

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mpi.klass.theme.BackArrowIcon
import com.mpi.klass.theme.ChevronRightIcon
import com.mpi.klass.theme.CloseIcon
import com.mpi.klass.theme.KlassFeaturePalette
import com.mpi.klass.theme.ShieldIcon

internal enum class LoginInputField {
    EMAIL,
    PASSWORD,
}

internal enum class CustomKeyboardTab(
    val badge: String,
    val bengaliLabel: String,
) {
    LETTERS("ABC", "বর্ণ"),
    NUMBERS("123", "সংখ্যা"),
    SPECIAL_SYMBOLS("✦#", "প্রতীক"),
    EMOJIS("⚡☺", "ইমোজি"),
}

internal object KlassKeyboardLayouts {
    val lettersRow1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")
    val lettersRow2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l")
    val lettersRow3 = listOf("z", "x", "c", "v", "b", "n", "m")

    val numbersRow1 = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
    val numbersRow2 = listOf("@", "#", "$", "_", "&", "-", "+", "(", ")", "/")
    val numbersRow3 = listOf("*", "\"", "'", ":", ";", "!", "?", "~", "^", "=")

    // Uncommon / high-security symbols not on standard mobile keyboards
    val specialRow1 = listOf("§", "¶", "†", "‡", "※", "‽", "‰", "‱", "№", "™")
    val specialRow2 = listOf("◈", "◇", "❖", "✦", "✧", "✪", "❂", "✵", "✹", "✴")
    val specialRow3 = listOf("⌘", "⌥", "⌃", "∞", "≈", "≠", "≤", "≥", "±", "÷")
    val specialRow4 = listOf("Ω", "Δ", "Σ", "π", "λ", "α", "β", "৳", "₿", "€")

    // Special tech, security & expressive emojis/glyphs
    val emojiRow1 = listOf("🔐", "🛡️", "🔑", "🗝️", "⚡", "🔥", "💎", "🚀", "🌟", "👑")
    val emojiRow2 = listOf("🧠", "🎯", "✨", "💠", "🌀", "❄️", "🌙", "☀️", "🌊", "🦉")
    val emojiRow3 = listOf("🎓", "📚", "💻", "🧬", "🛰️", "⚙️", "🔔", "🏅", "❤️", "✅")

    val emailQuickSnippets = listOf("@", "@gmail.com", ".com", ".edu.bd", "_", "-", ".")
    val passwordQuickSymbols = listOf("◈", "⚡", "❖", "✦", "⌘", "§", "∞", "🔐", "🛡️")
}

internal fun popLastKeyboardToken(input: String): String {
    if (input.isEmpty()) return input
    var end = input.length
    while (end > 0) {
        val ch = input[end - 1]
        if (ch == '\uFE0F' || ch == '\uFE0E' || ch == '\u200D') {
            end--
            if (ch == '\u200D' && end > 0) {
                end = if (end >= 2 && input[end - 1].isLowSurrogate() && input[end - 2].isHighSurrogate()) {
                    end - 2
                } else {
                    end - 1
                }
                continue
            }
            continue
        }
        end = if (end >= 2 && ch.isLowSurrogate() && input[end - 2].isHighSurrogate()) {
            end - 2
        } else {
            end - 1
        }
        break
    }
    return input.substring(0, end.coerceAtLeast(0))
}

@Composable
internal fun KlassCustomKeyboard(
    activeField: LoginInputField,
    isDark: Boolean,
    hindSiliguri: FontFamily,
    onInsertText: (String) -> Unit,
    onBackspace: () -> Unit,
    onClearField: () -> Unit,
    onNextOrSubmit: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var currentTab by remember(activeField) {
        mutableStateOf(CustomKeyboardTab.LETTERS)
    }
    var isShifted by remember { mutableStateOf(false) }

    val accent = if (activeField == LoginInputField.EMAIL) {
        KlassFeaturePalette.routine(isDark).primary
    } else {
        KlassFeaturePalette.teaching(isDark).primary
    }
    val boardBg = if (isDark) Color(0xFF0E1624) else Color(0xFFE7EEF6)
    val keyBg = if (isDark) Color(0xFF1B273A) else Color(0xFFFFFFFF)
    val actionKeyBg = if (isDark) Color(0xFF26354D) else Color(0xFFD5E1EE)
    val keyTextColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val borderSubtle = if (isDark) Color.White.copy(alpha = 0.09f) else Color(0xFFCBD5E1)

    Surface(
        shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
        color = boardBg,
        border = BorderStroke(1.dp, accent.copy(alpha = if (isDark) 0.34f else 0.24f)),
        shadowElevation = 14.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("klass_custom_keyboard"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            // Mode Switcher & Header Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    CustomKeyboardTab.entries.forEach { tab ->
                        val selected = tab == currentTab
                        val chipBg by animateColorAsState(
                            targetValue = if (selected) accent else actionKeyBg.copy(alpha = 0.72f),
                            animationSpec = tween(160),
                            label = "kb_tab_bg_${tab.name}",
                        )
                        Surface(
                            onClick = { currentTab = tab },
                            shape = RoundedCornerShape(10.dp),
                            color = chipBg,
                            modifier = Modifier
                                .height(30.dp)
                                .testTag("kb_tab_${tab.name.lowercase()}"),
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 9.dp),
                            ) {
                                Text(
                                    text = if (tab == CustomKeyboardTab.LETTERS || tab == CustomKeyboardTab.NUMBERS) {
                                        tab.badge
                                    } else {
                                        "${tab.badge.take(1)} ${tab.bengaliLabel.localized()}"
                                    },
                                    fontFamily = hindSiliguri,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                                    color = if (selected) Color.White else keyTextColor,
                                    maxLines = 1,
                                    softWrap = false,
                                )
                            }
                        }
                    }
                }

                // Clear + Collapse buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Surface(
                        onClick = onClearField,
                        shape = RoundedCornerShape(9.dp),
                        color = actionKeyBg.copy(alpha = 0.8f),
                        modifier = Modifier
                            .height(30.dp)
                            .testTag("kb_clear_button"),
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 8.dp),
                        ) {
                            Text(
                                text = "মুছুন".localized(),
                                fontFamily = hindSiliguri,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = keyTextColor.copy(alpha = 0.85f),
                            )
                        }
                    }

                    Surface(
                        onClick = onDismiss,
                        shape = CircleShape,
                        color = actionKeyBg,
                        modifier = Modifier
                            .size(30.dp)
                            .testTag("kb_hide_button"),
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                imageVector = CloseIcon,
                                contentDescription = "বন্ধ করুন".localized(),
                                tint = keyTextColor,
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    }
                }
            }

            // Contextual Quick-Insert Ribbon (Email domains or Unique Rare Symbols)
            val quickTokens = if (activeField == LoginInputField.EMAIL) {
                KlassKeyboardLayouts.emailQuickSnippets
            } else {
                KlassKeyboardLayouts.passwordQuickSymbols
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = accent.copy(alpha = if (isDark) 0.18f else 0.12f),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = ShieldIcon,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(11.dp),
                        )
                        Text(
                            text = if (activeField == LoginInputField.EMAIL) {
                                "ইমেইল".localized()
                            } else {
                                "পাসওয়ার্ড".localized()
                            },
                            fontFamily = hindSiliguri,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = accent,
                        )
                    }
                }

                quickTokens.forEach { token ->
                    Surface(
                        onClick = { onInsertText(token) },
                        shape = RoundedCornerShape(9.dp),
                        color = keyBg,
                        border = BorderStroke(0.75.dp, borderSubtle),
                        modifier = Modifier.height(28.dp),
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 10.dp),
                        ) {
                            Text(
                                text = token,
                                fontFamily = hindSiliguri,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = keyTextColor,
                            )
                        }
                    }
                }
            }

            // Main Key Rows according to active tab
            when (currentTab) {
                CustomKeyboardTab.LETTERS -> {
                    // Row 1: q..p
                    KeyboardCharRow(
                        keys = KlassKeyboardLayouts.lettersRow1.map { if (isShifted) it.uppercase() else it },
                        keyBg = keyBg,
                        keyTextColor = keyTextColor,
                        borderSubtle = borderSubtle,
                        onKeyPress = { ch ->
                            onInsertText(ch)
                            if (isShifted) isShifted = false
                        },
                    )
                    // Row 2: a..l (centered with slight horizontal inset)
                    KeyboardCharRow(
                        keys = KlassKeyboardLayouts.lettersRow2.map { if (isShifted) it.uppercase() else it },
                        keyBg = keyBg,
                        keyTextColor = keyTextColor,
                        borderSubtle = borderSubtle,
                        horizontalPadding = 16.dp,
                        onKeyPress = { ch ->
                            onInsertText(ch)
                            if (isShifted) isShifted = false
                        },
                    )
                    // Row 3: Shift + z..m + Backspace
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        KeyboardActionKey(
                            label = if (isShifted) "⬆" else "⇧",
                            bg = if (isShifted) accent else actionKeyBg,
                            contentColor = if (isShifted) Color.White else keyTextColor,
                            borderSubtle = borderSubtle,
                            modifier = Modifier
                                .weight(1.35f)
                                .testTag("kb_shift_button"),
                            onClick = { isShifted = !isShifted },
                        )

                        KlassKeyboardLayouts.lettersRow3.forEach { raw ->
                            val ch = if (isShifted) raw.uppercase() else raw
                            KeyboardCharKey(
                                label = ch,
                                keyBg = keyBg,
                                keyTextColor = keyTextColor,
                                borderSubtle = borderSubtle,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    onInsertText(ch)
                                    if (isShifted) isShifted = false
                                },
                            )
                        }

                        KeyboardIconKey(
                            icon = BackArrowIcon,
                            contentDescription = "মুছুন".localized(),
                            bg = actionKeyBg,
                            tint = keyTextColor,
                            borderSubtle = borderSubtle,
                            modifier = Modifier
                                .weight(1.35f)
                                .testTag("kb_backspace_button"),
                            onClick = onBackspace,
                        )
                    }
                }

                CustomKeyboardTab.NUMBERS -> {
                    KeyboardCharRow(
                        keys = KlassKeyboardLayouts.numbersRow1,
                        keyBg = keyBg,
                        keyTextColor = keyTextColor,
                        borderSubtle = borderSubtle,
                        onKeyPress = onInsertText,
                    )
                    KeyboardCharRow(
                        keys = KlassKeyboardLayouts.numbersRow2,
                        keyBg = keyBg,
                        keyTextColor = keyTextColor,
                        borderSubtle = borderSubtle,
                        onKeyPress = onInsertText,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        KlassKeyboardLayouts.numbersRow3.take(8).forEach { sym ->
                            KeyboardCharKey(
                                label = sym,
                                keyBg = keyBg,
                                keyTextColor = keyTextColor,
                                borderSubtle = borderSubtle,
                                modifier = Modifier.weight(1f),
                                onClick = { onInsertText(sym) },
                            )
                        }
                        KeyboardIconKey(
                            icon = BackArrowIcon,
                            contentDescription = "মুছুন".localized(),
                            bg = actionKeyBg,
                            tint = keyTextColor,
                            borderSubtle = borderSubtle,
                            modifier = Modifier
                                .weight(1.4f)
                                .testTag("kb_backspace_button"),
                            onClick = onBackspace,
                        )
                    }
                }

                CustomKeyboardTab.SPECIAL_SYMBOLS -> {
                    KeyboardCharRow(
                        keys = KlassKeyboardLayouts.specialRow1,
                        keyBg = keyBg,
                        keyTextColor = accent,
                        borderSubtle = accent.copy(alpha = 0.25f),
                        onKeyPress = onInsertText,
                    )
                    KeyboardCharRow(
                        keys = KlassKeyboardLayouts.specialRow2,
                        keyBg = keyBg,
                        keyTextColor = accent,
                        borderSubtle = accent.copy(alpha = 0.25f),
                        onKeyPress = onInsertText,
                    )
                    KeyboardCharRow(
                        keys = KlassKeyboardLayouts.specialRow3,
                        keyBg = keyBg,
                        keyTextColor = keyTextColor,
                        borderSubtle = borderSubtle,
                        onKeyPress = onInsertText,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        KlassKeyboardLayouts.specialRow4.take(8).forEach { sym ->
                            KeyboardCharKey(
                                label = sym,
                                keyBg = keyBg,
                                keyTextColor = keyTextColor,
                                borderSubtle = borderSubtle,
                                modifier = Modifier.weight(1f),
                                onClick = { onInsertText(sym) },
                            )
                        }
                        KeyboardIconKey(
                            icon = BackArrowIcon,
                            contentDescription = "মুছুন".localized(),
                            bg = actionKeyBg,
                            tint = keyTextColor,
                            borderSubtle = borderSubtle,
                            modifier = Modifier
                                .weight(1.5f)
                                .testTag("kb_backspace_button"),
                            onClick = onBackspace,
                        )
                    }
                }

                CustomKeyboardTab.EMOJIS -> {
                    KeyboardCharRow(
                        keys = KlassKeyboardLayouts.emojiRow1,
                        keyBg = keyBg,
                        keyTextColor = keyTextColor,
                        borderSubtle = borderSubtle,
                        fontSizeSp = 17f,
                        onKeyPress = onInsertText,
                    )
                    KeyboardCharRow(
                        keys = KlassKeyboardLayouts.emojiRow2,
                        keyBg = keyBg,
                        keyTextColor = keyTextColor,
                        borderSubtle = borderSubtle,
                        fontSizeSp = 17f,
                        onKeyPress = onInsertText,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        KlassKeyboardLayouts.emojiRow3.take(8).forEach { emoji ->
                            KeyboardCharKey(
                                label = emoji,
                                keyBg = keyBg,
                                keyTextColor = keyTextColor,
                                borderSubtle = borderSubtle,
                                fontSizeSp = 17f,
                                modifier = Modifier.weight(1f),
                                onClick = { onInsertText(emoji) },
                            )
                        }
                        KeyboardIconKey(
                            icon = BackArrowIcon,
                            contentDescription = "মুছুন".localized(),
                            bg = actionKeyBg,
                            tint = keyTextColor,
                            borderSubtle = borderSubtle,
                            modifier = Modifier
                                .weight(1.5f)
                                .testTag("kb_backspace_button"),
                            onClick = onBackspace,
                        )
                    }
                }
            }

            // Bottom Control Row: 123/ABC toggle, @/., Spacebar, Backspace/Submit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                KeyboardActionKey(
                    label = if (currentTab == CustomKeyboardTab.LETTERS) "123" else "ABC",
                    bg = actionKeyBg,
                    contentColor = keyTextColor,
                    borderSubtle = borderSubtle,
                    modifier = Modifier.weight(1.25f),
                    onClick = {
                        currentTab = if (currentTab == CustomKeyboardTab.LETTERS) {
                            CustomKeyboardTab.NUMBERS
                        } else {
                            CustomKeyboardTab.LETTERS
                        }
                    },
                )

                KeyboardActionKey(
                    label = if (activeField == LoginInputField.EMAIL) "@" else "✦",
                    bg = actionKeyBg,
                    contentColor = accent,
                    borderSubtle = borderSubtle,
                    modifier = Modifier.weight(0.95f),
                    onClick = {
                        if (activeField == LoginInputField.EMAIL) {
                            onInsertText("@")
                        } else {
                            currentTab = if (currentTab == CustomKeyboardTab.SPECIAL_SYMBOLS) {
                                CustomKeyboardTab.EMOJIS
                            } else {
                                CustomKeyboardTab.SPECIAL_SYMBOLS
                            }
                        }
                    },
                )

                KeyboardActionKey(
                    label = "স্পেস".localized(),
                    bg = keyBg,
                    contentColor = keyTextColor.copy(alpha = 0.72f),
                    borderSubtle = borderSubtle,
                    modifier = Modifier.weight(2.4f),
                    onClick = { onInsertText(" ") },
                )

                KeyboardActionKey(
                    label = ".".localized(),
                    bg = actionKeyBg,
                    contentColor = keyTextColor,
                    borderSubtle = borderSubtle,
                    modifier = Modifier.weight(0.9f),
                    onClick = { onInsertText(".") },
                )

                Surface(
                    onClick = onNextOrSubmit,
                    shape = RoundedCornerShape(10.dp),
                    color = accent,
                    modifier = Modifier
                        .weight(1.75f)
                        .height(39.dp)
                        .testTag("kb_submit_or_next_button"),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = if (activeField == LoginInputField.EMAIL) {
                                "পরবর্তী".localized()
                            } else {
                                "লগইন".localized()
                            },
                            fontFamily = hindSiliguri,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            softWrap = false,
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Icon(
                            imageVector = ChevronRightIcon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KeyboardCharRow(
    keys: List<String>,
    keyBg: Color,
    keyTextColor: Color,
    borderSubtle: Color,
    horizontalPadding: androidx.compose.ui.unit.Dp = 0.dp,
    fontSizeSp: Float = 15.5f,
    onKeyPress: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        keys.forEach { key ->
            KeyboardCharKey(
                label = key,
                keyBg = keyBg,
                keyTextColor = keyTextColor,
                borderSubtle = borderSubtle,
                fontSizeSp = fontSizeSp,
                modifier = Modifier.weight(1f),
                onClick = { onKeyPress(key) },
            )
        }
    }
}

@Composable
private fun KeyboardCharKey(
    label: String,
    keyBg: Color,
    keyTextColor: Color,
    borderSubtle: Color,
    modifier: Modifier = Modifier,
    fontSizeSp: Float = 15.5f,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(9.dp),
        color = keyBg,
        border = BorderStroke(0.6.dp, borderSubtle),
        shadowElevation = 1.dp,
        modifier = modifier
            .height(39.dp)
            .clip(RoundedCornerShape(9.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick,
            ),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text = label,
                fontSize = fontSizeSp.sp,
                fontWeight = FontWeight.Medium,
                color = keyTextColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = false,
            )
        }
    }
}

@Composable
private fun KeyboardActionKey(
    label: String,
    bg: Color,
    contentColor: Color,
    borderSubtle: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(9.dp),
        color = bg,
        border = BorderStroke(0.6.dp, borderSubtle),
        modifier = modifier
            .height(39.dp)
            .clip(RoundedCornerShape(9.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick,
            ),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = false,
            )
        }
    }
}

@Composable
private fun KeyboardIconKey(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    bg: Color,
    tint: Color,
    borderSubtle: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(9.dp),
        color = bg,
        border = BorderStroke(0.6.dp, borderSubtle),
        modifier = modifier
            .height(39.dp)
            .clip(RoundedCornerShape(9.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick,
            ),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}
