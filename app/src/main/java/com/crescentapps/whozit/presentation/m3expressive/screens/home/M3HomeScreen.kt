package com.crescentapps.whozit.presentation.m3expressive.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crescentapps.whozit.core.model.OccurrenceStatus
import com.crescentapps.whozit.core.model.ResolvedTurn
import com.crescentapps.whozit.core.model.ScheduleType
import com.crescentapps.whozit.presentation.theme.withDockPadding
import com.crescentapps.whozit.presentation.m3expressive.components.WhozitExpressiveButton
import com.crescentapps.whozit.presentation.m3expressive.components.WhozitExpressiveCard
import com.crescentapps.whozit.presentation.m3expressive.theme.ExpressiveTokens
import com.crescentapps.whozit.presentation.m3expressive.components.expressiveLightRay
import com.crescentapps.whozit.data.preferences.UserPreferences
import com.crescentapps.whozit.presentation.screens.home.HomeViewModel
import com.crescentapps.whozit.presentation.components.WhozitBackdropBackground
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.BorderStroke

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun M3HomeScreen(
    viewModel: HomeViewModel,
    prefs: UserPreferences,
    onCreateSchedule: () -> Unit,
    onScheduleClick: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var turnForAmountDialog by remember { mutableStateOf<ResolvedTurn?>(null) }
    var enteredAmount by remember { mutableStateOf("") }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = com.crescentapps.whozit.presentation.theme.whozitScaffoldWindowInsets(),
        floatingActionButton = {
            if (uiState.todayTurns.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = onCreateSchedule,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("New Schedule") },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.expressiveLightRay(shape = CircleShape)
                )
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.todayTurns.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(32.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.size(96.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "All Caught Up!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No turns are due right now. Tap '+' below to create a schedule.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onCreateSchedule,
                        modifier = Modifier.expressiveLightRay(shape = CircleShape),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Create Schedule", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            val pendingTurns = uiState.filteredTurns.filter { it.status == OccurrenceStatus.PENDING }
            val firstPending = pendingTurns.firstOrNull() ?: uiState.filteredTurns.firstOrNull()
            val remainingFiltered = uiState.filteredTurns.filter { it != firstPending }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(
                    start = ExpressiveTokens.spacing.normal,
                    end = ExpressiveTokens.spacing.normal,
                    top = ExpressiveTokens.spacing.small,
                    bottom = 20.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Next Turn Hero Card
                if (firstPending != null) {
                    item {
                        HeroTurnCard(
                            turn = firstPending,
                            onClick = { onScheduleClick(firstPending.schedule.id) },
                            onComplete = {
                                if (firstPending.schedule.type == ScheduleType.MONEY) {
                                    turnForAmountDialog = firstPending
                                    enteredAmount = firstPending.schedule.defaultAmount?.toString() ?: ""
                                } else {
                                    viewModel.markTurnComplete(firstPending)
                                }
                            },
                            onSwap = { viewModel.skipTurn(firstPending) },
                            onUndo = { viewModel.undoCompletion(firstPending) }
                        )
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TODAY'S TURNS",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "${uiState.totalPending} left · ${uiState.totalCompleted} done",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val filters = listOf(
                                com.crescentapps.whozit.presentation.screens.home.HomeFilter.ALL to "All Turns",
                                com.crescentapps.whozit.presentation.screens.home.HomeFilter.LOCAL to "My Schedules",
                                com.crescentapps.whozit.presentation.screens.home.HomeFilter.SHARED to "Shared"
                            )
                            filters.forEach { (filter, label) ->
                                FilterChip(
                                    selected = uiState.currentFilter == filter,
                                    onClick = { viewModel.setFilter(filter) },
                                    label = { Text(label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }
                    }
                }

                if (remainingFiltered.isNotEmpty()) {
                    items(remainingFiltered) { turn ->
                        UpcomingTurnCard(
                            turn = turn,
                            onClick = { onScheduleClick(turn.schedule.id) }
                        )
                    }
                }
            }
        }
    }

    if (turnForAmountDialog != null) {
        com.crescentapps.whozit.presentation.m3expressive.components.WhozitExpressiveDialog(
            title = "Record Expense",
            message = "",
            confirmText = "Save",
            dismissText = "Cancel",
            onConfirm = {
                val amount = enteredAmount.toDoubleOrNull()
                viewModel.markTurnComplete(turnForAmountDialog!!, amount)
                turnForAmountDialog = null
            },
            onDismissRequest = { turnForAmountDialog = null }
        ) {
            OutlinedTextField(
                value = enteredAmount,
                onValueChange = { enteredAmount = it },
                label = { Text("Amount (${turnForAmountDialog!!.schedule.currencyCode})") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HeroTurnCard(
    turn: ResolvedTurn,
    onClick: () -> Unit,
    onComplete: () -> Unit,
    onSwap: () -> Unit,
    onUndo: () -> Unit
) {
    val isCompleted = turn.status == com.crescentapps.whozit.core.model.OccurrenceStatus.COMPLETED
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(24.dp, RoundedCornerShape(32.dp), spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
            .expressiveLightRay(shape = RoundedCornerShape(32.dp)),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.8f)
        )
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Ethereal gradient blobs
            Box(
                modifier = Modifier
                    .offset(x = 100.dp, y = (-40).dp)
                    .size(160.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .offset(x = (-60).dp, y = 80.dp)
                    .size(140.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "NEXT TURN",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Text(
                                    text = turn.schedule.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = turn.participant.name,
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Today",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = " • 11:59 PM",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Orb Icon
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .size(64.dp)
                            .shadow(8.dp, RoundedCornerShape(20.dp), spotColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(16.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (isCompleted) {
                        Button(
                            onClick = onUndo,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .expressiveLightRay(shape = CircleShape),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = MaterialTheme.colorScheme.onSecondary
                            )
                        ) {
                            Icon(Icons.Default.Undo, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Undo Done", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = onComplete,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .expressiveLightRay(shape = CircleShape),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Mark Done", fontWeight = FontWeight.Bold)
                        }
                    }

                    Surface(
                        onClick = onSwap,
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            Icon(
                                Icons.Default.SwapHoriz,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Swap",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UpcomingTurnCard(
    turn: ResolvedTurn,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = turn.participant.name.take(2).uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = turn.participant.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Tomorrow • 8:00 AM", // Hardcoded for demo, normally from turn date
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = turn.schedule.name,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}
