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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.SIMULATED_TOPUP_AMOUNT
import com.example.model.TransactionType
import com.example.model.UiState
import com.example.model.UserProfile
import com.example.model.WalletTransaction
import com.example.ui.components.EmptyArenaCard
import com.example.ui.components.MkBrandHeader
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CrimsonBright
import com.example.ui.theme.CrimsonContainer
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldWin
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.MoltenGold
import com.example.ui.theme.MutedSilver
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SubtleBorder
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun WalletScreen(
    userProfile: UserProfile?,
    transactionsState: UiState<List<WalletTransaction>>,
    isOwnerAdmin: Boolean,
    isActionLoading: Boolean,
    onClaimDemoCoins: () -> Unit,
    onAdminClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack)
    ) {
        MkBrandHeader(
            demoBalance = userProfile?.demoWalletBalance,
            isOwnerAdmin = isOwnerAdmin,
            onWalletClick = {},
            onAdminClick = onAdminClick
        )

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CarbonSurface),
                    border = BorderStroke(1.5.dp, MoltenGold.copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(GoldContainer, CarbonSurface, CrimsonContainer.copy(alpha = 0.5f))
                                )
                            )
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "MK DEMO WALLET",
                                style = MaterialTheme.typography.titleMedium,
                                color = GoldBright
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = ObsidianBlack.copy(alpha = 0.7f),
                                border = BorderStroke(1.dp, MoltenGold.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "SIMULATED ONLY",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MoltenGold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Text(
                            text = "${userProfile?.demoWalletBalance ?: 0} DC",
                            style = MaterialTheme.typography.displayLarge,
                            color = PureWhite
                        )
                        Text(
                            text = "Virtual MK Demo Coins for practice & Paid-Demo lobbies. Real-money deposits and withdrawals are disabled.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedSilver
                        )

                        Button(
                            onClick = onClaimDemoCoins,
                            enabled = !isActionLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("claim_demo_coins_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MoltenGold,
                                contentColor = ObsidianBlack
                            )
                        ) {
                            Icon(Icons.Filled.AddCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CLAIM +$SIMULATED_TOPUP_AMOUNT SIMULATED COINS",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }

            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CarbonSurface,
                    border = BorderStroke(1.dp, SubtleBorder)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Info, null, tint = MoltenGold, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Simulation Policy: MK Demo Coins carry zero cash value and cannot be deposited with or withdrawn for real currency.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedSilver
                        )
                    }
                }
            }

            item {
                Text(
                    text = "TRANSACTION HISTORY",
                    style = MaterialTheme.typography.headlineMedium,
                    color = PureWhite
                )
            }

            when (transactionsState) {
                is UiState.Loading -> {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(28.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MoltenGold)
                        }
                    }
                }
                is UiState.Error -> {
                    item {
                        EmptyArenaCard(Icons.Filled.AccountBalanceWallet, "Error loading ledger", transactionsState.message)
                    }
                }
                is UiState.Success -> {
                    if (transactionsState.data.isEmpty()) {
                        item {
                            EmptyArenaCard(
                                icon = Icons.Filled.AccountBalanceWallet,
                                title = "No transactions yet",
                                subtitle = "Claim simulated demo coins or enter a tournament to see your ledger."
                            )
                        }
                    } else {
                        items(transactionsState.data, key = { it.id }) { tx ->
                            val txType = TransactionType.fromRaw(tx.type)
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = CarbonSurface),
                                border = BorderStroke(1.dp, SubtleBorder)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(if (tx.credit) EmeraldContainer else CrimsonContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (tx.credit) Icons.Filled.ArrowDownward else Icons.Filled.ArrowUpward,
                                                contentDescription = null,
                                                tint = if (tx.credit) EmeraldWin else CrimsonBright,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = tx.title,
                                                style = MaterialTheme.typography.titleSmall,
                                                color = PureWhite
                                            )
                                            Text(
                                                text = "${txType.label} • ${tx.createdAt?.toDate()?.let { dateFormat.format(it) } ?: "Just now"}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MutedSilver
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${if (tx.credit) "+" else "-"}${tx.amountDemo} DC",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (tx.credit) EmeraldWin else CrimsonBright,
                                        fontWeight = FontWeight.Bold
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
