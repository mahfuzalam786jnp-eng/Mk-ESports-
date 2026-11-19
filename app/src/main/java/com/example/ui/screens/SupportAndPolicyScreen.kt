package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.SupportCategory
import com.example.model.SupportTicket
import com.example.model.UiState
import com.example.ui.components.StatusPill
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberPending
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.ElevatedSlate
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldWin
import com.example.ui.theme.MoltenGold
import com.example.ui.theme.MutedSilver
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SubtleBorder

@Composable
fun SupportAndPolicyScreen(
    playerIgn: String,
    ticketsState: UiState<List<SupportTicket>>,
    isActionLoading: Boolean,
    onBack: () -> Unit,
    onSubmitTicket: (SupportCategory, String, String) -> Unit
) {
    BackHandler(onBack = onBack)

    var selectedCategory by remember { mutableStateOf(SupportCategory.TOURNAMENT) }
    var subject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("support_back_button")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = PureWhite)
            }
            Text(
                text = "SUPPORT & PRIVACY POLICY",
                style = MaterialTheme.typography.headlineMedium,
                color = MoltenGold
            )
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CarbonSurface),
                    border = BorderStroke(1.dp, SubtleBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.SupportAgent, null, tint = MoltenGold)
                            Spacer(modifier = Modifier.padding(4.dp))
                            Text("CONTACT MK ESPORTS SUPPORT", style = MaterialTheme.typography.titleMedium, color = PureWhite)
                        }
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(SupportCategory.entries) { cat ->
                                FilterChip(
                                    selected = selectedCategory == cat,
                                    onClick = { selectedCategory = cat },
                                    label = { Text(cat.label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CrimsonRed,
                                        selectedLabelColor = PureWhite,
                                        containerColor = ElevatedSlate,
                                        labelColor = MutedSilver
                                    )
                                )
                            }
                        }
                        OutlinedTextField(
                            value = subject,
                            onValueChange = { subject = it.take(80) },
                            label = { Text("Subject") },
                            singleLine = true,
                            colors = esportsTextFieldColors(),
                            modifier = Modifier.fillMaxWidth().testTag("support_subject_input")
                        )
                        OutlinedTextField(
                            value = message,
                            onValueChange = { message = it.take(500) },
                            label = { Text("Describe your issue (Include Tournament Title / UID)") },
                            minLines = 3,
                            colors = esportsTextFieldColors(),
                            modifier = Modifier.fillMaxWidth().testTag("support_message_input")
                        )
                        if (localError != null) {
                            Text(localError ?: "", style = MaterialTheme.typography.bodySmall, color = CrimsonRed)
                        }
                        Button(
                            onClick = {
                                when {
                                    subject.trim().length < 3 -> localError = "Enter a subject (3+ characters)."
                                    message.trim().length < 5 -> localError = "Enter message details (5+ characters)."
                                    else -> {
                                        localError = null
                                        onSubmitTicket(selectedCategory, subject, message)
                                        subject = ""
                                        message = ""
                                    }
                                }
                            },
                            enabled = !isActionLoading,
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("submit_support_ticket_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                        ) {
                            Text("SUBMIT SUPPORT TICKET", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }

            if (ticketsState is UiState.Success && ticketsState.data.isNotEmpty()) {
                item {
                    Text("MY SUPPORT TICKETS", style = MaterialTheme.typography.titleLarge, color = PureWhite)
                }
                items(ticketsState.data, key = { it.id }) { ticket ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CarbonSurface),
                        border = BorderStroke(1.dp, SubtleBorder)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(ticket.subject, style = MaterialTheme.typography.titleSmall, color = PureWhite)
                                StatusPill(
                                    text = ticket.status,
                                    containerColor = if (ticket.status == "RESOLVED") EmeraldContainer else AmberContainer,
                                    contentColor = if (ticket.status == "RESOLVED") EmeraldWin else AmberPending
                                )
                            }
                            Text(ticket.message, style = MaterialTheme.typography.bodySmall, color = MutedSilver)
                            if (ticket.adminReply.isNotBlank()) {
                                Text(
                                    text = "Admin Reply: ${ticket.adminReply}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MoltenGold
                                )
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CarbonSurface),
                    border = BorderStroke(1.dp, SubtleBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Policy, null, tint = MoltenGold)
                            Spacer(modifier = Modifier.padding(4.dp))
                            Text("PRIVACY POLICY & FAIR PLAY", style = MaterialTheme.typography.titleMedium, color = PureWhite)
                        }
                        Text(
                            text = "1. Data Privacy: MK Esports collects only your verified sign-in identifier, mobile number, and Free Fire MAX UID/IGN to manage tournament slots and room access. Personal contact data is never exposed to other players.\n\n" +
                                "2. Simulated Demo Currency: All MK Demo Coins, Paid-Demo entries, and Prize Rewards are 100% virtual simulation credits. Real-money deposits, cash wagering, and withdrawals are not supported.\n\n" +
                                "3. Room Security: Custom Room IDs and Passwords are encrypted in transit and restricted strictly to Admin-approved slot holders.\n\n" +
                                "4. Fair Play: Hacking, teaming in Solo/Duo, or emulator spoofing results in immediate slot rejection and leaderboard removal.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedSilver
                        )
                    }
                }
            }
        }
    }
}
