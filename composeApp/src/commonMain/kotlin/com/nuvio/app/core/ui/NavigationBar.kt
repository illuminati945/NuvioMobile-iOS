package com.nuvio.app.core.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Stable
class NuvioNavBarScrollState {
    var labelVisibility by mutableFloatStateOf(1f)
        private set

    private var accumulatedDelta = 0f
    private var isLocked = false

    fun lockForTabSwitch() {
        isLocked = true
        labelVisibility = 1f
        accumulatedDelta = 0f
    }

    fun unlock() {
        isLocked = false
    }

    fun expand() {
        labelVisibility = 1f
        accumulatedDelta = 0f
    }

    fun collapse() {
        labelVisibility = 0f
        accumulatedDelta = 0f
    }

    val nestedScrollConnection: NestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            if (isLocked) return Offset.Zero
            val deltaY = available.y
            if (deltaY == 0f) return Offset.Zero

            accumulatedDelta += deltaY

            if (accumulatedDelta < -SCROLL_THRESHOLD && labelVisibility != 0f) {
                labelVisibility = 0f
                accumulatedDelta = 0f
            } else if (accumulatedDelta > SCROLL_THRESHOLD && labelVisibility != 1f) {
                labelVisibility = 1f
                accumulatedDelta = 0f
            }

            if (deltaY < 0f && accumulatedDelta > 0f) accumulatedDelta = deltaY
            if (deltaY > 0f && accumulatedDelta < 0f) accumulatedDelta = deltaY

            return Offset.Zero
        }
    }

    companion object {
        private const val SCROLL_THRESHOLD = 120f
    }
}

@Composable
fun rememberNuvioNavBarScrollState(): NuvioNavBarScrollState =
    androidx.compose.runtime.remember { NuvioNavBarScrollState() }

@Composable
fun NuvioNavigationBar(
    modifier: Modifier = Modifier,
    scrollState: NuvioNavBarScrollState? = null,
    hazeState: HazeState? = null,
    content: @Composable NuvioNavigationBarScope.() -> Unit,
) {
    val labelFraction by animateFloatAsState(
        targetValue = scrollState?.labelVisibility ?: 1f,
        animationSpec = spring(
            dampingRatio = 0.85f,
            stiffness = 300f,
        ),
        label = "nav_label_alpha",
    )

    val navigationBarInsets = nuvioBottomNavigationBarInsets()
    val bottomSafePadding = navigationBarInsets.asPaddingValues().calculateBottomPadding()
    val horizontalPadding = 18.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = bottomSafePadding + nuvioBottomNavigationExtraVerticalPadding + 8.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        val pillShape = RoundedCornerShape(32.dp)

        Box(
            modifier = Modifier
                .padding(horizontal = horizontalPadding)
                .fillMaxWidth()
                .shadow(
                    elevation = 16.dp,
                    shape = pillShape,
                    ambientColor = Color.Black.copy(alpha = 0.45f),
                    spotColor = Color.Black.copy(alpha = 0.55f),
                )
                .clip(pillShape)
                .then(
                    if (hazeState != null) {
                        Modifier.hazeEffect(state = hazeState) {
                            blurRadius = 32.dp
                            noiseFactor = 0.04f
                        }
                    } else {
                        Modifier
                    },
                )
                // Apple Liquid Glass translucent background + specular sheen
                .background(Color(0x3B16161B))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.12f),
                            Color.Transparent,
                            Color.White.copy(alpha = 0.03f),
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.38f),
                            Color.White.copy(alpha = 0.08f),
                            Color.White.copy(alpha = 0.16f),
                        )
                    ),
                    shape = pillShape,
                )
                .height(64.dp), // Stable fixed height so the pill NEVER jumps vertically
            contentAlignment = Alignment.Center,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                NuvioNavigationBarScopeImpl(
                    rowScope = this,
                    labelFraction = labelFraction,
                ).content()
            }
        }
    }
}

interface NuvioNavigationBarScope {
    @Composable
    fun NavItem(
        selected: Boolean,
        onClick: () -> Unit,
        icon: ImageVector,
        contentDescription: String?,
        modifier: Modifier = Modifier,
        label: String? = null,
    )

    @Composable
    fun NavItem(
        selected: Boolean,
        onClick: () -> Unit,
        icon: DrawableResource,
        contentDescription: String?,
        modifier: Modifier = Modifier,
        label: String? = null,
    )

    @Composable
    fun NavItem(
        selected: Boolean,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        label: String? = null,
        content: @Composable () -> Unit,
    )
}

private class NuvioNavigationBarScopeImpl(
    private val rowScope: androidx.compose.foundation.layout.RowScope,
    private val labelFraction: Float,
) : NuvioNavigationBarScope {

    @Composable
    override fun NavItem(
        selected: Boolean,
        onClick: () -> Unit,
        icon: ImageVector,
        contentDescription: String?,
        modifier: Modifier,
        label: String?,
    ) {
        val tokens = MaterialTheme.nuvio
        val palette = ThemeColors.getColorPalette(MaterialTheme.appTheme)
        val useGradient = selected && palette.accentGradient.size >= 2
        val iconColor by animateColorAsState(
            targetValue = if (selected) tokens.colors.accent else tokens.colors.textMuted.copy(alpha = 0.85f),
            label = "nav_icon_color",
        )
        val selectedBgColor by animateColorAsState(
            targetValue = if (selected) Color.White.copy(alpha = 0.12f) else Color.Transparent,
            label = "nav_bg_color",
        )

        with(rowScope) {
            Column(
                modifier = modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(selectedBgColor)
                    .selectable(
                        selected = selected,
                        enabled = true,
                        role = Role.Tab,
                        onClick = onClick,
                    )
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    modifier = Modifier
                        .size(24.dp)
                        .then(if (useGradient) Modifier.gradientMask(palette.accentBrush()) else Modifier),
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = if (useGradient) Color.White else iconColor,
                )
                if (label != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    NavItemLabel(label = label, labelFraction = labelFraction, iconColor = iconColor, selected = selected)
                }
            }
        }
    }

    @Composable
    override fun NavItem(
        selected: Boolean,
        onClick: () -> Unit,
        icon: DrawableResource,
        contentDescription: String?,
        modifier: Modifier,
        label: String?,
    ) {
        val tokens = MaterialTheme.nuvio
        val palette = ThemeColors.getColorPalette(MaterialTheme.appTheme)
        val useGradient = selected && palette.accentGradient.size >= 2
        val iconColor by animateColorAsState(
            targetValue = if (selected) tokens.colors.accent else tokens.colors.textMuted.copy(alpha = 0.85f),
            label = "nav_icon_color",
        )
        val selectedBgColor by animateColorAsState(
            targetValue = if (selected) Color.White.copy(alpha = 0.12f) else Color.Transparent,
            label = "nav_bg_color",
        )

        with(rowScope) {
            Column(
                modifier = modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(selectedBgColor)
                    .selectable(
                        selected = selected,
                        enabled = true,
                        role = Role.Tab,
                        onClick = onClick,
                    )
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    modifier = Modifier
                        .size(24.dp)
                        .then(if (useGradient) Modifier.gradientMask(palette.accentBrush()) else Modifier),
                    painter = painterResource(icon),
                    contentDescription = contentDescription,
                    tint = if (useGradient) Color.White else iconColor,
                )
                if (label != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    NavItemLabel(label = label, labelFraction = labelFraction, iconColor = iconColor, selected = selected)
                }
            }
        }
    }

    @Composable
    override fun NavItem(
        selected: Boolean,
        onClick: () -> Unit,
        modifier: Modifier,
        label: String?,
        content: @Composable () -> Unit,
    ) {
        val tokens = MaterialTheme.nuvio
        val selectedBgColor by animateColorAsState(
            targetValue = if (selected) Color.White.copy(alpha = 0.12f) else Color.Transparent,
            label = "nav_bg_color",
        )
        val iconColor by animateColorAsState(
            targetValue = if (selected) tokens.colors.accent else tokens.colors.textMuted.copy(alpha = 0.85f),
            label = "nav_icon_color",
        )

        with(rowScope) {
            Column(
                modifier = modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(selectedBgColor)
                    .selectable(
                        selected = selected,
                        enabled = true,
                        role = Role.Tab,
                        onClick = onClick,
                    )
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                content()
                if (label != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    NavItemLabel(label = label, labelFraction = labelFraction, iconColor = iconColor, selected = selected)
                }
            }
        }
    }
}

@Composable
private fun NavItemLabel(
    label: String?,
    labelFraction: Float,
    iconColor: Color,
    selected: Boolean,
) {
    if (label == null) return
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(
            fontSize = NuvioTokens.Type.labelXs,
            lineHeight = NuvioTokens.LineHeight.labelXs,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        ),
        color = iconColor.copy(alpha = labelFraction.coerceIn(0f, 1f)),
        maxLines = 1,
        overflow = TextOverflow.Clip,
    )
}

@Composable
fun NuvioClassicNavigationBar(
    modifier: Modifier = Modifier,
    content: @Composable NuvioNavigationBarScope.() -> Unit,
) {
    val tokens = MaterialTheme.nuvio
    Column(modifier.fillMaxWidth()) {
        androidx.compose.material3.HorizontalDivider(
            thickness = NuvioTokens.Space.hairline,
            color = tokens.colors.borderDefault,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(nuvioBottomNavigationBarInsets().asPaddingValues())
                .padding(horizontal = NuvioTokens.Space.s4, vertical = nuvioBottomNavigationExtraVerticalPadding),
            horizontalArrangement = Arrangement.spacedBy(tokens.spacing.controlGap, Alignment.CenterHorizontally),
        ) {
            NuvioClassicNavigationBarScopeImpl(this).content()
        }
    }
}

private class NuvioClassicNavigationBarScopeImpl(
    private val rowScope: androidx.compose.foundation.layout.RowScope,
) : NuvioNavigationBarScope {

    @Composable
    override fun NavItem(
        selected: Boolean,
        onClick: () -> Unit,
        icon: ImageVector,
        contentDescription: String?,
        modifier: Modifier,
        label: String?,
    ) {
        val tokens = MaterialTheme.nuvio
        val palette = ThemeColors.getColorPalette(MaterialTheme.appTheme)
        val useGradient = selected && palette.accentGradient.size >= 2
        val iconColor by animateColorAsState(
            targetValue = if (selected) tokens.colors.accent else tokens.colors.textMuted,
            label = "classic_nav_icon_color",
        )
        with(rowScope) {
            Icon(
                modifier = modifier
                    .widthIn(max = tokens.components.navItemMaxWidth)
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .clip(tokens.components.navItemShape)
                    .selectable(
                        selected = selected,
                        enabled = true,
                        role = Role.Tab,
                        onClick = onClick,
                    )
                    .padding(NuvioTokens.Space.s10)
                    .size(tokens.components.navIconSize)
                    .then(if (useGradient) Modifier.gradientMask(palette.accentBrush()) else Modifier),
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (useGradient) Color.White else iconColor,
            )
        }
    }

    @Composable
    override fun NavItem(
        selected: Boolean,
        onClick: () -> Unit,
        icon: DrawableResource,
        contentDescription: String?,
        modifier: Modifier,
        label: String?,
    ) {
        val tokens = MaterialTheme.nuvio
        val palette = ThemeColors.getColorPalette(MaterialTheme.appTheme)
        val useGradient = selected && palette.accentGradient.size >= 2
        val iconColor by animateColorAsState(
            targetValue = if (selected) tokens.colors.accent else tokens.colors.textMuted,
            label = "classic_nav_icon_color",
        )
        with(rowScope) {
            Icon(
                modifier = modifier
                    .widthIn(max = tokens.components.navItemMaxWidth)
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .clip(tokens.components.navItemShape)
                    .selectable(
                        selected = selected,
                        enabled = true,
                        role = Role.Tab,
                        onClick = onClick,
                    )
                    .padding(NuvioTokens.Space.s10)
                    .size(tokens.components.navIconSize)
                    .then(if (useGradient) Modifier.gradientMask(palette.accentBrush()) else Modifier),
                painter = painterResource(icon),
                contentDescription = contentDescription,
                tint = if (useGradient) Color.White else iconColor,
            )
        }
    }

    @Composable
    override fun NavItem(
        selected: Boolean,
        onClick: () -> Unit,
        modifier: Modifier,
        label: String?,
        content: @Composable () -> Unit,
    ) {
        val tokens = MaterialTheme.nuvio
        with(rowScope) {
            Box(
                modifier = modifier
                    .widthIn(max = tokens.components.navItemMaxWidth)
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .clip(tokens.components.navItemShape)
                    .selectable(
                        selected = selected,
                        enabled = true,
                        role = Role.Tab,
                        onClick = onClick,
                    )
                    .padding(NuvioTokens.Space.s10),
                contentAlignment = Alignment.Center,
            ) {
                content()
            }
        }
    }
}
