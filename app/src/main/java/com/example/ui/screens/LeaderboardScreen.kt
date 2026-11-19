package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.LeaderboardEntry
import com.example.model.UiState
import com.example.model.UserProfile
import com.example.ui.components.EmptyArenaCard
import com.example.ui.components.MkBrandHeader
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CrimsonContainer
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.ElevatedSlate
import com.example.ui.theme.EmeraldWin
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.MoltenGold
import com.example.ui.theme.MutedSilver
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SubtleBorder

@Composable
fun LeaderboardScreen(
    userProfile: UserProfile?,
    leaderboardState: UiState<List<LeaderboardEntry>>,
    isOwnerAdmin: Boolean,
    onWalletClick: () -> Unit,
    onAdminClick: () -> Unit
) {
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
                text = "MK ESPORTS LEADERBOARD",
                style = MaterialTheme.typography.headlineMedium,
                color = MoltenGold
            )
            Text(
                text = "Ranked by official match points, Booyahs & eliminations",
                style = MaterialTheme.typography.bodySmall,
                color = MutedSilver
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (leaderboardState) {
            is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MoltenGold)
                }
            }
            is UiState.Error -> {
                Box(modifier = Modifier.padding(16.dp)) {
                    EmptyArenaCard(Icons.Filled.Leaderboard, "Leaderboard unavailable", leaderboardState.message)
                }
            }
            is UiState.Success -> {
                if (leaderboardState.data.isEmpty()) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        EmptyArenaCard(
                            icon = Icons.Filled.EmojiEvents,
                            title = "No leaderboard stats yet",
                            subtitle = "Play matches and climb the MK Esports Free Fire MAX rankings!"
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(leaderboardState.data, key = { _, item -> item.userId }) { index, entry ->
                            val rank = index + 1
                            val isTop3 = rank <= 3
                            val accent = when (rank) {
                                1 -> MoltenGold
                                2 -> PureWhite
                                3 -> CrimsonRed
                                else -> MutedSilver
                            }
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (rank == 1) GoldContainer else CarbonSurface
                                ),
                                border = BorderStroke(
                                    width = if (isTop3) 1.5.dp else 1.dp,
                                    color = if (isTop3) accent.copy(alpha = 0.6f) else SubtleBorder
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            modifier = Modifier.size(36.dp).clip(CircleShape),
                                            color = if (rank == 1) MoltenGold else ElevatedSlate
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = "#$rank",
                                                    style = MaterialTheme.typography.labelLarge,
                                                    color = if (rank == 1) ObsidianBlack else accent
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = entry.freeFireIgn,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = PureWhite,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "${entry.booyahs} Booyahs • ${entry.totalKills} Kills • ${entry.matchesPlayed} Matches",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MutedSilver
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${entry.points} PTS",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = GoldBright,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${entry.demoWinnings} DC",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = EmeraldWin
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
