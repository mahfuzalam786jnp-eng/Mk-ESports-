package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.model.RegistrationStatus
import com.example.model.UiState
import com.example.model.UserProfile
import com.example.model.UserRegistration
import com.example.ui.components.EmptyArenaCard
import com.example.ui.components.MkBrandHeader
import com.example.ui.components.RegistrationStatusPill
import com.example.ui.components.StatusPill
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.ElevatedSlate
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.MoltenGold
import com.example.ui.theme.MutedSilver
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SubtleBorder

@Composable
fun MyMatchesScreen(
    userProfile: UserProfile?,
    registrationsState: UiState<List<UserRegistration>>,
    isOwnerAdmin: Boolean,
    onTournamentClick: (String) -> Unit,
    onWalletClick: () -> Unit,
    onAdminClick: () -> Unit
) {
    var filterStatus by remember { mutableStateOf<RegistrationStatus?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack)
    ) {
        MkBrandHeader(
            demoBalance = userProfile?.demoWalletBalance,
            isOwnerAdmin = isOwnerAdmin,
            onWalletClick = onWalletClick,
            onAdminClick = onAdminClick
        )

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = "MY MATCHES & HISTORY",
                style = MaterialTheme.typography.headlineMedium,
                color = PureWhite
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = filterStatus == null,
                    onClick = { filterStatus = null },
                    label = { Text("ALL") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CrimsonRed,
                        selectedLabelColor = PureWhite,
                        containerColor = ElevatedSlate,
                        labelColor = MutedSilver
                    )
                )
                RegistrationStatus.entries.forEach { st ->
                    FilterChip(
                        selected = filterStatus == st,
                        onClick = { filterStatus = st },
                        label = { Text(st.label.uppercase()) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CrimsonRed,
                            selectedLabelColor = PureWhite,
                            containerColor = ElevatedSlate,
                            labelColor = MutedSilver
                        )
                    )
                }
            }
        }

        when (registrationsState) {
            is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MoltenGold)
                }
            }
            is UiState.Error -> {
                Box(modifier = Modifier.padding(16.dp)) {
                    EmptyArenaCard(Icons.Filled.SportsEsports, "Unable to load matches", registrationsState.message)
                }
            }
            is UiState.Success -> {
                val filtered = registrationsState.data.filter {
                    filterStatus == null || it.statusEnum == filterStatus
                }
                if (filtered.isEmpty()) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        EmptyArenaCard(
                            icon = Icons.Filled.SportsEsports,
                            title = "No tournament entries yet",
                            subtitle = "Join a Solo, Duo, or Squad tournament from the Home Arena to view your slot status & room IDs here."
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filtered, key = { it.tournamentId }) { reg ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onTournamentClick(reg.tournamentId) },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = CarbonSurface),
                                border = BorderStroke(1.dp, SubtleBorder)
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        StatusPill(reg.modeEnum.label, GoldContainer, GoldBright)
                                        RegistrationStatusPill(reg.statusEnum, reg.slotNumber)
                                    }
                                    Text(
                                        text = reg.tournamentTitle,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = PureWhite
                                    )
                                    Text(
                                        text = "FF UID: ${reg.freeFireUid} • IGN: ${reg.freeFireIgn}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MutedSilver
                                    )
                                    if (reg.statusEnum == RegistrationStatus.APPROVED) {
                                        Text(
                                            text = "Tap to view Slot #${reg.slotNumber} & Room ID/Password",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MoltenGold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
