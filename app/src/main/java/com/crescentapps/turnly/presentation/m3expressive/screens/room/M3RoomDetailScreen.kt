package com.crescentapps.turnly.presentation.m3expressive.screens.room

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import com.crescentapps.turnly.presentation.components.HorizontalDivider
import com.crescentapps.turnly.presentation.components.CircularProgressIndicator
import com.crescentapps.turnly.presentation.components.LinearProgressIndicator
import com.crescentapps.turnly.presentation.components.RadioButton
import com.crescentapps.turnly.presentation.components.Checkbox

import com.crescentapps.turnly.presentation.components.Text
import com.crescentapps.turnly.presentation.components.Icon
import com.crescentapps.turnly.presentation.theme.LocalTurnlyColors
import androidx.compose.ui.text.TextStyle

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crescentapps.turnly.core.model.*
import com.crescentapps.turnly.core.util.QRCodeUtils
import com.crescentapps.turnly.data.repository.RoomRepository
import com.crescentapps.turnly.data.repository.TurnlyRepository
import com.crescentapps.turnly.data.sync.SyncWorker
import androidx.compose.material3.*
import com.crescentapps.turnly.presentation.m3expressive.components.TurnlyExpressiveButton
import com.crescentapps.turnly.presentation.m3expressive.components.TurnlyExpressiveCard
import com.crescentapps.turnly.presentation.screens.room.RoomDetailViewModel
import com.crescentapps.turnly.presentation.catalog.utils.LocalBackdrop
import com.crescentapps.turnly.presentation.components.TurnlyRoleBadge
import com.crescentapps.turnly.presentation.components.TurnlyRoomStatusBadge
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch



@Composable
fun M3RoomDetailScreen(
    viewModel: RoomDetailViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSettings: (Long) -> Unit,
    onScheduleClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val backdrop = LocalBackdrop.current
    var showInviteDialog by remember { mutableStateOf(false) }

    val room = uiState.room

    if (room == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val colors = com.crescentapps.turnly.presentation.theme.LocalTurnlyColors.current
    val adaptiveColor = MaterialTheme.colorScheme.onBackground

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }

            Text(
                text = room.name,
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                fontWeight = FontWeight.Bold,
                color = adaptiveColor
            )

            IconButton(onClick = { onNavigateToSettings(room.id) }) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Status Card
            item {
                TurnlyExpressiveCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(56.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Home,
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                            
                            TurnlyRoomStatusBadge(syncState = room.syncState)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Room Code",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = room.roomCode,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        letterSpacing = 2.sp
                                    )
                                }
                                
                                TurnlyRoleBadge(role = room.role)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        
                        TurnlyExpressiveButton(
                            text = "Sync Now",
                            icon = { Icon(Icons.Default.Sync, contentDescription = null) },
                            onClick = {
                                viewModel.syncNow(context)
                                Toast.makeText(context, "Synchronizing...", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            isPrimary = false
                        )
                    }
                }
            }

            // Action: Invite Member
            item {
                TurnlyExpressiveButton(
                    text = "Invite Member (QR & Code)",
                    icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                    onClick = { showInviteDialog = true },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Connected Members
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Members (${uiState.members.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    uiState.members.forEach { member ->
                        TurnlyExpressiveCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    Column {
                                        Text(
                                            text = member.displayName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (member.presence == MemberPresence.ONLINE) "Online" else "Offline",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (member.role == RoomRole.OWNER) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                                ) {
                                    Text(
                                        text = if (member.role == RoomRole.OWNER) "Owner" else "Member",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (member.role == RoomRole.OWNER) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Shared Schedules
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Shared Schedules (${uiState.sharedSchedules.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                if (uiState.sharedSchedules.isEmpty()) {
                    Text(
                        text = "No schedules shared in this room yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.sharedSchedules.forEach { sInfo ->
                            val localSched = uiState.localSchedulesMap[sInfo.localScheduleId]
                            TurnlyExpressiveCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                onClick = { localSched?.let { onScheduleClick(it.id) } }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = MaterialTheme.colorScheme.secondaryContainer,
                                            modifier = Modifier.size(44.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(Icons.Default.Event, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                                            }
                                        }
                                        Column {
                                            Text(
                                                text = localSched?.name ?: "Shared Schedule",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = localSched?.type?.displayName ?: "Rotation",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = "View",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Invite Modal Dialog (Clean layout with no duplicate action buttons)
    if (showInviteDialog) {
        val deepLink = QRCodeUtils.createInviteDeepLink(
            RoomInvitePayload(
                roomId = room.remoteRoomId,
                roomCode = room.roomCode,
                roomName = room.name,
                ownerName = room.ownerDisplayName,
                inviteToken = "token_${room.roomCode}"
            )
        )
        val qrBmp = remember(room.roomCode) {
            QRCodeUtils.generateQRCodeBitmap(deepLink, 400, 400)
        }

        com.crescentapps.turnly.presentation.m3expressive.components.TurnlyExpressiveDialog(
            title = "Invite to ${room.name}",
            message = "",
            confirmText = "Share Invite",
            dismissText = "Close",
            onConfirm = {
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "Join my Turnly room: ${room.name}\nRoom Code: ${room.roomCode}\nOpen Turnly and choose Join Room."
                    )
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(sendIntent, "Share Room Invitation"))
            },
            onDismissRequest = { showInviteDialog = false }
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Image(
                    bitmap = qrBmp.asImageBitmap(),
                    contentDescription = "Invite QR Code",
                    modifier = Modifier
                        .sizeIn(minWidth = 160.dp, maxWidth = 200.dp, minHeight = 160.dp, maxHeight = 200.dp)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(10.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = room.roomCode,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 3.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Turnly Room Code", room.roomCode))
                            Toast.makeText(context, "Room code copied", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Code")
                    }
                }
            }
        }
    }
}

