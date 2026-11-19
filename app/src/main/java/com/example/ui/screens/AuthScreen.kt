package com.example.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.model.DEV_TEST_OTP_CODE
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
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@SuppressLint("DiscouragedApi")
private fun resolveWebClientId(context: Context): String? {
    val resId = context.resources.getIdentifier(
        "default_web_client_id",
        "string",
        context.packageName
    )
    if (resId == 0) return null
    return try {
        context.getString(resId).takeIf { it.isNotBlank() }
    } catch (_: Exception) {
        null
    }
}

fun attemptAutoSignIn(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onUnauthenticated: () -> Unit,
    scope: CoroutineScope
) {
    if (Firebase.auth.currentUser != null) {
        onAuthSuccess()
        return
    }
    val clientId = resolveWebClientId(context) ?: run {
        onUnauthenticated()
        return
    }
    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(true)
        .setServerClientId(clientId)
        .setAutoSelectEnabled(true)
        .build()
    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()
    scope.launch {
        try {
            val cred = credentialManager.getCredential(context, request).credential
            if (cred is CustomCredential && cred.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val token = GoogleIdTokenCredential.createFrom(cred.data).idToken
                Firebase.auth.signInWithCredential(GoogleAuthProvider.getCredential(token, null)).await()
                onAuthSuccess()
            } else {
                onUnauthenticated()
            }
        } catch (_: Exception) {
            onUnauthenticated()
        }
    }
}

fun onGoogleSignInClicked(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onAuthError: (String) -> Unit,
    scope: CoroutineScope,
    onAuthCancelled: () -> Unit = {}
) {
    val clientId = resolveWebClientId(context) ?: run {
        onAuthError("Google Sign-In configuration missing: default_web_client_id not found")
        return
    }
    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()
    scope.launch {
        try {
            val cred = credentialManager.getCredential(context as Activity, request).credential
            if (cred is CustomCredential && cred.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val token = GoogleIdTokenCredential.createFrom(cred.data).idToken
                Firebase.auth.signInWithCredential(GoogleAuthProvider.getCredential(token, null)).await()
                onAuthSuccess()
            } else {
                onAuthError("Unexpected credential type returned.")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("Auth", "Google Sign-In cancelled or dismissed: ${e.message}", e)
            onAuthCancelled()
        } catch (e: Exception) {
            Log.e("Auth", "Google Sign-In failed", e)
            onAuthError(e.localizedMessage ?: "Sign in failed. Please try again.")
        }
    }
}

fun signOutFromApp(
    credentialManager: CredentialManager,
    scope: CoroutineScope,
    onSignOutComplete: () -> Unit = {}
) {
    scope.launch {
        withContext(NonCancellable) {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.w("Auth", "Failed to clear credential state: ${e.message}")
            } finally {
                Firebase.auth.signOut()
                onSignOutComplete()
            }
        }
    }
}

@Composable
fun AuthScreen(
    prefilledPhone: String,
    onPrefilledPhoneChange: (String) -> Unit,
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var attemptedSilent by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!attemptedSilent) {
            attemptedSilent = true
            attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = onAuthSuccess,
                onUnauthenticated = {},
                scope = scope
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_mk_hero_banner),
                    contentDescription = "MK Esports Arena",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    ObsidianBlack.copy(alpha = 0.35f),
                                    ObsidianBlack
                                )
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.mk_esports_icon),
                        contentDescription = "MK Esports Crest",
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(2.dp, MoltenGold, RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row {
                        Text(
                            text = "MK ",
                            style = MaterialTheme.typography.displayLarge,
                            color = CrimsonRed
                        )
                        Text(
                            text = "ESPORTS",
                            style = MaterialTheme.typography.displayLarge,
                            color = MoltenGold
                        )
                    }
                    Text(
                        text = "FREE FIRE MAX TOURNAMENT ARENA",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedSilver
                    )
                }
            }

            Column(
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CarbonSurface),
                    border = BorderStroke(1.dp, SubtleBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "PLAYER LOGIN & SIGNUP",
                            style = MaterialTheme.typography.headlineMedium,
                            color = PureWhite
                        )
                        Text(
                            text = "Enter your mobile number for Test OTP verification, then verify with Google Sign-In to sync your tournaments, room credentials, and demo wallet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedSilver
                        )

                        OutlinedTextField(
                            value = prefilledPhone,
                            onValueChange = { onPrefilledPhoneChange(it.filter { ch -> ch.isDigit() || ch == '+' }.take(16)) },
                            label = { Text("Mobile Number (e.g. +919876543210)") },
                            leadingIcon = {
                                Icon(Icons.Filled.PhoneAndroid, contentDescription = null, tint = MoltenGold)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            colors = esportsTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_mobile_input")
                        )

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = GoldContainer,
                            border = BorderStroke(1.dp, MoltenGold.copy(alpha = 0.45f))
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Security,
                                    contentDescription = null,
                                    tint = MoltenGold,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Dev Test OTP Mode active ($DEV_TEST_OTP_CODE). Simulated Demo Coins only — no real-money deposits.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GoldBright
                                )
                            }
                        }

                        if (errorMessage != null) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = CrimsonContainer,
                                border = BorderStroke(1.dp, CrimsonRed)
                            ) {
                                Text(
                                    text = errorMessage ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PureWhite,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }

                        Button(
                            onClick = {
                                errorMessage = null
                                isLoading = true
                                onGoogleSignInClicked(
                                    context = context,
                                    credentialManager = credentialManager,
                                    onAuthSuccess = {
                                        isLoading = false
                                        onAuthSuccess()
                                    },
                                    onAuthError = { msg ->
                                        isLoading = false
                                        errorMessage = msg
                                    },
                                    scope = scope,
                                    onAuthCancelled = {
                                        isLoading = false
                                    }
                                )
                            },
                            enabled = !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("google_sign_in_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CrimsonRed,
                                contentColor = PureWhite
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = PureWhite,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.VerifiedUser,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "CONTINUE WITH GOOGLE & OTP",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = ElevatedSlate),
                    border = BorderStroke(1.dp, SubtleBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "ARENA HIGHLIGHTS",
                            style = MaterialTheme.typography.labelLarge,
                            color = MoltenGold
                        )
                        Text(
                            text = "• Free Fire MAX Solo, Duo & Squad custom rooms\n• Free & Paid-Demo categories with 1,000 free welcome coins\n• Instant Room ID & Password unlock for approved players",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedSilver
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MobileOtpOnboardingScreen(
    initialPhone: String,
    initialDisplayName: String,
    userEmail: String,
    isSubmitting: Boolean,
    onSubmitOnboarding: (
        displayName: String,
        phoneNumber: String,
        freeFireUid: String,
        freeFireIgn: String,
        referralCode: String
    ) -> Unit,
    onSignOut: () -> Unit
) {
    var displayName by remember { mutableStateOf(initialDisplayName.ifBlank { "" }) }
    var phoneNumber by remember { mutableStateOf(initialPhone.ifBlank { "+91" }) }
    var freeFireUid by remember { mutableStateOf("") }
    var freeFireIgn by remember { mutableStateOf("") }
    var referralCode by remember { mutableStateOf("") }

    var otpSent by remember { mutableStateOf(false) }
    var otpInput by remember { mutableStateOf("") }
    var otpVerified by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.widthIn(max = 520.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "COMPLETE PLAYER PROFILE",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MoltenGold
                        )
                        Text(
                            text = "Signed in as $userEmail",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedSilver
                        )
                    }
                    TextButton(onClick = onSignOut) {
                        Text("Switch Account", color = CrimsonBright)
                    }
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CarbonSurface),
                    border = BorderStroke(1.dp, SubtleBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "1. MOBILE NUMBER & TEST OTP",
                            style = MaterialTheme.typography.titleMedium,
                            color = PureWhite
                        )

                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = {
                                phoneNumber = it.filter { ch -> ch.isDigit() || ch == '+' }.take(16)
                                otpVerified = false
                            },
                            label = { Text("Mobile Number") },
                            leadingIcon = { Icon(Icons.Filled.PhoneAndroid, null, tint = MoltenGold) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            colors = esportsTextFieldColors(),
                            modifier = Modifier.fillMaxWidth().testTag("onboarding_phone_input")
                        )

                        if (!otpVerified) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        localError = null
                                        if (phoneNumber.trim().length < 7) {
                                            localError = "Enter a valid mobile number (at least 7 digits)."
                                        } else {
                                            otpSent = true
                                        }
                                    },
                                    border = BorderStroke(1.dp, MoltenGold),
                                    modifier = Modifier.testTag("send_test_otp_button")
                                ) {
                                    Text(
                                        text = if (otpSent) "RESEND TEST OTP" else "SEND TEST OTP",
                                        color = MoltenGold
                                    )
                                }
                                if (otpSent) {
                                    Text(
                                        text = "Test OTP: $DEV_TEST_OTP_CODE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = GoldBright
                                    )
                                }
                            }

                            if (otpSent) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = otpInput,
                                        onValueChange = { otpInput = it.filter { ch -> ch.isDigit() }.take(6) },
                                        label = { Text("Enter 6-digit OTP") },
                                        leadingIcon = { Icon(Icons.Filled.Lock, null, tint = CrimsonBright) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        colors = esportsTextFieldColors(),
                                        modifier = Modifier.weight(1f).testTag("otp_code_input")
                                    )
                                    Button(
                                        onClick = {
                                            if (otpInput == DEV_TEST_OTP_CODE) {
                                                otpVerified = true
                                                localError = null
                                            } else {
                                                localError = "Invalid OTP. Use Test OTP: $DEV_TEST_OTP_CODE"
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                                        modifier = Modifier.height(52.dp).testTag("verify_otp_button")
                                    ) {
                                        Text("VERIFY")
                                    }
                                }
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = EmeraldContainer,
                                border = BorderStroke(1.dp, EmeraldWin.copy(alpha = 0.6f))
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.CheckCircle, null, tint = EmeraldWin)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Mobile number verified (Dev Test OTP)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = EmeraldWin
                                    )
                                }
                            }
                        }
                    }
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CarbonSurface),
                    border = BorderStroke(1.dp, SubtleBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "2. FREE FIRE MAX IDENTITY",
                            style = MaterialTheme.typography.titleMedium,
                            color = PureWhite
                        )
                        OutlinedTextField(
                            value = displayName,
                            onValueChange = { displayName = it.take(50) },
                            label = { Text("Player Full Name") },
                            leadingIcon = { Icon(Icons.Filled.Person, null, tint = MoltenGold) },
                            singleLine = true,
                            colors = esportsTextFieldColors(),
                            modifier = Modifier.fillMaxWidth().testTag("onboarding_name_input")
                        )
                        OutlinedTextField(
                            value = freeFireIgn,
                            onValueChange = { freeFireIgn = it.take(30) },
                            label = { Text("Free Fire MAX In-Game Name (IGN)") },
                            leadingIcon = { Icon(Icons.Filled.SportsEsports, null, tint = CrimsonBright) },
                            singleLine = true,
                            colors = esportsTextFieldColors(),
                            modifier = Modifier.fillMaxWidth().testTag("onboarding_ign_input")
                        )
                        OutlinedTextField(
                            value = freeFireUid,
                            onValueChange = { freeFireUid = it.filter { ch -> ch.isDigit() }.take(16) },
                            label = { Text("Free Fire MAX UID (6-16 digits)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = esportsTextFieldColors(),
                            modifier = Modifier.fillMaxWidth().testTag("onboarding_uid_input")
                        )
                        OutlinedTextField(
                            value = referralCode,
                            onValueChange = { referralCode = it.uppercase().take(20) },
                            label = { Text("Invite / Referral Code (Optional +250 Demo Coins)") },
                            singleLine = true,
                            colors = esportsTextFieldColors(),
                            modifier = Modifier.fillMaxWidth().testTag("onboarding_referral_input")
                        )
                    }
                }

                if (localError != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = CrimsonContainer,
                        border = BorderStroke(1.dp, CrimsonRed)
                    ) {
                        Text(
                            text = localError ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = PureWhite,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Button(
                    onClick = {
                        localError = null
                        when {
                            !otpVerified -> localError = "Please verify your mobile number using Test OTP ($DEV_TEST_OTP_CODE) first."
                            freeFireIgn.trim().length < 2 -> localError = "Enter your Free Fire MAX IGN (at least 2 characters)."
                            freeFireUid.trim().length < 6 -> localError = "Enter a valid Free Fire MAX UID (6-16 digits)."
                            else -> onSubmitOnboarding(
                                displayName.ifBlank { freeFireIgn },
                                phoneNumber,
                                freeFireUid,
                                freeFireIgn,
                                referralCode
                            )
                        }
                    },
                    enabled = !isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("complete_signup_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CrimsonRed,
                        contentColor = PureWhite
                    )
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), color = PureWhite, strokeWidth = 2.dp)
                    } else {
                        Text(
                            text = "ENTER MK ESPORTS ARENA (+1,000 DEMO COINS)",
                            style = MaterialTheme.typography.labelLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun esportsTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MoltenGold,
    unfocusedBorderColor = SubtleBorder,
    focusedLabelColor = MoltenGold,
    unfocusedLabelColor = MutedSilver,
    focusedTextColor = PureWhite,
    unfocusedTextColor = PureWhite,
    cursorColor = CrimsonRed,
    focusedContainerColor = ElevatedSlate,
    unfocusedContainerColor = ElevatedSlate
)
