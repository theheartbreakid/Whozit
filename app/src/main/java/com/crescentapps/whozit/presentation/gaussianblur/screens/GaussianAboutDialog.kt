package com.crescentapps.whozit.presentation.gaussianblur.screens

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
import com.crescentapps.whozit.presentation.components.HorizontalDivider
import com.crescentapps.whozit.presentation.components.openUrl
import com.crescentapps.whozit.presentation.components.sendEmail
import com.crescentapps.whozit.presentation.gaussianblur.components.GaussianCard
import com.crescentapps.whozit.presentation.gaussianblur.theme.LocalGaussianBlurColors

@Composable
fun GaussianAboutDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val aboutInfo = remember { AboutInfo() }
    val currentVersion = remember { UpdateManager.getInstance(context).currentVersionName }
    val colors = LocalGaussianBlurColors.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        GaussianCard(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(24.dp)
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
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = colors.textPrimary)
                    }
                }

                HorizontalDivider(color = colors.textPrimary.copy(alpha = 0.1f))

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
                            Text(aboutInfo.appName, fontSize = 28.sp, fontWeight = FontWeight.Black, color = colors.textPrimary)
                            Text("Version $currentVersion", fontSize = 14.sp, color = colors.textSecondary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = colors.accent.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    "Student Passion Project",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.accent
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(aboutInfo.description, fontSize = 14.sp, color = colors.textSecondary, textAlign = TextAlign.Center)
                        }
                    }

                    // Developer Profile
                    item {
                        Text("Developer", fontWeight = FontWeight.Bold, color = colors.textPrimary, modifier = Modifier.padding(bottom = 8.dp))
                        Text(aboutInfo.developerName, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = colors.textPrimary)
                        Text(aboutInfo.developerRole, fontSize = 14.sp, color = colors.textSecondary)
                        Text(aboutInfo.developerLocation, fontSize = 14.sp, color = colors.textSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(aboutInfo.developerIntro, fontSize = 14.sp, color = colors.textSecondary)
                    }

                    // Education
                    item {
                        Text("Education", fontWeight = FontWeight.Bold, color = colors.textPrimary, modifier = Modifier.padding(vertical = 8.dp))
                    }
                    items(aboutInfo.education) { edu ->
                        Column(modifier = Modifier.padding(bottom = 12.dp)) {
                            Text(edu.degree, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = colors.textPrimary)
                            Text(edu.institution, fontSize = 12.sp, color = colors.textSecondary)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(edu.years, fontSize = 12.sp, color = colors.textSecondary)
                                Text("CGPA: ${edu.cgpa}", fontSize = 12.sp, color = colors.textSecondary)
                            }
                        }
                    }

                    // Skills
                    item {
                        Text("Skills", fontWeight = FontWeight.Bold, color = colors.textPrimary, modifier = Modifier.padding(vertical = 8.dp))
                        Text("Web: ${aboutInfo.skills.web.joinToString(", ")}", fontSize = 14.sp, color = colors.textSecondary, modifier = Modifier.padding(bottom = 4.dp))
                        Text("Programming: ${aboutInfo.skills.programmingAndDb.joinToString(", ")}", fontSize = 14.sp, color = colors.textSecondary, modifier = Modifier.padding(bottom = 4.dp))
                        Text("Tools: ${aboutInfo.skills.tools.joinToString(", ")}", fontSize = 14.sp, color = colors.textSecondary)
                    }

                    // Projects
                    item {
                        Text("Other Projects", fontWeight = FontWeight.Bold, color = colors.textPrimary, modifier = Modifier.padding(vertical = 8.dp))
                    }
                    items(aboutInfo.projects) { proj ->
                        Column(modifier = Modifier.padding(bottom = 12.dp)) {
                            Text(proj.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = colors.textPrimary)
                            Text(proj.description, fontSize = 12.sp, color = colors.textSecondary)
                            Text("Tech: ${proj.technologies}", fontSize = 12.sp, color = colors.textSecondary.copy(alpha = 0.8f))
                        }
                    }

                    // Links
                    item {
                        Text("Links", fontWeight = FontWeight.Bold, color = colors.textPrimary, modifier = Modifier.padding(vertical = 8.dp))
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                            IconButton(onClick = { openUrl(context, aboutInfo.links.githubProfile) }) {
                                Icon(Icons.Outlined.Person, contentDescription = "GitHub Profile", tint = colors.accent)
                            }
                            IconButton(onClick = { openUrl(context, aboutInfo.links.whozitRepo) }) {
                                Icon(Icons.Outlined.Code, contentDescription = "Repository", tint = colors.accent)
                            }
                            IconButton(onClick = { openUrl(context, aboutInfo.links.releases) }) {
                                Icon(Icons.Outlined.SystemUpdate, contentDescription = "Releases", tint = colors.accent)
                            }
                            IconButton(onClick = { sendEmail(context, aboutInfo.links.email) }) {
                                Icon(Icons.Outlined.Email, contentDescription = "Email", tint = colors.accent)
                            }
                        }
                    }

                    // Acknowledgements
                    item {
                        Text("Acknowledgements", fontWeight = FontWeight.Bold, color = colors.textPrimary, modifier = Modifier.padding(vertical = 8.dp))
                    }
                    items(aboutInfo.acknowledgements) { ack ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Text(
                                ack.libraryName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.accent,
                                modifier = Modifier.clickable { openUrl(context, ack.url) }
                            )
                            Text(ack.authors, fontSize = 12.sp, color = colors.textSecondary)
                        }
                    }

                    // Project Disclosure
                    item {
                        HorizontalDivider(color = colors.textPrimary.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 12.dp))
                        Text(
                            aboutInfo.projectDisclosure,
                            fontSize = 12.sp,
                            color = colors.textSecondary,
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