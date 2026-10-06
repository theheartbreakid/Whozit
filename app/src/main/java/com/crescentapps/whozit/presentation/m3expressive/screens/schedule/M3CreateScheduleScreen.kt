package com.crescentapps.whozit.presentation.m3expressive.screens.schedule

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.crescentapps.whozit.presentation.m3expressive.components.WhozitExpressiveButton
import com.crescentapps.whozit.presentation.m3expressive.components.WhozitExpressiveCard
import com.crescentapps.whozit.presentation.m3expressive.theme.ExpressiveTokens
import com.crescentapps.whozit.presentation.screens.schedule.ScheduleFormViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun M3CreateScheduleScreen(
    viewModel: ScheduleFormViewModel,
    onNavigateBack: () -> Unit,
    onCreatedSuccessfully: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onCreatedSuccessfully()
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = com.crescentapps.whozit.presentation.theme.whozitScaffoldWindowInsets(),
        topBar = {
            TopAppBar(
                title = { Text("New Schedule") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = ExpressiveTokens.spacing.normal)
        ) {
            WhozitExpressiveCard(
                modifier = Modifier.fillMaxWidth().weight(1f),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp)
                ) {
                    when (uiState.currentStep) {
                        1 -> {
                            Text("Schedule Identity", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text("Basic information", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(ExpressiveTokens.spacing.normal))
                            OutlinedTextField(
                                value = uiState.name,
                                onValueChange = { viewModel.updateName(it) },
                                label = { Text("Schedule Name") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp)
                            )
                            Spacer(Modifier.weight(1f))
                            WhozitExpressiveButton(
                                onClick = { viewModel.nextStep() },
                                text = "Next",
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                        2 -> {
                            Text("Turn Order", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text("Who participates in this rotation", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(ExpressiveTokens.spacing.normal))
                            var newName by remember { mutableStateOf("") }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = newName,
                                    onValueChange = { newName = it },
                                    label = { Text("Add Member") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                Spacer(Modifier.width(ExpressiveTokens.spacing.small))
                                WhozitExpressiveButton(
                                    onClick = {
                                        if (newName.isNotBlank()) {
                                            viewModel.addParticipant(newName, null, "")
                                            newName = ""
                                        }
                                    },
                                    text = "Add",
                                    isPrimary = false
                                )
                            }
                            Spacer(Modifier.height(ExpressiveTokens.spacing.normal))
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                itemsIndexed(uiState.participants) { index, p ->
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                                    ) {
                                        ListItem(
                                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                            trailingContent = {
                                                IconButton(onClick = { viewModel.removeParticipant(index) }) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                                                }
                                            }
                                        ) {
                                            Text(p.name, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = ExpressiveTokens.spacing.normal),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                WhozitExpressiveButton(onClick = { viewModel.previousStep() }, text = "Back", isPrimary = false)
                                WhozitExpressiveButton(onClick = { viewModel.nextStep() }, text = "Next")
                            }
                        }
                        3 -> {
                            Text("Review & Save", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(ExpressiveTokens.spacing.normal))
                            Surface(
                                shape = RoundedCornerShape(24.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(Modifier.padding(ExpressiveTokens.spacing.normal)) {
                                    Text("Name: ${uiState.name}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(4.dp))
                                    Text("Participants: ${uiState.participants.size}", style = MaterialTheme.typography.bodyLarge)
                                    Spacer(Modifier.height(4.dp))
                                    Text("Frequency: ${uiState.frequencyType.name}", style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                            Spacer(Modifier.weight(1f))
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = ExpressiveTokens.spacing.normal),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                WhozitExpressiveButton(onClick = { viewModel.previousStep() }, text = "Back", isPrimary = false)
                                WhozitExpressiveButton(onClick = { viewModel.saveSchedule() }, text = "Create Schedule")
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
