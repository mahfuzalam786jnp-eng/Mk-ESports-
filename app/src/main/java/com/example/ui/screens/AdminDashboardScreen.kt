package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.model.OWNER_ADMIN_EMAIL
import com.example.model.RegistrationStatus
import com.example.model.RoomCredential
import com.example.model.SupportTicket
import com.example.model.Tournament
import com.example.model.TournamentCategory
import com.example.model.TournamentMode
import com.example.model.TournamentRegistration
import com.example.model.TournamentStatus
import com.example.model.UiState
import com.example.ui.components.EmptyArenaCard
import com.example.ui.components.RegistrationStatusPill
import com.example.ui.components.TournamentStatusPill
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CrimsonBright
import com.example.ui.theme.CrimsonContainer
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.ElevatedSlate
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldWin
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.MoltenGold
import com.example.ui.theme.MutedSilver
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SubtleBorder

@Composable
fun AdminDashboardScreen(
    isOwnerAdmin: Boolean,
    tournamentsState: UiState<List<Tournament>>,
    selectedTournament: Tournament?,
    selectedRegistrations: List<TournamentRegistration>,
    roomCredential: RoomCredential?,
    allSupportTicketsState: UiState<List<SupportTicket>>,
    isActionLoading: Boolean,
    onBack: () -> Unit,
    onSelectTournament: (String) -> Unit,
    onLoadRoomCredentials: (String) -> Unit,
    onSeedTournaments: () -> Unit,
    onSaveTournament: (
        existing: Tournament?,
        title: String,
        mode: TournamentMode,
        category: TournamentCategory,
        mapName: String,
        entryFeeDemo: Long,
        prizePoolDemo: Long,
        perKillRewardDemo: Long,
        maxSlots: Long,
        scheduledTimeText: String,
        status: TournamentStatus,
        rulesSummary: String
    ) -> Unit,
    onDeleteTournament: (String) -> Unit,
    onApproveRegistration: (String, TournamentRegistration, Long) -> Unit,
    onRejectRegistration: (String, TournamentRegistration) -> Unit,
    onPublishRoom: (String, String, String, String, String) -> Unit,
    onPublishResult: (
        tournamentId: String,
        tournamentTitle: String,
        playerUserId: String,
        freeFireIgn: String,
        freeFireUid: String,
        teamName: String,
        placement: Long,
        kills: Long,
        points: Long,
        prizeDemo: Long
    ) -> Unit,
    onResolveTicket: (String, String) -> Unit
) {
    BackHandler(onBack = onBack)

    var selectedTab by remember { mutableIntStateOf(0) }
    var showTournamentEditor by remember { mutableStateOf(false) }
    var editingTournament by remember { mutableStateOf<Tournament?>(null) }

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
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("admin_back_button")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = PureWhite)
                }
                Column {
                    Text(
                        text = "OWNER ADMIN CONSOLE",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MoltenGold
                    )
                    Text(
                        text = "Owner: $OWNER_ADMIN_EMAIL",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedSilver
                    )
                }
            }
        }

        if (!isOwnerAdmin) {
            Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                EmptyArenaCard(
                    icon = Icons.Filled.Shield,
                    title = "Owner Access Only",
                    subtitle = "Only the single owner-controlled administrator account ($OWNER_ADMIN_EMAIL) can access this console."
                )
            }
            return
        }

        val tabs = listOf("Tournaments", "Rooms & Slots", "Winners", "Support")
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = CarbonSurface,
            contentColor = MoltenGold,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = CrimsonRed
                )
            }
        ) {
            tabs.forEachIndexed { idx, title ->
                Tab(
                    selected = selectedTab == idx,
                    onClick = { selectedTab = idx },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selectedTab == idx) MoltenGold else MutedSilver
                        )
                    }
                )
            }
        }

        val tournaments = (tournamentsState as? UiState.Success)?.data ?: emptyList()

        when (selectedTab) {
            0 -> AdminTournamentsTab(
                tournaments = tournaments,
                isActionLoading = isActionLoading,
                onCreateClick = {
                    editingTournament = null
                    showTournamentEditor = true
                },
                onEditClick = { tourney ->
                    editingTournament = tourney
                    showTournamentEditor = true
                },
                onManageClick = { tourney ->
                    onSelectTournament(tourney.id)
                    onLoadRoomCredentials(tourney.id)
                    selectedTab = 1
                },
                onDeleteClick = onDeleteTournament,
                onSeedClick = onSeedTournaments
            )
            1 -> AdminApprovalsAndRoomTab(
                tournaments = tournaments,
                selectedTournament = selectedTournament,
                registrations = selectedRegistrations,
                roomCredential = roomCredential,
                isActionLoading = isActionLoading,
                onSelectTournament = {
                    onSelectTournament(it)
                    onLoadRoomCredentials(it)
                },
                onApprove = onApproveRegistration,
                onReject = onRejectRegistration,
                onPublishRoom = onPublishRoom
            )
            2 -> AdminWinnersTab(
                tournaments = tournaments,
                selectedTournament = selectedTournament,
                registrations = selectedRegistrations,
                isActionLoading = isActionLoading,
                onSelectTournament = onSelectTournament,
                onPublishResult = onPublishResult
            )
            3 -> AdminSupportTab(
                ticketsState = allSupportTicketsState,
                isActionLoading = isActionLoading,
                onResolveTicket = onResolveTicket
            )
        }
    }

    if (showTournamentEditor) {
        TournamentEditorDialog(
            existing = editingTournament,
            onDismiss = { showTournamentEditor = false },
            onSave = { title, mode, cat, map, entry, prize, kill, slots, time, status, rules ->
                showTournamentEditor = false
                onSaveTournament(editingTournament, title, mode, cat, map, entry, prize, kill, slots, time, status, rules)
            }
        )
    }
}

@Composable
private fun AdminTournamentsTab(
    tournaments: List<Tournament>,
    isActionLoading: Boolean,
    onCreateClick: () -> Unit,
    onEditClick: (Tournament) -> Unit,
    onManageClick: (Tournament) -> Unit,
    onDeleteClick: (String) -> Unit,
    onSeedClick: () -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onCreateClick,
                    modifier = Modifier.weight(1f).height(48.dp).testTag("admin_create_tournament_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                ) {
                    Icon(Icons.Filled.Add, null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("CREATE TOURNAMENT", style = MaterialTheme.typography.labelLarge)
                }
                OutlinedButton(
                    onClick = onSeedClick,
                    enabled = !isActionLoading,
                    border = BorderStroke(1.dp, MoltenGold),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("SEED 4", color = MoltenGold, style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        if (tournaments.isEmpty()) {
            item {
                EmptyArenaCard(Icons.Filled.EmojiEvents, "No tournaments created", "Tap CREATE TOURNAMENT or SEED 4 to launch lobbies.")
            }
        } else {
            items(tournaments, key = { it.id }) { tourney ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CarbonSurface),
                    border = BorderStroke(1.dp, SubtleBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(tourney.title, style = MaterialTheme.typography.titleMedium, color = PureWhite)
                                Text(
                                    "${tourney.modeEnum.label} • ${tourney.categoryEnum.label} • ${tourney.filledSlots}/${tourney.maxSlots} slots",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedSilver
                                )
                            }
                            TournamentStatusPill(tourney.statusEnum)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { onManageClick(tourney) },
                                border = BorderStroke(1.dp, MoltenGold),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("SLOTS & ROOM", color = MoltenGold, style = MaterialTheme.typography.labelSmall)
                            }
                            IconButton(onClick = { onEditClick(tourney) }) {
                                Icon(Icons.Filled.Edit, "Edit", tint = PureWhite)
                            }
                            IconButton(onClick = { onDeleteClick(tourney.id) }) {
                                Icon(Icons.Filled.Delete, "Delete", tint = CrimsonBright)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminApprovalsAndRoomTab(
    tournaments: List<Tournament>,
    selectedTournament: Tournament?,
    registrations: List<TournamentRegistration>,
    roomCredential: RoomCredential?,
    isActionLoading: Boolean,
    onSelectTournament: (String) -> Unit,
    onApprove: (String, TournamentRegistration, Long) -> Unit,
    onReject: (String, TournamentRegistration) -> Unit,
    onPublishRoom: (String, String, String, String, String) -> Unit
) {
    var roomId by remember(selectedTournament?.id, roomCredential?.roomId) { mutableStateOf(roomCredential?.roomId ?: "") }
    var roomPassword by remember(selectedTournament?.id, roomCredential?.roomPassword) { mutableStateOf(roomCredential?.roomPassword ?: "") }
    var serverRegion by remember(selectedTournament?.id, roomCredential?.serverRegion) { mutableStateOf(roomCredential?.serverRegion ?: "India") }
    var instructions by remember(selectedTournament?.id, roomCredential?.instructions) { mutableStateOf(roomCredential?.instructions ?: "Join 10 mins before match start.") }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("SELECT TOURNAMENT", style = MaterialTheme.typography.labelLarge, color = MoltenGold)
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(tournaments, key = { it.id }) { t ->
                    FilterChip(
                        selected = selectedTournament?.id == t.id,
                        onClick = { onSelectTournament(t.id) },
                        label = { Text(t.title.take(24)) },
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

        if (selectedTournament == null) {
            item {
                EmptyArenaCard(Icons.Filled.MeetingRoom, "Pick a tournament above", "Select a tournament to approve players and publish Room ID/Password.")
            }
        } else {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CarbonSurface),
                    border = BorderStroke(1.dp, MoltenGold.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("PUBLISH ROOM ID & PASSWORD", style = MaterialTheme.typography.titleMedium, color = MoltenGold)
                        OutlinedTextField(
                            value = roomId,
                            onValueChange = { roomId = it.take(40) },
                            label = { Text("Custom Room ID") },
                            singleLine = true,
                            colors = esportsTextFieldColors(),
                            modifier = Modifier.fillMaxWidth().testTag("admin_room_id_input")
                        )
                        OutlinedTextField(
                            value = roomPassword,
                            onValueChange = { roomPassword = it.take(40) },
                            label = { Text("Room Password") },
                            singleLine = true,
                            colors = esportsTextFieldColors(),
                            modifier = Modifier.fillMaxWidth().testTag("admin_room_pass_input")
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = serverRegion,
                                onValueChange = { serverRegion = it.take(40) },
                                label = { Text("Server") },
                                singleLine = true,
                                colors = esportsTextFieldColors(),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = instructions,
                                onValueChange = { instructions = it.take(240) },
                                label = { Text("Lobby Note") },
                                singleLine = true,
                                colors = esportsTextFieldColors(),
                                modifier = Modifier.weight(2f)
                            )
                        }
                        Button(
                            onClick = { onPublishRoom(selectedTournament.id, roomId, roomPassword, serverRegion, instructions) },
                            enabled = !isActionLoading && roomId.isNotBlank() && roomPassword.isNotBlank(),
                            modifier = Modifier.fillMaxWidth().testTag("admin_publish_room_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldWin, contentColor = ObsidianBlack)
                        ) {
                            Text("PUBLISH TO APPROVED PLAYERS", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }

            item {
                Text("PLAYER REGISTRATIONS (${registrations.size})", style = MaterialTheme.typography.titleLarge, color = PureWhite)
            }

            if (registrations.isEmpty()) {
                item {
                    Text("No registrations yet for ${selectedTournament.title}.", style = MaterialTheme.typography.bodyMedium, color = MutedSilver)
                }
            } else {
                val nextSlot = (registrations.maxOfOrNull { it.slotNumber } ?: 0L) + 1L
                items(registrations, key = { it.userId }) { reg ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CarbonSurface),
                        border = BorderStroke(1.dp, SubtleBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("${reg.freeFireIgn} (UID: ${reg.freeFireUid})", style = MaterialTheme.typography.titleSmall, color = PureWhite)
                                    if (reg.teamName.isNotBlank()) {
                                        Text("Team: ${reg.teamName} • ${reg.partnerInfo}", style = MaterialTheme.typography.bodySmall, color = GoldBright)
                                    }
                                }
                                RegistrationStatusPill(reg.statusEnum, reg.slotNumber)
                            }
                            if (reg.statusEnum == RegistrationStatus.PENDING) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { onApprove(selectedTournament.id, reg, nextSlot) },
                                        enabled = !isActionLoading,
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldWin, contentColor = ObsidianBlack),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Filled.Check, null)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("APPROVE #$nextSlot")
                                    }
                                    OutlinedButton(
                                        onClick = { onReject(selectedTournament.id, reg) },
                                        enabled = !isActionLoading,
                                        border = BorderStroke(1.dp, CrimsonRed),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Filled.Close, null, tint = CrimsonBright)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("REJECT", color = CrimsonBright)
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

@Composable
private fun AdminWinnersTab(
    tournaments: List<Tournament>,
    selectedTournament: Tournament?,
    registrations: List<TournamentRegistration>,
    isActionLoading: Boolean,
    onSelectTournament: (String) -> Unit,
    onPublishResult: (String, String, String, String, String, String, Long, Long, Long, Long) -> Unit
) {
    var selectedPlayerUid by remember { mutableStateOf("") }
    var playerIgn by remember { mutableStateOf("") }
    var ffUid by remember { mutableStateOf("") }
    var teamName by remember { mutableStateOf("") }
    var placement by remember { mutableStateOf("1") }
    var kills by remember { mutableStateOf("8") }
    var points by remember { mutableStateOf("180") }
    var prizeDemo by remember { mutableStateOf("500") }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("SELECT TOURNAMENT", style = MaterialTheme.typography.labelLarge, color = MoltenGold)
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(tournaments, key = { it.id }) { t ->
                    FilterChip(
                        selected = selectedTournament?.id == t.id,
                        onClick = { onSelectTournament(t.id) },
                        label = { Text(t.title.take(24)) },
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

        if (selectedTournament != null) {
            val approved = registrations.filter { it.statusEnum == RegistrationStatus.APPROVED }
            if (approved.isNotEmpty()) {
                item {
                    Text("TAP APPROVED PLAYER TO AUTO-FILL", style = MaterialTheme.typography.labelSmall, color = MutedSilver)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(approved, key = { it.userId }) { reg ->
                            FilterChip(
                                selected = selectedPlayerUid == reg.userId,
                                onClick = {
                                    selectedPlayerUid = reg.userId
                                    playerIgn = reg.freeFireIgn
                                    ffUid = reg.freeFireUid
                                    teamName = reg.teamName
                                },
                                label = { Text("#${reg.slotNumber} ${reg.freeFireIgn}") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GoldContainer,
                                    selectedLabelColor = GoldBright,
                                    containerColor = CarbonSurface,
                                    labelColor = PureWhite
                                )
                            )
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
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("PUBLISH RESULT & CREDIT DEMO REWARD", style = MaterialTheme.typography.titleMedium, color = MoltenGold)
                        OutlinedTextField(
                            value = playerIgn,
                            onValueChange = { playerIgn = it.take(30) },
                            label = { Text("Winner Free Fire IGN") },
                            singleLine = true,
                            colors = esportsTextFieldColors(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = teamName,
                            onValueChange = { teamName = it.take(40) },
                            label = { Text("Team Name (optional for Solo)") },
                            singleLine = true,
                            colors = esportsTextFieldColors(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = placement,
                                onValueChange = { placement = it.filter { c -> c.isDigit() }.take(3) },
                                label = { Text("Rank #") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                colors = esportsTextFieldColors(),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = kills,
                                onValueChange = { kills = it.filter { c -> c.isDigit() }.take(2) },
                                label = { Text("Kills") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                colors = esportsTextFieldColors(),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = points,
                                onValueChange = { points = it.filter { c -> c.isDigit() }.take(5) },
                                label = { Text("Points") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                colors = esportsTextFieldColors(),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = prizeDemo,
                                onValueChange = { prizeDemo = it.filter { c -> c.isDigit() }.take(6) },
                                label = { Text("Prize (Demo Coins)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                colors = esportsTextFieldColors(),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Button(
                            onClick = {
                                onPublishResult(
                                    selectedTournament.id,
                                    selectedTournament.title,
                                    selectedPlayerUid,
                                    playerIgn,
                                    ffUid,
                                    teamName,
                                    placement.toLongOrNull() ?: 1L,
                                    kills.toLongOrNull() ?: 0L,
                                    points.toLongOrNull() ?: 0L,
                                    prizeDemo.toLongOrNull() ?: 0L
                                )
                            },
                            enabled = !isActionLoading && playerIgn.isNotBlank(),
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                        ) {
                            Text("SAVE RESULT & UPDATE LEADERBOARD", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminSupportTab(
    ticketsState: UiState<List<SupportTicket>>,
    isActionLoading: Boolean,
    onResolveTicket: (String, String) -> Unit
) {
    val tickets = (ticketsState as? UiState.Success)?.data ?: emptyList()
    var replyingTicket by remember { mutableStateOf<SupportTicket?>(null) }
    var replyText by remember { mutableStateOf("") }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (tickets.isEmpty()) {
            item {
                EmptyArenaCard(Icons.Filled.Shield, "Support Inbox Clear", "No player support tickets have been submitted.")
            }
        } else {
            items(tickets, key = { it.id }) { ticket ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CarbonSurface),
                    border = BorderStroke(1.dp, SubtleBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("${ticket.subject} (${ticket.playerIgn})", style = MaterialTheme.typography.titleSmall, color = PureWhite)
                        Text(ticket.message, style = MaterialTheme.typography.bodySmall, color = MutedSilver)
                        Text("Status: ${ticket.status}", style = MaterialTheme.typography.labelSmall, color = MoltenGold)
                        if (ticket.status != "RESOLVED") {
                            OutlinedButton(
                                onClick = {
                                    replyingTicket = ticket
                                    replyText = "Resolved by MK Esports Owner Admin."
                                },
                                enabled = !isActionLoading
                            ) {
                                Text("REPLY & RESOLVE", color = EmeraldWin)
                            }
                        } else if (ticket.adminReply.isNotBlank()) {
                            Text("Replied: ${ticket.adminReply}", style = MaterialTheme.typography.bodySmall, color = EmeraldWin)
                        }
                    }
                }
            }
        }
    }

    if (replyingTicket != null) {
        AlertDialog(
            onDismissRequest = { replyingTicket = null },
            containerColor = CarbonSurface,
            title = { Text("RESOLVE TICKET", color = MoltenGold) },
            text = {
                OutlinedTextField(
                    value = replyText,
                    onValueChange = { replyText = it.take(300) },
                    label = { Text("Admin Reply") },
                    colors = esportsTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val t = replyingTicket!!
                        replyingTicket = null
                        onResolveTicket(t.id, replyText)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldWin, contentColor = ObsidianBlack)
                ) {
                    Text("RESOLVE")
                }
            },
            dismissButton = {
                TextButton(onClick = { replyingTicket = null }) { Text("CANCEL", color = MutedSilver) }
            }
        )
    }
}

@Composable
private fun TournamentEditorDialog(
    existing: Tournament?,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        mode: TournamentMode,
        category: TournamentCategory,
        mapName: String,
        entryFeeDemo: Long,
        prizePoolDemo: Long,
        perKillRewardDemo: Long,
        maxSlots: Long,
        scheduledTimeText: String,
        status: TournamentStatus,
        rulesSummary: String
    ) -> Unit
) {
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var mode by remember { mutableStateOf(existing?.modeEnum ?: TournamentMode.SOLO) }
    var category by remember { mutableStateOf(existing?.categoryEnum ?: TournamentCategory.FREE) }
    var mapName by remember { mutableStateOf(existing?.mapName ?: "Bermuda") }
    var entryFee by remember { mutableStateOf((existing?.entryFeeDemo ?: 50L).toString()) }
    var prizePool by remember { mutableStateOf((existing?.prizePoolDemo ?: 1000L).toString()) }
    var perKill by remember { mutableStateOf((existing?.perKillRewardDemo ?: 20L).toString()) }
    var maxSlots by remember { mutableStateOf((existing?.maxSlots ?: mode.defaultMaxSlots).toString()) }
    var scheduleText by remember { mutableStateOf(existing?.scheduledTimeText ?: "Tonight • 9:00 PM IST") }
    var status by remember { mutableStateOf(existing?.statusEnum ?: TournamentStatus.UPCOMING) }
    var rules by remember { mutableStateOf(existing?.rulesSummary ?: "Mobile only. Fair play mandatory.") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CarbonSurface,
        title = {
            Text(
                text = if (existing == null) "CREATE TOURNAMENT" else "EDIT TOURNAMENT",
                style = MaterialTheme.typography.headlineMedium,
                color = MoltenGold
            )
        },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it.take(80) },
                        label = { Text("Tournament Title") },
                        singleLine = true,
                        colors = esportsTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(TournamentMode.entries) { m ->
                            FilterChip(
                                selected = mode == m,
                                onClick = {
                                    mode = m
                                    if (existing == null) maxSlots = m.defaultMaxSlots.toString()
                                },
                                label = { Text(m.label) }
                            )
                        }
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TournamentCategory.entries.forEach { c ->
                            FilterChip(
                                selected = category == c,
                                onClick = { category = c },
                                label = { Text(c.label) }
                            )
                        }
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = mapName,
                            onValueChange = { mapName = it.take(40) },
                            label = { Text("Map") },
                            singleLine = true,
                            colors = esportsTextFieldColors(),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = maxSlots,
                            onValueChange = { maxSlots = it.filter { c -> c.isDigit() }.take(3) },
                            label = { Text("Max Slots") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = esportsTextFieldColors(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (category == TournamentCategory.PAID_DEMO) {
                            OutlinedTextField(
                                value = entryFee,
                                onValueChange = { entryFee = it.filter { c -> c.isDigit() }.take(6) },
                                label = { Text("Entry DC") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                colors = esportsTextFieldColors(),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        OutlinedTextField(
                            value = prizePool,
                            onValueChange = { prizePool = it.filter { c -> c.isDigit() }.take(7) },
                            label = { Text("Prize Pool DC") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = esportsTextFieldColors(),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = perKill,
                            onValueChange = { perKill = it.filter { c -> c.isDigit() }.take(5) },
                            label = { Text("Per Kill DC") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = esportsTextFieldColors(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = scheduleText,
                        onValueChange = { scheduleText = it.take(60) },
                        label = { Text("Schedule Time") },
                        singleLine = true,
                        colors = esportsTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(TournamentStatus.entries) { s ->
                            FilterChip(
                                selected = status == s,
                                onClick = { status = s },
                                label = { Text(s.label) }
                            )
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = rules,
                        onValueChange = { rules = it.take(600) },
                        label = { Text("Rules & Notes") },
                        minLines = 2,
                        colors = esportsTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.trim().length >= 3) {
                        onSave(
                            title,
                            mode,
                            category,
                            mapName,
                            entryFee.toLongOrNull() ?: 50L,
                            prizePool.toLongOrNull() ?: 1000L,
                            perKill.toLongOrNull() ?: 20L,
                            (maxSlots.toLongOrNull() ?: 48L).coerceIn(2L, 100L),
                            scheduleText,
                            status,
                            rules
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
            ) {
                Text("PUBLISH")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL", color = MutedSilver) }
        }
    )
}
