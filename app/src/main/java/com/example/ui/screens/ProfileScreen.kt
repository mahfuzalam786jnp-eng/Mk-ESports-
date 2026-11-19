package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.model.UserProfile
import com.example.ui.components.MetricBadge
import com.example.ui.components.MkBrandHeader
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
fun ProfileScreen(
    userProfile: UserProfile?,
    isOwnerAdmin: Boolean,
    isActionLoading: Boolean,
    onWalletClick: () -> Unit,
    onAdminClick: () -> Unit,
    onSupportPolicyClick: () -> Unit,
    onUpdateProfile: (String, String, String, String) -> Unit,
    onRedeemReferral: (String) -> Unit,
    onCopiedMessage: (String) -> Unit,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var showEditDialog by remember { mutableStateOf(false) }
    var referralInput by remember { mutableStateOf("") }

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

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CarbonSurface),
                    border = BorderStroke(1.dp, SubtleBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(CircleShape)
                                        .background(CrimsonContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.Person, null, tint = MoltenGold, modifier = Modifier.size(28.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = userProfile?.freeFireIgn?.ifBlank { userProfile.displayName } ?: "Player",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = PureWhite
                                    )
                                    Text(
                                        text = "FF UID: ${userProfile?.freeFireUid ?: "-"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = GoldBright
                                    )
                                }
                            }
                            IconButton(
                                onClick = { showEditDialog = true },
                                modifier = Modifier.testTag("edit_profile_button")
                            ) {
                                Icon(Icons.Filled.Edit, "Edit Profile", tint = MoltenGold)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Mobile: ${userProfile?.phoneNumber ?: "-"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedSilver
                            )
                            if (userProfile?.phoneVerified == true) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = EmeraldContainer
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Filled.Verified, null, tint = EmeraldWin, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("OTP VERIFIED", style = MaterialTheme.typography.labelSmall, color = EmeraldWin)
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(ElevatedSlate)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MetricBadge("MATCHES", "${userProfile?.matchesPlayed ?: 0}", PureWhite)
                            MetricBadge("KILLS", "${userProfile?.totalKills ?: 0}", CrimsonBright)
                            MetricBadge("BOOYAHS", "${userProfile?.booyahs ?: 0}", MoltenGold)
                            MetricBadge("WON", "${userProfile?.totalWinningsDemo ?: 0}", EmeraldWin)
                        }
                    }
                }
            }

            // Invite Friends & Referral Code Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CarbonSurface),
                    border = BorderStroke(1.dp, MoltenGold.copy(alpha = 0.45f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CardGiftcard, null, tint = MoltenGold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("INVITE FRIENDS & REFERRALS", style = MaterialTheme.typography.titleMedium, color = PureWhite)
                        }
                        Text(
                            text = "Share your MK Esports referral code with your Free Fire squad. New players get +250 Demo Coins on redemption!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedSilver
                        )

                        val myCode = userProfile?.referralCode ?: "MKESPORTS"
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = GoldContainer,
                            border = BorderStroke(1.dp, MoltenGold.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("YOUR REFERRAL CODE", style = MaterialTheme.typography.labelSmall, color = MutedSilver)
                                    Text(myCode, style = MaterialTheme.typography.headlineMedium, color = GoldBright)
                                }
                                Row {
                                    IconButton(onClick = {
                                        clipboard.setText(AnnotatedString(myCode))
                                        onCopiedMessage("Referral code $myCode copied!")
                                    }) {
                                        Icon(Icons.Filled.ContentCopy, "Copy referral code", tint = MoltenGold)
                                    }
                                    IconButton(onClick = {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(
                                                Intent.EXTRA_TEXT,
                                                "Join me in Free Fire MAX tournaments on MK Esports! Use my referral code $myCode to unlock +250 Demo Coins."
                                            )
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Invite Friends to MK Esports"))
                                    }) {
                                        Icon(Icons.Filled.Share, "Share referral code", tint = PureWhite)
                                    }
                                }
                            }
                        }

                        if (userProfile != null && userProfile.referredByCode.isBlank()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = referralInput,
                                    onValueChange = { referralInput = it.uppercase().take(20) },
                                    label = { Text("Have a friend's code?") },
                                    singleLine = true,
                                    colors = esportsTextFieldColors(),
                                    modifier = Modifier.weight(1f).testTag("profile_referral_input")
                                )
                                Button(
                                    onClick = {
                                        if (referralInput.isNotBlank()) {
                                            onRedeemReferral(referralInput)
                                            referralInput = ""
                                        }
                                    },
                                    enabled = !isActionLoading && referralInput.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                                    modifier = Modifier.height(52.dp).testTag("redeem_referral_button")
                                ) {
                                    Text("REDEEM")
                                }
                            }
                        } else if (userProfile != null) {
                            Text(
                                text = "Referred by code: ${userProfile.referredByCode}",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldWin
                            )
                        }
                    }
                }
            }

            if (isOwnerAdmin) {
                item {
                    Button(
                        onClick = onAdminClick,
                        modifier = Modifier.fillMaxWidth().height(52.dp).testTag("profile_admin_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed, contentColor = PureWhite)
                    ) {
                        Icon(Icons.Filled.Shield, contentDescription = null, tint = MoltenGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("OPEN OWNER ADMIN DASHBOARD", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            item {
                OutlinedButton(
                    onClick = onSupportPolicyClick,
                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("support_policy_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, SubtleBorder)
                ) {
                    Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = null, tint = MoltenGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("CUSTOMER SUPPORT & PRIVACY POLICY", color = PureWhite, style = MaterialTheme.typography.labelLarge)
                }
            }

            item {
                OutlinedButton(
                    onClick = onSignOut,
                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("sign_out_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CrimsonRed.copy(alpha = 0.6f))
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = CrimsonBright)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SIGN OUT", color = CrimsonBright, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }

    if (showEditDialog && userProfile != null) {
        EditProfileDialog(
            profile = userProfile,
            onDismiss = { showEditDialog = false },
            onSave = { name, phone, uid, ign ->
                showEditDialog = false
                onUpdateProfile(name, phone, uid, ign)
            }
        )
    }
}

@Composable
private fun EditProfileDialog(
    profile: UserProfile,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(profile.displayName) }
    var phone by remember { mutableStateOf(profile.phoneNumber) }
    var ffUid by remember { mutableStateOf(profile.freeFireUid) }
    var ffIgn by remember { mutableStateOf(profile.freeFireIgn) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CarbonSurface,
        title = { Text("EDIT PLAYER PROFILE", style = MaterialTheme.typography.headlineMedium, color = MoltenGold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(50) },
                    label = { Text("Full Name") },
                    singleLine = true,
                    colors = esportsTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it.filter { c -> c.isDigit() || c == '+' }.take(16) },
                    label = { Text("Mobile Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    colors = esportsTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = ffIgn,
                    onValueChange = { ffIgn = it.take(30) },
                    label = { Text("Free Fire MAX IGN") },
                    singleLine = true,
                    colors = esportsTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = ffUid,
                    onValueChange = { ffUid = it.filter { c -> c.isDigit() }.take(16) },
                    label = { Text("Free Fire MAX UID") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = esportsTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (error != null) {
                    Text(error ?: "", style = MaterialTheme.typography.bodySmall, color = CrimsonBright)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        name.trim().isEmpty() -> error = "Name required."
                        phone.trim().length < 7 -> error = "Valid mobile number required."
                        ffIgn.trim().length < 2 -> error = "IGN must be 2+ characters."
                        ffUid.trim().length < 6 -> error = "UID must be 6-16 digits."
                        else -> onSave(name, phone, ffUid, ffIgn)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
            ) {
                Text("SAVE")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = MutedSilver)
            }
        }
    )
}
