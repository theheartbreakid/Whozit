package com.crescentapps.whozit.presentation.m3expressive.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.crescentapps.whozit.core.model.AboutInfo
import com.crescentapps.whozit.core.update.UpdateManager
import com.crescentapps.whozit.presentation.components.openUrl
import com.crescentapps.whozit.presentation.components.sendEmail

@Composable
fun M3AboutDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val aboutInfo = remember { AboutInfo() }
    val currentVersion = remember { UpdateManager.getInstance(context).currentVersionName }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "About",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider()

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // App Identity
                    item {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text(aboutInfo.appName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                            Text("Version $currentVersion", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    "Student Passion Project",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(aboutInfo.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                        }
                    }

                    // Developer Profile
                    item {
                        Text("Developer", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                        Text(aboutInfo.developerName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(aboutInfo.developerRole, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(aboutInfo.developerLocation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(aboutInfo.developerIntro, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    // Education
                    item {
                        Text("Education", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
                    }
                    items(aboutInfo.education) { edu ->
                        Column(modifier = Modifier.padding(bottom = 12.dp)) {
                            Text(edu.degree, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text(edu.institution, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(edu.years, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("CGPA: ${edu.cgpa}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // Skills
                    item {
                        Text("Skills", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
                        Text("Web: ${aboutInfo.skills.web.joinToString(", ")}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp))
                        Text("Programming: ${aboutInfo.skills.programmingAndDb.joinToString(", ")}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp))
                        Text("Tools: ${aboutInfo.skills.tools.joinToString(", ")}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    // Projects
                    item {
                        Text("Other Projects", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
                    }
                    items(aboutInfo.projects) { proj ->
                        Column(modifier = Modifier.padding(bottom = 12.dp)) {
                            Text(proj.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text(proj.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Tech: ${proj.technologies}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                        }
                    }

                    // Links
                    item {
                        Text("Links", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                            FilledTonalIconButton(onClick = { openUrl(context, aboutInfo.links.githubProfile) }) {
                                Icon(Icons.Outlined.Person, contentDescription = "GitHub Profile")
                            }
                            FilledTonalIconButton(onClick = { openUrl(context, aboutInfo.links.whozitRepo) }) {
                                Icon(Icons.Outlined.Code, contentDescription = "Repository")
                            }
                            FilledTonalIconButton(onClick = { openUrl(context, aboutInfo.links.releases) }) {
                                Icon(Icons.Outlined.SystemUpdate, contentDescription = "Releases")
                            }
                            FilledTonalIconButton(onClick = { sendEmail(context, aboutInfo.links.email) }) {
                                Icon(Icons.Outlined.Email, contentDescription = "Email")
                            }
                        }
                    }

                    // Acknowledgements
                    item {
                        Text("Acknowledgements", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
                    }
                    items(aboutInfo.acknowledgements) { ack ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Text(
                                ack.libraryName,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable { openUrl(context, ack.url) }
                            )
                            Text(ack.authors, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // Project Disclosure
                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        Text(
                            aboutInfo.projectDisclosure,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}