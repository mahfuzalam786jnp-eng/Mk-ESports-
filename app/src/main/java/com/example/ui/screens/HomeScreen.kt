package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.Tournament
import com.example.model.TournamentCategory
import com.example.model.TournamentMode
import com.example.model.TournamentStatus
import com.example.model.UiState
import com.example.model.UserProfile
import com.example.ui.components.EmptyArenaCard
import com.example.ui.components.MkBrandHeader
import com.example.ui.components.StatusPill
import com.example.ui.components.TournamentCard
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CrimsonBright
import com.example.ui.theme.CrimsonContainer
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
fun HomeScreen(
    userProfile: UserProfile?,
    tournamentsState: UiState<List<Tournament>>,
    isOwnerAdmin: Boolean,
    isActionLoading: Boolean,
    onTournamentClick: (String) -> Unit,
    onWalletClick: () -> Unit,
    onAdminClick: () -> Unit,
    onSeedTournaments: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMode by remember { mutableStateOf<TournamentMode?>(null) }
    var selectedCategory by remember { mutableStateOf<TournamentCategory?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack)
    ) {
        MkBrandHeader(
            demoBalance = userProfile?.demoWalletBalance,
            isOwnerAdmin = isOwnerAdmin,
            onWalletClick = onWalletClick,
            onAdminClick = onAdminClick
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                FeaturedArenaBanner(
                    userProfile = userProfile,
                    featuredTournament = (tournamentsState as? UiState.Success)?.data?.firstOrNull {
                        it.statusEnum == TournamentStatus.LIVE || it.statusEnum == TournamentStatus.UPCOMING
                    },
                    onFeaturedClick = onTournamentClick
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "FREE FIRE MAX ARENAS",
                        style = MaterialTheme.typography.headlineMedium,
                        color = PureWhite
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            ModeFilterChip(
                                label = "ALL MODES",
                                selected = selectedMode == null,
                                onClick = { selectedMode = null },
                                testTag = "filter_mode_all"
                            )
                        }
                        items(TournamentMode.entries) { mode ->
                            ModeFilterChip(
                                label = mode.label.uppercase(),
                                selected = selectedMode == mode,
                                onClick = { selectedMode = mode },
                                testTag = "filter_mode_${mode.name.lowercase()}"
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CategoryFilterChip(
                            label = "ALL CATEGORIES",
                            selected = selectedCategory == null,
                            onClick = { selectedCategory = null },
                            testTag = "filter_cat_all"
                        )
                        CategoryFilterChip(
                            label = "FREE",
                            selected = selectedCategory == TournamentCategory.FREE,
                            onClick = { selectedCategory = TournamentCategory.FREE },
                            testTag = "filter_cat_free"
                        )
                        CategoryFilterChip(
                            label = "PAID DEMO",
                            selected = selectedCategory == TournamentCategory.PAID_DEMO,
                            onClick = { selectedCategory = TournamentCategory.PAID_DEMO },
                            testTag = "filter_cat_paid"
                        )
                    }
                }
            }

            when (tournamentsState) {
                is UiState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MoltenGold)
                        }
                    }
                }
                is UiState.Error -> {
                    item {
                        EmptyArenaCard(
                            icon = Icons.Filled.Refresh,
                            title = "Could not load tournaments",
                            subtitle = tournamentsState.message
                        )
                    }
                }
                is UiState.Success -> {
                    val filtered = tournamentsState.data.filter { tourney ->
                        (selectedMode == null || tourney.modeEnum == selectedMode) &&
                            (selectedCategory == null || tourney.categoryEnum == selectedCategory)
                    }
                    if (filtered.isEmpty()) {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                EmptyArenaCard(
                                    icon = Icons.Filled.EmojiEvents,
                                    title = "No matching tournaments",
                                    subtitle = if (tournamentsState.data.isEmpty()) {
                                        "The MK Esports arena has no scheduled matches yet."
                                    } else {
                                        "Try switching Solo / Duo / Squad or category filters."
                                    }
                                )
                                if (isOwnerAdmin && tournamentsState.data.isEmpty()) {
                                    Button(
                                        onClick = onSeedTournaments,
                                        enabled = !isActionLoading,
                                        modifier = Modifier.fillMaxWidth().testTag("seed_tournaments_button"),
                                        colors = ButtonDefaults.buttonColors(containerColor = MoltenGold, contentColor = ObsidianBlack)
                                    ) {
                                        Icon(Icons.Filled.Shield, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("OWNER: PUBLISH 4 STARTER TOURNAMENTS", style = MaterialTheme.typography.labelLarge)
                                    }
                                }
                            }
                        }
                    } else {
                        items(filtered, key = { it.id }) { tournament ->
                            TournamentCard(
                                tournament = tournament,
                                onClick = { onTournamentClick(tournament.id) }
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun FeaturedArenaBanner(
    userProfile: UserProfile?,
    featuredTournament: Tournament?,
    onFeaturedClick: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CarbonSurface),
        border = BorderStroke(1.dp, MoltenGold.copy(alpha = 0.45f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(164.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_mk_hero_banner),
                contentDescription = "MK Esports Featured Arena",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                ObsidianBlack.copy(alpha = 0.94f),
                                ObsidianBlack.copy(alpha = 0.65f),
                                ObsidianBlack.copy(alpha = 0.35f)
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusPill(
                        text = if (featuredTournament != null) "FEATURED MATCH" else "WELCOME SOLDIER",
                        containerColor = CrimsonContainer,
                        contentColor = GoldBright,
                        borderColor = MoltenGold.copy(alpha = 0.5f)
                    )
                    if (userProfile != null) {
                        Text(
                            text = "IGN: ${userProfile.freeFireIgn}",
                            style = MaterialTheme.typography.labelSmall,
                            color = GoldBright
                        )
                    }
                }

                Column {
                    Text(
                        text = featuredTournament?.title ?: "DOMINATE THE BERMUDA DROP",
                        style = MaterialTheme.typography.headlineMedium,
                        color = PureWhite
                    )
                    Text(
                        text = if (featuredTournament != null) {
                            "${featuredTournament.modeEnum.label} • Prize Pool ${featuredTournament.prizePoolDemo} DC • ${featuredTournament.filledSlots}/${featuredTournament.maxSlots} Slots"
                        } else {
                            "Compete in Free Fire MAX Solo, Duo & Squad custom rooms."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedSilver
                    )
                }

                if (featuredTournament != null) {
                    Surface(
                        onClick = { onFeaturedClick(featuredTournament.id) },
                        shape = RoundedCornerShape(8.dp),
                        color = CrimsonRed
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocalFireDepartment,
                                contentDescription = null,
                                tint = MoltenGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ENTER LOBBY",
                                style = MaterialTheme.typography.labelLarge,
                                color = PureWhite
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModeFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelLarge) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = CrimsonRed,
            selectedLabelColor = PureWhite,
            containerColor = ElevatedSlate,
            labelColor = MutedSilver
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = SubtleBorder,
            selectedBorderColor = MoltenGold
        ),
        modifier = Modifier.testTag(testTag)
    )
}

@Composable
private fun CategoryFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = GoldContainer,
            selectedLabelColor = GoldBright,
            containerColor = CarbonSurface,
            labelColor = MutedSilver
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = SubtleBorder,
            selectedBorderColor = MoltenGold
        ),
        modifier = Modifier.testTag(testTag)
    )
}
