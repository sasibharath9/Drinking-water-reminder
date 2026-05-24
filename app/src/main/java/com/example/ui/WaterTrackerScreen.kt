package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.HydrationRecord
import com.example.data.model.UserSettings
import kotlinx.coroutines.launch
import kotlin.math.sin

enum class TrackerTab(val title: String, val icon: ImageVector, val tag: String) {
    DASHBOARD("Home", Icons.Default.WaterDrop, "tab_home"),
    HISTORY("Trends", Icons.Default.BarChart, "tab_trends"),
    SETTINGS("Settings", Icons.Default.Settings, "tab_settings")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterTrackerScreen(
    viewModel: WaterViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val systemSettings by viewModel.userSettings.collectAsStateWithLifecycle()
    val todayRecords by viewModel.todayRecords.collectAsStateWithLifecycle()
    val allRecords by viewModel.allRecords.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(TrackerTab.DASHBOARD) }

    val currentIntake = viewModel.getTodayTotalMl(todayRecords)
    val progress = if (systemSettings.dailyGoalMl > 0) {
        (currentIntake.toFloat() / systemSettings.dailyGoalMl.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .drawBehind {
                // Immersive top ambient radial glow (HTML design: bg-[radial-gradient(circle_at_50%_-20%,_#1E3A5F_0%,_transparent_60%)] opacity-40)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF1E3A5F).copy(alpha = 0.35f),
                            Color.Transparent
                        ),
                        center = androidx.compose.ui.geometry.Offset(size.width / 2f, 0f),
                        radius = size.width * 1.1f
                    )
                )
            }
    ) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "GOOD MORNING",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.5.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Stay Hydrated 💧",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    },
                    actions = {
                        // Sync Status Indicator
                        Row(
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val pulseAlpha by rememberInfiniteTransition(label = "pulse").animateFloat(
                                initialValue = 0.4f,
                                targetValue = 1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1000, easing = EaseInOutSine),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "pulseAlpha"
                            )
                            
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (syncStatus) {
                                            "Synced" -> Color(0xFF4CAF50).copy(alpha = pulseAlpha)
                                            "Syncing" -> Color(0xFFFF9800).copy(alpha = pulseAlpha)
                                            else -> MaterialTheme.colorScheme.primary.copy(alpha = pulseAlpha)
                                        }
                                    )
                            )
                            Text(
                                text = syncStatus,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                    tonalElevation = 0.dp,
                    windowInsets = WindowInsets.navigationBars,
                    modifier = Modifier
                        .border(
                            width = 0.8.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                        )
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                ) {
                    TrackerTab.values().forEach { tab ->
                        NavigationBarItem(
                            selected = activeTab == tab,
                            onClick = { activeTab = tab },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title
                                )
                            },
                            label = {
                                Text(text = tab.title, fontWeight = FontWeight.SemiBold)
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier.testTag(tab.tag)
                        )
                    }
                }
            }
        ) { innerPadding ->
            AnimatedContent(
                targetState = activeTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                },
                label = "tab_transition",
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) { currentTab ->
                when (currentTab) {
                    TrackerTab.DASHBOARD -> {
                        DashboardTab(
                            progress = progress,
                            currentIntake = currentIntake,
                            goal = systemSettings.dailyGoalMl,
                            todayRecords = todayRecords,
                            settings = systemSettings,
                            onAddWater = { viewModel.addWaterIntake(it) },
                            onDeleteWater = { viewModel.deleteWaterIntake(it) }
                        )
                    }
                    TrackerTab.HISTORY -> {
                        HistoryTab(
                            allRecords = allRecords,
                            trendData = viewModel.getWeeklyTrendData(allRecords),
                            goal = systemSettings.dailyGoalMl,
                            onDeleteWater = { viewModel.deleteWaterIntake(it) },
                            onClearAll = { viewModel.clearAllHistory() }
                        )
                    }
                    TrackerTab.SETTINGS -> {
                        SettingsTab(
                            settings = systemSettings,
                            onSaveSettings = { goal, interval, notify, theme, cloud ->
                                viewModel.updateSettings(goal, interval, notify, theme, cloud)
                            },
                            onTriggerDemoNotice = { viewModel.triggerQuickDemoReminder() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardTab(
    progress: Float,
    currentIntake: Int,
    goal: Int,
    todayRecords: List<HydrationRecord>,
    settings: UserSettings,
    onAddWater: (Int) -> Unit,
    onDeleteWater: (HydrationRecord) -> Unit
) {
    var showCustomDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Animated Immersive Hydro Circle Wave Tracker Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.45f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 0.8.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(32.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.BottomCenter,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            WaterWaveBubble(
                                progress = progress,
                                currentIntake = currentIntake,
                                goal = goal
                            )
                            
                            // Active reminder status capsule
                            Box(
                                modifier = Modifier
                                    .offset(y = 10.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(MaterialTheme.colorScheme.tertiary)
                                    .border(1.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(50))
                                    .padding(horizontal = 14.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "ACTIVE REMINDER 💧",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Interval & Last Drink Dual Widgets Grid
        item {
            val lastDrinkTime = todayRecords.firstOrNull()?.formattedTime ?: "None logged"
            val intervalText = if (settings.isNotificationsEnabled) "Every ${settings.reminderIntervalMinutes}m" else "Disabled"

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card 1: Interval
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.45f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            width = 0.8.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(24.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "INTERVAL",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = intervalText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                // Card 2: Last Drink
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.45f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            width = 0.8.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(24.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "LAST DRINK",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = lastDrinkTime,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }

        // Quick Preset Logging Blocks Title
        item {
            Text(
                text = "Log Water Intake",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp, top = 6.dp)
            )
            
            Spacer(modifier = Modifier.height(6.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PresetCupButton(
                        amount = 150,
                        icon = Icons.Default.LocalDrink,
                        description = "Sip • 150ml",
                        onClick = { onAddWater(150) },
                        modifier = Modifier.weight(1f).testTag("log_150")
                    )
                    PresetCupButton(
                        amount = 250,
                        icon = Icons.Default.LocalDrink,
                        description = "Regular • 250ml",
                        onClick = { onAddWater(250) },
                        modifier = Modifier.weight(1f).testTag("log_250")
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PresetCupButton(
                        amount = 350,
                        icon = Icons.Default.LocalDrink,
                        description = "Glass • 350ml",
                        onClick = { onAddWater(350) },
                        modifier = Modifier.weight(1f).testTag("log_350")
                    )
                    PresetCupButton(
                        amount = 500,
                        icon = Icons.Default.LocalDrink,
                        description = "Flask • 500ml",
                        onClick = { onAddWater(500) },
                        modifier = Modifier.weight(1f).testTag("log_500")
                    )
                }

                // Custom log option
                OutlinedButton(
                    onClick = { showCustomDialog = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("custom_amount_button"),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Custom")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Add Custom Amount...", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Today's Logs Title
        item {
            Text(
                text = "Today's Intake Log",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )
        }

        if (todayRecords.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 0.8.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(20.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = "None Today Logo",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Column {
                            Text(
                                text = "No water logged yet today",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Choose a preset above to record your hydration.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        } else {
            items(todayRecords, key = { it.id }) { record ->
                TodayIntakeCard(record = record, onDelete = { onDeleteWater(record) })
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showCustomDialog) {
        CustomAmountDialog(
            onDismiss = { showCustomDialog = false },
            onConfirm = { amount ->
                if (amount > 0) onAddWater(amount)
                showCustomDialog = false
            }
        )
    }
}

@Composable
fun WaterWaveBubble(
    progress: Float,
    currentIntake: Int,
    goal: Int
) {
    val wavePhase = rememberInfiniteTransition(label = "").animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val animateProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "fluidProgress"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val accentColor = MaterialTheme.colorScheme.tertiary

    Box(
        modifier = Modifier
            .size(200.dp)
            .clip(CircleShape)
            .background(primaryColor.copy(alpha = 0.05f))
            .border(2.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f), CircleShape)
            .padding(6.dp)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Calculate the water level from the bottom
            val waterLevelHeight = height * (1f - animateProgress)

            val circlePath = Path().apply {
                addOval(androidx.compose.ui.geometry.Rect(0f, 0f, width, height))
            }

            clipPath(circlePath) {
                // Conic/radial background effect block
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF0A1218),
                            Color(0xFF1E3A5F)
                        )
                    )
                )

                // Draw water surface waves
                val waterPath = Path()
                if (animateProgress > 0f) {
                    val baseHeight = waterLevelHeight
                    val amplitude = if (animateProgress in 0.01f..0.99f) 10.dp.toPx() else 0f
                    val frequency = 2f * Math.PI.toFloat() / width

                    waterPath.moveTo(0f, height)
                    waterPath.lineTo(0f, baseHeight)

                    for (x in 0..width.toInt()) {
                        val y = sin(x.toFloat() * frequency + wavePhase.value) * amplitude + baseHeight
                        waterPath.lineTo(x.toFloat(), y)
                    }

                    waterPath.lineTo(width, height)
                    waterPath.close()

                    // Draw the wave
                    drawPath(
                        path = waterPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.85f),
                                primaryColor
                            )
                        )
                    )
                }
            }
        }

        // Concentric layout metric numbers in center
        val percent = (progress * 100).toInt()
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = String.format("%,d", currentIntake),
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.White,
                    letterSpacing = (-1).sp
                )
                Text(
                    text = "ml",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
                )
            }
            Text(
                text = "$percent% of daily goal",
                fontSize = 11.sp,
                color = if (animateProgress > 0.45f) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun PresetCupButton(
    amount: Int,
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.45f)
        ),
        onClick = onClick,
        modifier = modifier
            .height(68.dp)
            .border(
                width = 0.8.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column {
                Text(
                    text = "+$amount mL",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                )
            }
        }
    }
}

@Composable
fun TodayIntakeCard(
    record: HydrationRecord,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.45f)),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 0.8.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalDrink,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "${record.amountMl} mL",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 15.sp
                    )
                    Text(
                        text = record.formattedTime,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
            IconButton(
                onClick = onDelete,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                ),
                modifier = Modifier.testTag("delete_${record.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove entry",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun CustomAmountDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var amountStr by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Custom Intake",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Enter the exact amount of water you drank in mL:")
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it.filter { char -> char.isDigit() } },
                    label = { Text("Amount (mL)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("custom_amount_input"),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountStr.toIntOrNull() ?: 0
                    if (amount > 0) onConfirm(amount)
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("dialog_confirm")
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dialog_dismiss")
            ) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun HistoryTab(
    allRecords: List<HydrationRecord>,
    trendData: List<Pair<String, Int>>,
    goal: Int,
    onDeleteWater: (HydrationRecord) -> Unit,
    onClearAll: () -> Unit
) {
    var showClearConfirm by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Weekly Trends Graph Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.45f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 0.8.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(24.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Weekly Trends",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Last 7 days dynamic insights",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Pure Compose Customized Bar Chart
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        val maxInTrend = (trendData.maxOfOrNull { it.second } ?: 1000).coerceAtLeast(goal)
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            trendData.forEach { (day, amount) ->
                                val barHeightFactor = if (maxInTrend > 0) amount.toFloat() / maxInTrend.toFloat() else 0f
                                
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Amount popup
                                    AnimatedVisibility(
                                        visible = amount > 0,
                                        enter = fadeIn() + expandVertically(),
                                        exit = fadeOut()
                                    ) {
                                        Text(
                                            text = if (amount >= 1000) "${String.format("%.1f", amount / 1000f)}k" else "$amount",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            textAlign = TextAlign.Center
                                        )
                                    }

                                    // Bar segment
                                    Box(
                                        modifier = Modifier
                                            .width(20.dp)
                                            .fillMaxHeight(barHeightFactor.coerceIn(0.04f, 1f))
                                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = if (amount >= goal) {
                                                        listOf(MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.primary)
                                                    } else {
                                                        listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), MaterialTheme.colorScheme.primary)
                                                    }
                                                )
                                            )
                                    )

                                    // Day label
                                    Text(
                                        text = day,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Stats summary block
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Total Sessions",
                    value = "${allRecords.size}",
                    icon = Icons.Default.EventRepeat,
                    modifier = Modifier.weight(1f)
                )
                val totalVolume = allRecords.sumOf { it.amountMl }
                StatCard(
                    title = "Volume Tracked",
                    value = if (totalVolume >= 1000) "${String.format("%.1f", totalVolume/1000f)} L" else "$totalVolume mL",
                    icon = Icons.Default.WaterDrop,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Historic Logs Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "All Records History",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp)
                )
                if (allRecords.isNotEmpty()) {
                    TextButton(
                        onClick = { showClearConfirm = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.testTag("clear_all_button")
                    ) {
                        Text("Clear All")
                    }
                }
            }
        }

        if (allRecords.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No history available",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Records tracked across linked devices will stream here once logged.",
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        } else {
            items(allRecords, key = { "his_${it.id}_${it.timestamp}" }) { record ->
                HistoryRecordCard(record = record, onDelete = { onDeleteWater(record) })
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear All Records?", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) },
            text = { Text("This will permanently remove entire tracked drinking intervals database. This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAll()
                        showClearConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.45f)),
        modifier = modifier.border(
            width = 0.8.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            shape = RoundedCornerShape(20.dp)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(
                    text = value,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun HistoryRecordCard(
    record: HydrationRecord,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.45f)),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 0.8.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalDrink,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${record.amountMl} mL",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 14.sp
                        )
                        Text(
                            text = record.formattedTime,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                    Text(
                        text = record.formattedDate,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }
            IconButton(
                onClick = onDelete,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete record",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun SettingsTab(
    settings: UserSettings,
    onSaveSettings: (Int, Int, Boolean, String, Boolean) -> Unit,
    onTriggerDemoNotice: () -> Unit
) {
    var goalInput by remember(settings.dailyGoalMl) { mutableStateOf(settings.dailyGoalMl.toString()) }
    var expandedInterval by remember { mutableStateOf(false) }
    var currentInterval by remember(settings.reminderIntervalMinutes) { mutableStateOf(settings.reminderIntervalMinutes) }
    var notificationEnabled by remember(settings.isNotificationsEnabled) { mutableStateOf(settings.isNotificationsEnabled) }
    var activeThemeMode by remember(settings.themeMode) { mutableStateOf(settings.themeMode) }
    var cloudSyncEnabled by remember(settings.isCloudSyncEnabled) { mutableStateOf(settings.isCloudSyncEnabled) }

    val intervalChoices = listOf(15, 30, 45, 60, 90, 120, 180)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Daily Goals Config Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.45f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 0.8.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(20.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrackChanges,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Daily Hydration Goals",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    OutlinedTextField(
                        value = goalInput,
                        onValueChange = {
                            goalInput = it.filter { char -> char.isDigit() }
                            val newGoal = goalInput.toIntOrNull() ?: 2000
                            onSaveSettings(newGoal, currentInterval, notificationEnabled, activeThemeMode, cloudSyncEnabled)
                        },
                        label = { Text("Daily Target (mL)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("settings_goal_input"),
                        singleLine = true
                    )
                    Text(
                        text = "Standard health guidelines recommend 2000 - 3000 mL daily depending on exertion.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }

        // Custom Notification Intervals Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.45f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 0.8.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(20.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Reminder Intervals",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Switch(
                            checked = notificationEnabled,
                            onCheckedChange = {
                                notificationEnabled = it
                                val goal = goalInput.toIntOrNull() ?: 2000
                                onSaveSettings(goal, currentInterval, notificationEnabled, activeThemeMode, cloudSyncEnabled)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.surface,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("notification_toggle")
                        )
                    }

                    AnimatedVisibility(visible = notificationEnabled) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "Reminder Frequency:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )

                            // Clean Select list
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    onClick = { expandedInterval = true },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("interval_dropdown_trigger")
                                ) {
                                    Text(
                                        text = "Every ${if (currentInterval >= 60) "${currentInterval / 60}h" else ""} ${if (currentInterval % 60 > 0) "${currentInterval % 60}m" else ""}",
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                                }

                                DropdownMenu(
                                    expanded = expandedInterval,
                                    onDismissRequest = { expandedInterval = false },
                                    modifier = Modifier.fillMaxWidth(0.85f)
                                ) {
                                    intervalChoices.forEach { minutes ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = when {
                                                        minutes < 60 -> "$minutes Minutes"
                                                        minutes == 60 -> "1 Hour"
                                                        minutes == 120 -> "2 Hours"
                                                        minutes == 180 -> "3 Hours"
                                                        else -> "${minutes / 60}h ${minutes % 60}m"
                                                    },
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            },
                                            onClick = {
                                                currentInterval = minutes
                                                expandedInterval = false
                                                val goal = goalInput.toIntOrNull() ?: 2000
                                                onSaveSettings(goal, currentInterval, notificationEnabled, activeThemeMode, cloudSyncEnabled)
                                            }
                                        )
                                    }
                                }
                            }

                            // Trigger demo push
                            Button(
                                onClick = onTriggerDemoNotice,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                                    contentColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("demo_notification_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Notifications,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Test Hydration Notification [7s]", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Appearance Mode Configuration Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.45f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 0.8.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(20.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Theme Appearance",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Mode selections Segmented Buttons representation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("SYSTEM", "LIGHT", "DARK").forEach { mode ->
                            val isSelected = activeThemeMode == mode
                            val colorScheme = MaterialTheme.colorScheme
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) colorScheme.primary else colorScheme.primary.copy(alpha = 0.05f)
                                    )
                                    .clickable {
                                        activeThemeMode = mode
                                        val goal = goalInput.toIntOrNull() ?: 2000
                                        onSaveSettings(goal, currentInterval, notificationEnabled, activeThemeMode, cloudSyncEnabled)
                                    }
                                    .padding(vertical = 10.dp)
                                    .testTag("theme_mode_$mode"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = mode,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Multi-Device Cloud Sync Config Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.45f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 0.8.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(20.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CloudDone,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Cross-Device Sync",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Switch(
                            checked = cloudSyncEnabled,
                            onCheckedChange = {
                                cloudSyncEnabled = it
                                val goal = goalInput.toIntOrNull() ?: 2000
                                onSaveSettings(goal, currentInterval, notificationEnabled, activeThemeMode, cloudSyncEnabled)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.surface,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    Text(
                        text = "Automatically syncing logs with water database enables instant progress visualization across tablet, web companion and phone accessories.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
