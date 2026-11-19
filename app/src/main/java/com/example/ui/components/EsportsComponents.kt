package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.RegistrationStatus
import com.example.model.Tournament
import com.example.model.TournamentCategory
import com.example.model.TournamentMode
import com.example.model.TournamentStatus
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberPending
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
fun MkBrandHeader(
    demoBalance: Long?,
    isOwnerAdmin: Boolean,
    onWalletClick: () -> Unit,
    onAdminClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.mk_esports_icon),
                contentDescription = "MK Esports Logo",
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, MoltenGold.copy(alpha = 0.6f), RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "MK ",
                        style = MaterialTheme.typography.titleLarge,
                        color = CrimsonRed
                    )
                    Text(
                        text = "ESPORTS",
                        style = MaterialTheme.typography.titleLarge,
                        color = MoltenGold
                    )
                }
                Text(
                    text = "FREE FIRE MAX ARENA",
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedSilver
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isOwnerAdmin) {
                Surface(
                    onClick = onAdminClick,
                    shape = RoundedCornerShape(10.dp),
                    color = CrimsonContainer,
                    border = BorderStroke(1.dp, CrimsonRed.copy(alpha = 0.7f)),
                    modifier = Modifier.testTag("header_admin_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Shield,
                            contentDescription = "Owner Admin",
                            tint = CrimsonBright,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "ADMIN",
                            style = MaterialTheme.typography.labelSmall,
                            color = PureWhite
                        )
                    }
                }
            }

            if (demoBalance != null) {
                Surface(
                    onClick = onWalletClick,
                    shape = RoundedCornerShape(10.dp),
                    color = GoldContainer,
                    border = BorderStroke(1.dp, MoltenGold.copy(alpha = 0.6f)),
                    modifier = Modifier.testTag("header_wallet_chip")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MonetizationOn,
                            contentDescription = "Demo Coins",
                            tint = MoltenGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$demoBalance",
                            style = MaterialTheme.typography.labelLarge,
                            color = GoldBright
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusPill(
    text: String,
    containerColor: Color,
    contentColor: Color,
    borderColor: Color = contentColor.copy(alpha = 0.4f),
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun TournamentStatusPill(status: TournamentStatus) {
    val (bg, fg) = when (status) {
        TournamentStatus.LIVE -> CrimsonContainer to CrimsonBright
        TournamentStatus.UPCOMING -> GoldContainer to GoldBright
        TournamentStatus.COMPLETED -> EmeraldContainer to EmeraldWin
        TournamentStatus.CANCELLED -> ElevatedSlate to MutedSilver
    }
    StatusPill(text = status.label, containerColor = bg, contentColor = fg)
}

@Composable
fun RegistrationStatusPill(status: RegistrationStatus, slotNumber: Long = 0L) {
    val (bg, fg, label) = when (status) {
        RegistrationStatus.APPROVED -> Triple(
            EmeraldContainer,
            EmeraldWin,
            if (slotNumber > 0L) "APPROVED • SLOT #$slotNumber" else "APPROVED"
        )
        RegistrationStatus.PENDING -> Triple(AmberContainer, AmberPending, "PENDING APPROVAL")
        RegistrationStatus.REJECTED -> Triple(CrimsonContainer, CrimsonBright, "REJECTED")
    }
    StatusPill(text = label, containerColor = bg, contentColor = fg)
}

@Composable
fun TournamentCard(
    tournament: Tournament,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val artworkRes = when (tournament.modeEnum) {
        TournamentMode.SOLO -> R.drawable.img_mode_solo
        TournamentMode.DUO -> R.drawable.img_mode_duo
        TournamentMode.SQUAD -> R.drawable.img_mode_squad
    }
    val progress = if (tournament.maxSlots > 0) {
        (tournament.filledSlots.toFloat() / tournament.maxSlots.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("tournament_card_${tournament.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CarbonSurface),
        border = BorderStroke(1.dp, SubtleBorder)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(132.dp)
            ) {
                Image(
                    painter = painterResource(id = artworkRes),
                    contentDescription = "${tournament.modeEnum.label} tournament banner",
                    modifier = Modifier.fillMaxWidth().height(132.dp),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(132.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    ObsidianBlack.copy(alpha = 0.25f),
                                    ObsidianBlack.copy(alpha = 0.92f)
                                )
                            )
                        )
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        StatusPill(
                            text = tournament.modeEnum.label,
                            containerColor = ObsidianBlack.copy(alpha = 0.85f),
                            contentColor = MoltenGold,
                            borderColor = MoltenGold.copy(alpha = 0.5f)
                        )
                        StatusPill(
                            text = if (tournament.categoryEnum == TournamentCategory.FREE) "FREE" else "${tournament.entryFeeDemo} DEMO",
                            containerColor = if (tournament.categoryEnum == TournamentCategory.FREE) EmeraldContainer else CrimsonContainer,
                            contentColor = if (tournament.categoryEnum == TournamentCategory.FREE) EmeraldWin else PureWhite
                        )
                    }
                    TournamentStatusPill(status = tournament.statusEnum)
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = tournament.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = PureWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${tournament.mapName} • ${tournament.scheduledTimeText}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedSilver
                    )
                }
            }

            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricBadge(label = "PRIZE POOL", value = "${tournament.prizePoolDemo} DC", accent = MoltenGold)
                    MetricBadge(label = "PER KILL", value = "${tournament.perKillRewardDemo} DC", accent = CrimsonBright)
                    MetricBadge(
                        label = "ENTRY",
                        value = if (tournament.categoryEnum == TournamentCategory.FREE) "FREE" else "${tournament.entryFeeDemo} DC",
                        accent = if (tournament.categoryEnum == TournamentCategory.FREE) EmeraldWin else PureWhite
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${tournament.filledSlots} / ${tournament.maxSlots} SLOTS FILLED",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedSilver
                    )
                    if (tournament.roomPublished) {
                        Text(
                            text = "ROOM READY",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldWin,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = if (progress >= 0.9f) CrimsonRed else MoltenGold,
                    trackColor = ElevatedSlate
                )

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CrimsonRed,
                        contentColor = PureWhite
                    )
                ) {
                    Text(
                        text = if (tournament.statusEnum == TournamentStatus.COMPLETED) "VIEW RESULTS" else "VIEW & JOIN ARENA",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
fun MetricBadge(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MutedSilver
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = accent,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun EmptyArenaCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CarbonSurface),
        border = BorderStroke(1.dp, SubtleBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(CrimsonContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MoltenGold,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = PureWhite
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MutedSilver
            )
        }
    }
}
