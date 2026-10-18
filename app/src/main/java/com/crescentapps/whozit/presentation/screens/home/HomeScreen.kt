package com.crescentapps.whozit.presentation.screens.home

import androidx.compose.foundation.layout.*
import com.crescentapps.whozit.presentation.components.Text
import com.crescentapps.whozit.presentation.components.Icon
import com.crescentapps.whozit.presentation.theme.LocalWhozitColors
import androidx.compose.ui.text.TextStyle

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Undo
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crescentapps.whozit.core.model.OccurrenceStatus
import com.crescentapps.whozit.core.model.ResolvedTurn
import com.crescentapps.whozit.core.model.ScheduleType
import com.crescentapps.whozit.core.util.DateUtils
import com.crescentapps.whozit.presentation.catalog.utils.LocalBackdrop
import com.crescentapps.whozit.presentation.components.EmptyTurnState
import com.crescentapps.whozit.presentation.components.LocalPrismalAdaptiveColor
import com.crescentapps.whozit.presentation.components.TurnCard
import com.crescentapps.whozit.presentation.components.liquid.LiquidButton
import com.crescentapps.whozit.presentation.components.liquid.LiquidCard
import com.crescentapps.whozit.presentation.components.liquid.LiquidChip
import com.crescentapps.whozit.presentation.components.liquid.LiquidDialog
import com.crescentapps.whozit.presentation.theme.LiquidColors
import com.crescentapps.whozit.presentation.theme.LocalWhozitColors
import com.crescentapps.whozit.presentation.theme.withDockPadding
import com.kyant.backdrop.backdrops.rememberLayerBackdrop

import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onCreateSchedule: () -> Unit,
    onScheduleClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val backdrop = LocalBackdrop.current ?: rememberLayerBackdrop()
    val adaptiveColor = LocalPrismalAdaptiveColor.current
    val colors = LocalWhozitColors.current

    var turnForAmountDialog by remember { mutableStateOf<ResolvedTurn?>(null) }
    var enteredAmount by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Whozit",
                    style = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold),
                    fontWeight = FontWeight.Black,
                    color = adaptiveColor
                )
                Text(
                    text = DateUtils.formatDisplay(DateUtils.todayString()),
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal),
                    color = adaptiveColor.copy(alpha = 0.7f)
                )
            }

            if (uiState.todayTurns.isNotEmpty()) {
                LiquidButton(
                    onClick = onCreateSchedule,
                    backdrop = backdrop,
                    surfaceColor = colors.accent.copy(alpha = 0.2f),
                    tint = colors.accent.copy(alpha = 0.35f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Schedule",
                        modifier = Modifier.size(18.dp),
                        tint = colors.accent
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New", fontWeight = FontWeight.Bold, color = colors.accent)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Content
        if (!uiState.isLoading && uiState.todayTurns.isEmpty()) {
            EmptyTurnState(
                title = "All clear for today!",
                description = "No turns are due right now. You can create a schedule to take turns for expenses, chores, pet care, or team duties.",
                actionText = "Create a Schedule",
                onAction = onCreateSchedule
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp).withDockPadding().withDockPadding()
            ) {
                // High-priority Hero Banner if there is a pending turn today
                val firstPending = uiState.todayTurns.firstOrNull { it.status == OccurrenceStatus.PENDING }
                if (firstPending != null) {
                    item(key = "hero_who_is_next") {
                        LiquidCard(
                            backdrop = backdrop,
                            modifier = Modifier.fillMaxWidth(),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(26.dp),
                            onClick = { onScheduleClick(firstPending.schedule.id) }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "WHO GOES NEXT?",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        color = LocalWhozitColors.current.accent
                                    )
                                    Text(
                                        text = firstPending.schedule.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = adaptiveColor.copy(alpha = 0.7f)
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    com.crescentapps.whozit.presentation.components.ParticipantAvatar(
                                        participant = firstPending.participant,
                                        size = 56.dp
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${firstPending.participant.name}'s turn",
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Black,
                                            color = adaptiveColor
                                        )
                                        if (firstPending.schedule.type == ScheduleType.MONEY && firstPending.schedule.defaultAmount != null) {
                                            Text(
                                                text = com.crescentapps.whozit.core.util.CurrencyUtils.formatAmount(
                                                    firstPending.schedule.defaultAmount,
                                                    firstPending.schedule.currencyCode
                                                ),
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = LocalWhozitColors.current.accent
                                            )
                                        } else {
                                            Text(
                                                text = "Scheduled for today",
                                                fontSize = 13.sp,
                                                color = adaptiveColor.copy(alpha = 0.65f)
                                            )
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    if (firstPending.status == OccurrenceStatus.COMPLETED) {
                                        LiquidButton(
                                            onClick = { viewModel.undoCompletion(firstPending) },
                                            backdrop = backdrop,
                                            modifier = Modifier.weight(1f),
                                            surfaceColor = colors.surface.copy(alpha = 0.2f),
                                            tint = adaptiveColor.copy(alpha = 0.4f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Undo,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = LocalPrismalAdaptiveColor.current
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Undo Done",
                                                fontWeight = FontWeight.Bold,
                                                color = LocalPrismalAdaptiveColor.current
                                            )
                                        }
                                    } else {
                                        LiquidButton(
                                            onClick = {
                                                if (firstPending.schedule.type == ScheduleType.MONEY) {
                                                    turnForAmountDialog = firstPending
                                                    enteredAmount = firstPending.schedule.defaultAmount?.toString() ?: ""
                                                } else {
                                                    viewModel.markTurnComplete(firstPending)
                                                }
                                            },
                                            backdrop = backdrop,
                                            modifier = Modifier.weight(1f),
                                            surfaceColor = LocalWhozitColors.current.accent.copy(alpha = 0.25f),
                                            tint = LocalWhozitColors.current.accent.copy(alpha = 0.4f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = LocalPrismalAdaptiveColor.current
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (firstPending.schedule.type == ScheduleType.MONEY) "Record & Done" else "Mark as Done",
                                                fontWeight = FontWeight.Bold,
                                                color = LocalPrismalAdaptiveColor.current
                                            )
                                        }
                                    }
                                    LiquidButton(
                                        onClick = { viewModel.skipTurn(firstPending) },
                                        backdrop = backdrop,
                                        modifier = Modifier.weight(1f),
                                        surfaceColor = colors.surface.copy(alpha = 0.1f),
                                        tint = adaptiveColor.copy(alpha = 0.5f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SwapHoriz,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = adaptiveColor
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Swap",
                                            fontWeight = FontWeight.Bold,
                                            color = adaptiveColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Section Summary & Filter Chips
                item(key = "header_filters") {
                    val colors = LocalWhozitColors.current
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TODAY'S TURNS",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = LocalWhozitColors.current.accent
                            )
                            Text(
                                text = "${uiState.totalPending} left · ${uiState.totalCompleted} done",
                                fontSize = 12.sp,
                                color = adaptiveColor.copy(alpha = 0.7f)
                            )
                        }

                        // Filter Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val filters = listOf(
                                HomeFilter.ALL to "All Turns",
                                HomeFilter.LOCAL to "My Schedules",
                                HomeFilter.SHARED to "Shared with Others"
                            )
                            filters.forEach { (filter, label) ->
                                val isSelected = uiState.currentFilter == filter
                                LiquidChip(
                                    onClick = { viewModel.setFilter(filter) },
                                    backdrop = backdrop,
                                    tint = if (isSelected) LocalWhozitColors.current.accent.copy(alpha = 0.4f) else Color.Transparent,
                                    surfaceColor = if (isSelected) LocalWhozitColors.current.accent.copy(alpha = 0.2f) else colors.surface.copy(alpha = 0.08f)
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) LocalWhozitColors.current.accent else adaptiveColor.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Turns list
                items(uiState.filteredTurns, key = { it.schedule.id }) { turn ->
                    TurnCard(
                        resolvedTurn = turn,
                        onMarkDone = { sId, _ ->
                            if (turn.schedule.type == ScheduleType.MONEY) {
                                turnForAmountDialog = turn
                                enteredAmount = turn.schedule.defaultAmount?.toString() ?: ""
                            } else {
                                viewModel.markTurnComplete(turn)
                            }
                        },
                        onSkip = { sId -> viewModel.skipTurn(turn) },
                        onUndo = { sId -> viewModel.undoCompletion(turn) },
                        onClick = { onScheduleClick(turn.schedule.id) }
                    )
                }
            }
        }
    }

    // Expense Confirmation Liquid Dialog
    turnForAmountDialog?.let { turn ->
        LiquidDialog(
            onDismissRequest = { turnForAmountDialog = null },
            backdrop = backdrop,
            title = "Record Expense",
            message = "Enter the amount paid for \"${turn.schedule.name}\":",
            positiveText = "Save & Mark Done",
            negativeText = "Cancel",
            onPositive = {
                val amount = enteredAmount.toDoubleOrNull()
                viewModel.markTurnComplete(turn, amount)
                turnForAmountDialog = null
            },
            accentColor = LocalWhozitColors.current.accent
        ) {
            com.crescentapps.whozit.presentation.components.liquid.AdaptiveGlassTextField(
                value = enteredAmount,
                onValueChange = { enteredAmount = it },
                label = "Amount (${turn.schedule.currencyCode})",
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
