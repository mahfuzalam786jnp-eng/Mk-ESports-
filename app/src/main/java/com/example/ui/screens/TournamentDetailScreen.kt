package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.MatchResult
import com.example.model.RegistrationStatus
import com.example.model.RoomCredential
import com.example.model.Tournament
import com.example.model.TournamentCategory
import com.example.model.TournamentMode
import com.example.model.TournamentRegistration
import com.example.model.TournamentSlot
import com.example.model.TournamentStatus
import com.example.model.UserProfile
import com.example.ui.components.MetricBadge
import com.example.ui.components.RegistrationStatusPill
import com.example.ui.components.StatusPill
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
fun TournamentDetailScreen(
    tournament: Tournament?,
    userProfile: UserProfile?,
    myRegistration: TournamentRegistration?,
    slots: List<TournamentSlot>,
    results: List<MatchResult>,
    roomCredential: RoomCredential?,
    roomCredentialError: String?,
    isOwnerAdmin: Boolean,
    isActionLoading: Boolean,
    onBack: () -> Unit,
    onLoadRoomCredentials: (String) -> Unit,
    onRegister: (
        freeFireUid: String,
        freeFireIgn: String,
        teamName: String,
        partnerInfo: String
    ) -> Unit,
    onCopiedMessage: (String) -> Unit
) {
    BackHandler(onBack = onBack)

    var showRegisterDialog by remember { mutableStateOf(false) }
    val clipboard = LocalClipboardManager.current

    LaunchedEffect(tournament?.id, tournament?.roomPublished, myRegistration?.status, isOwnerAdmin) {
        if (tournament != null && tournament.roomPublished &&
            (isOwnerAdmin || myRegistration?.statusEnum == RegistrationStatus.APPROVED)
        ) {
            onLoadRoomCredentials(tournament.id)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack)
    ) {
        if (tournament == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MoltenGold)
            }
            return
        }

        val artworkRes = when (tournament.modeEnum) {
            TournamentMode.SOLO -> R.drawable.img_mode_solo
            TournamentMode.DUO -> R.drawable.img_mode_duo
            TournamentMode.SQUAD -> R.drawable.img_mode_squad
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                ) {
                    Image(
                        painter = painterResource(id = artworkRes),
                        contentDescription = tournament.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        ObsidianBlack.copy(alpha = 0.55f),
                                        ObsidianBlack
                                    )
                                )
                            )
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("detail_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = PureWhite
                            )
                        }
                        TournamentStatusPill(status = tournament.statusEnum)
                    }

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatusPill(
                                text = tournament.modeEnum.label,
                                containerColor = GoldContainer,
                                contentColor = GoldBright
                            )
                            StatusPill(
                                text = if (tournament.categoryEnum == TournamentCategory.FREE) "FREE ENTRY" else "${tournament.entryFeeDemo} DEMO COINS",
                                containerColor = if (tournament.categoryEnum == TournamentCategory.FREE) EmeraldContainer else CrimsonContainer,
                                contentColor = PureWhite
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = tournament.title,
                            style = MaterialTheme.typography.headlineLarge,
                            color = PureWhite
                        )
                        Text(
                            text = "Map: ${tournament.mapName} • ${tournament.scheduledTimeText}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedSilver
                        )
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CarbonSurface),
                        border = BorderStroke(1.dp, SubtleBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                MetricBadge("PRIZE POOL", "${tournament.prizePoolDemo} DC", MoltenGold)
                                MetricBadge("PER KILL", "${tournament.perKillRewardDemo} DC", CrimsonBright)
                                MetricBadge(
                                    "SLOTS",
                                    "${tournament.filledSlots}/${tournament.maxSlots}",
                                    PureWhite
                                )
                            }
                            val progress = if (tournament.maxSlots > 0) {
                                (tournament.filledSlots.toFloat() / tournament.maxSlots.toFloat()).coerceIn(0f, 1f)
                            } else 0f
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth().height(7.dp),
                                color = MoltenGold,
                                trackColor = ElevatedSlate
                            )
                            if (tournament.rulesSummary.isNotBlank()) {
                                Text(
                                    text = tournament.rulesSummary,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MutedSilver
                                )
                            }
                        }
                    }

                    // Registration Status or Action Button
                    if (myRegistration != null) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = ElevatedSlate),
                            border = BorderStroke(1.dp, MoltenGold.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "YOUR REGISTRATION",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = PureWhite
                                    )
                                    RegistrationStatusPill(
                                        status = myRegistration.statusEnum,
                                        slotNumber = myRegistration.slotNumber
                                    )
                                }
                                Text(
                                    text = "FF UID: ${myRegistration.freeFireUid} • IGN: ${myRegistration.freeFireIgn}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MutedSilver
                                )
                                if (myRegistration.teamName.isNotBlank()) {
                                    Text(
                                        text = "Team: ${myRegistration.teamName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = GoldBright
                                    )
                                }
                            }
                        }
                    } else {
                        val canJoin = tournament.statusEnum in listOf(TournamentStatus.UPCOMING, TournamentStatus.LIVE) &&
                            tournament.filledSlots < tournament.maxSlots
                        Button(
                            onClick = { showRegisterDialog = true },
                            enabled = canJoin && !isActionLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("open_register_dialog_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CrimsonRed,
                                contentColor = PureWhite
                            )
                        ) {
                            Icon(Icons.Filled.SportsEsports, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when {
                                    tournament.filledSlots >= tournament.maxSlots -> "LOBBY FULL"
                                    tournament.statusEnum == TournamentStatus.COMPLETED -> "TOURNAMENT COMPLETED"
                                    tournament.categoryEnum == TournamentCategory.FREE -> "JOIN FREE TOURNAMENT"
                                    else -> "JOIN WITH ${tournament.entryFeeDemo} DEMO COINS"
                                },
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }

                    // Room ID & Password Section
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
                                Icon(
                                    imageVector = if (roomCredential != null) Icons.Filled.MeetingRoom else Icons.Filled.Lock,
                                    contentDescription = null,
                                    tint = if (roomCredential != null) EmeraldWin else MoltenGold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "CUSTOM ROOM CREDENTIALS",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = PureWhite
                                )
                            }

                            when {
                                !tournament.roomPublished -> {
                                    Text(
                                        text = "Room ID & Password will appear here once published by the MK Esports Admin (typically 15 mins before match start).",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MutedSilver
                                    )
                                }
                                roomCredential != null -> {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = EmeraldContainer,
                                        border = BorderStroke(1.dp, EmeraldWin.copy(alpha = 0.5f))
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text("ROOM ID", style = MaterialTheme.typography.labelSmall, color = MutedSilver)
                                                    Text(roomCredential.roomId, style = MaterialTheme.typography.titleLarge, color = PureWhite)
                                                }
                                                IconButton(onClick = {
                                                    clipboard.setText(AnnotatedString(roomCredential.roomId))
                                                    onCopiedMessage("Room ID copied!")
                                                }) {
                                                    Icon(Icons.Filled.ContentCopy, "Copy Room ID", tint = MoltenGold)
                                                }
                                            }
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text("PASSWORD", style = MaterialTheme.typography.labelSmall, color = MutedSilver)
                                                    Text(roomCredential.roomPassword, style = MaterialTheme.typography.titleLarge, color = GoldBright)
                                                }
                                                IconButton(onClick = {
                                                    clipboard.setText(AnnotatedString(roomCredential.roomPassword))
                                                    onCopiedMessage("Room Password copied!")
                                                }) {
                                                    Icon(Icons.Filled.ContentCopy, "Copy Password", tint = MoltenGold)
                                                }
                                            }
                                            if (roomCredential.instructions.isNotBlank()) {
                                                Text(
                                                    text = "${roomCredential.serverRegion}: ${roomCredential.instructions}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = PureWhite
                                                )
                                            }
                                        }
                                    }
                                }
                                myRegistration?.statusEnum == RegistrationStatus.APPROVED || isOwnerAdmin -> {
                                    OutlinedButton(onClick = { onLoadRoomCredentials(tournament.id) }) {
                                        Text("REVEAL ROOM DETAILS", color = MoltenGold)
                                    }
                                    if (roomCredentialError != null) {
                                        Text(roomCredentialError, style = MaterialTheme.typography.bodySmall, color = CrimsonBright)
                                    }
                                }
                                else -> {
                                    Text(
                                        text = "Room published! Credentials are restricted to Admin-approved players only.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = GoldBright
                                    )
                                }
                            }
                        }
                    }

                    if (tournament.winnerSummary.isNotBlank()) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = GoldContainer),
                            border = BorderStroke(1.dp, MoltenGold)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.EmojiEvents, null, tint = MoltenGold, modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("BOOYAH WINNER", style = MaterialTheme.typography.labelSmall, color = GoldBright)
                                    Text(tournament.winnerSummary, style = MaterialTheme.typography.titleMedium, color = PureWhite)
                                }
                            }
                        }
                    }
                }
            }

            if (results.isNotEmpty()) {
                item {
                    Text(
                        text = "OFFICIAL MATCH RESULTS",
                        style = MaterialTheme.typography.titleLarge,
                        color = MoltenGold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                items(results, key = { it.id }) { res ->
                    Card(
                        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CarbonSurface),
                        border = BorderStroke(1.dp, SubtleBorder)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "#${res.placement}",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = if (res.placement == 1L) MoltenGold else PureWhite
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(res.freeFireIgn, style = MaterialTheme.typography.titleSmall, color = PureWhite)
                                    Text("${res.kills} Kills • ${res.points} Pts", style = MaterialTheme.typography.bodySmall, color = MutedSilver)
                                }
                            }
                            Text(
                                text = "+${res.prizeDemo} DC",
                                style = MaterialTheme.typography.titleSmall,
                                color = EmeraldWin,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Group, null, tint = MoltenGold, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "REGISTERED SLOTS (${slots.size})",
                        style = MaterialTheme.typography.titleLarge,
                        color = PureWhite
                    )
                }
            }

            if (slots.isEmpty()) {
                item {
                    Text(
                        text = "No players registered yet. Be the first to claim Slot #1!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedSilver,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            } else {
                items(slots, key = { it.userId }) { slot ->
                    Card(
                        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CarbonSurface),
                        border = BorderStroke(1.dp, SubtleBorder)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (slot.teamName.isNotBlank()) "${slot.teamName} (${slot.freeFireIgn})" else slot.freeFireIgn,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = PureWhite
                                )
                                Text(
                                    text = slot.mode,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MutedSilver
                                )
                            }
                            RegistrationStatusPill(status = slot.statusEnum, slotNumber = slot.slotNumber)
                        }
                    }
                }
            }
        }
    }

    if (showRegisterDialog && tournament != null) {
        RegisterTournamentDialog(
            tournament = tournament,
            defaultUid = userProfile?.freeFireUid ?: "",
            defaultIgn = userProfile?.freeFireIgn ?: "",
            onDismiss = { showRegisterDialog = false },
            onConfirm = { uid, ign, team, partner ->
                showRegisterDialog = false
                onRegister(uid, ign, team, partner)
            }
        )
    }
}

@Composable
private fun RegisterTournamentDialog(
    tournament: Tournament,
    defaultUid: String,
    defaultIgn: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    var ffUid by remember { mutableStateOf(defaultUid) }
    var ffIgn by remember { mutableStateOf(defaultIgn) }
    var teamName by remember { mutableStateOf("") }
    var partnerInfo by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CarbonSurface,
        title = {
            Text(
                text = "JOIN ${tournament.modeEnum.label.uppercase()} LOBBY",
                style = MaterialTheme.typography.headlineMedium,
                color = MoltenGold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (tournament.categoryEnum == TournamentCategory.FREE) {
                        "Free Entry Tournament — Submit your Free Fire MAX UID for admin slot verification."
                    } else {
                        "Entry Fee: ${tournament.entryFeeDemo} Simulated MK Demo Coins (refunded automatically if rejected)."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedSilver
                )
                OutlinedTextField(
                    value = ffIgn,
                    onValueChange = { ffIgn = it.take(30) },
                    label = { Text("Free Fire MAX IGN") },
                    singleLine = true,
                    colors = esportsTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().testTag("reg_ign_input")
                )
                OutlinedTextField(
                    value = ffUid,
                    onValueChange = { ffUid = it.filter { c -> c.isDigit() }.take(16) },
                    label = { Text("Free Fire MAX UID (6-16 digits)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = esportsTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().testTag("reg_uid_input")
                )
                if (tournament.modeEnum != TournamentMode.SOLO) {
                    OutlinedTextField(
                        value = teamName,
                        onValueChange = { teamName = it.take(40) },
                        label = { Text(if (tournament.modeEnum == TournamentMode.DUO) "Duo Name" else "Squad Team Name") },
                        singleLine = true,
                        colors = esportsTextFieldColors(),
                        modifier = Modifier.fillMaxWidth().testTag("reg_team_input")
                    )
                    OutlinedTextField(
                        value = partnerInfo,
                        onValueChange = { partnerInfo = it.take(120) },
                        label = {
                            Text(
                                if (tournament.modeEnum == TournamentMode.DUO) "Partner IGN & FF UID"
                                else "Teammates FF UIDs (comma separated)"
                            )
                        },
                        colors = esportsTextFieldColors(),
                        modifier = Modifier.fillMaxWidth().testTag("reg_partner_input")
                    )
                }
                if (error != null) {
                    Text(error ?: "", style = MaterialTheme.typography.bodySmall, color = CrimsonBright)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        ffIgn.trim().length < 2 -> error = "Enter valid IGN."
                        ffUid.trim().length < 6 -> error = "Enter 6-16 digit Free Fire UID."
                        tournament.modeEnum != TournamentMode.SOLO && teamName.trim().length < 2 ->
                            error = "Enter a team name for ${tournament.modeEnum.label}."
                        else -> onConfirm(ffUid, ffIgn, teamName, partnerInfo)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                modifier = Modifier.testTag("confirm_registration_button")
            ) {
                Text("SUBMIT ENTRY")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = MutedSilver)
            }
        }
    )
}
