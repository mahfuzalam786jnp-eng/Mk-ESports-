package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Leaderboard
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.MkEsportsRepository
import com.example.model.UiState
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.MobileOtpOnboardingScreen
import com.example.ui.screens.MyMatchesScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SupportAndPolicyScreen
import com.example.ui.screens.TournamentDetailScreen
import com.example.ui.screens.WalletScreen
import com.example.ui.screens.signOutFromApp
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CrimsonContainer
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.MkEsportsTheme
import com.example.ui.theme.MoltenGold
import com.example.ui.theme.MutedSilver
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PureWhite
import com.example.viewmodel.MainViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

private const val ROUTE_HOME = "home"
private const val ROUTE_MATCHES = "matches"
private const val ROUTE_LEADERBOARD = "leaderboard"
private const val ROUTE_WALLET = "wallet"
private const val ROUTE_PROFILE = "profile"
private const val ROUTE_TOURNAMENT_DETAIL = "tournament_detail/{tournamentId}"
private const val ROUTE_SUPPORT_POLICY = "support_policy"
private const val ROUTE_ADMIN = "admin_dashboard"

private data class NavTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val bottomTabs = listOf(
    NavTab(ROUTE_HOME, "Arena", Icons.Filled.Home, Icons.Outlined.Home),
    NavTab(ROUTE_MATCHES, "Matches", Icons.Filled.SportsEsports, Icons.Outlined.SportsEsports),
    NavTab(ROUTE_LEADERBOARD, "Ranks", Icons.Filled.Leaderboard, Icons.Outlined.Leaderboard),
    NavTab(ROUTE_WALLET, "Wallet", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet),
    NavTab(ROUTE_PROFILE, "Profile", Icons.Filled.Person, Icons.Outlined.Person)
)

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MkEsportsTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation(auth: FirebaseAuth = Firebase.auth) {
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    var prefilledPhone by remember { mutableStateOf("+91") }

    val user = currentUser
    if (user == null) {
        AuthScreen(
            prefilledPhone = prefilledPhone,
            onPrefilledPhoneChange = { prefilledPhone = it },
            onAuthSuccess = {}
        )
    } else {
        key(user.uid) {
            AuthenticatedAppRoot(
                currentUser = user,
                prefilledPhone = prefilledPhone
            )
        }
    }
}

@Composable
private fun AuthenticatedAppRoot(
    currentUser: FirebaseUser,
    prefilledPhone: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val viewModel: MainViewModel = viewModel(
        key = currentUser.uid,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                MainViewModel(
                    repository = MkEsportsRepository(db),
                    currentUserId = currentUser.uid,
                    currentUserEmail = currentUser.email ?: "",
                    isEmailVerified = currentUser.isEmailVerified
                )
            }
        }
    )

    val profileState by viewModel.userProfileState.collectAsStateWithLifecycle()
    val isActionLoading by viewModel.isActionLoading.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    when (val pState = profileState) {
        is UiState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ObsidianBlack),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MoltenGold)
            }
        }
        is UiState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ObsidianBlack)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = pState.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = PureWhite
                )
            }
        }
        is UiState.Success -> {
            val profile = pState.data
            if (profile == null) {
                MobileOtpOnboardingScreen(
                    initialPhone = prefilledPhone,
                    initialDisplayName = currentUser.displayName ?: "",
                    userEmail = currentUser.email ?: "Player",
                    isSubmitting = isActionLoading,
                    onSubmitOnboarding = { name, phone, ffUid, ffIgn, refCode ->
                        viewModel.completeOnboarding(name, phone, ffUid, ffIgn, refCode)
                    },
                    onSignOut = {
                        signOutFromApp(credentialManager, scope)
                    }
                )
            } else {
                MkEsportsMainScaffold(
                    viewModel = viewModel,
                    userProfile = profile,
                    snackbarHostState = snackbarHostState,
                    onSignOut = {
                        signOutFromApp(credentialManager, scope)
                    }
                )
            }
        }
    }
}

@Composable
private fun MkEsportsMainScaffold(
    viewModel: MainViewModel,
    userProfile: com.example.model.UserProfile,
    snackbarHostState: SnackbarHostState,
    onSignOut: () -> Unit
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val tournamentsState by viewModel.tournamentsState.collectAsStateWithLifecycle()
    val userRegistrationsState by viewModel.userRegistrationsState.collectAsStateWithLifecycle()
    val walletTransactionsState by viewModel.walletTransactionsState.collectAsStateWithLifecycle()
    val leaderboardState by viewModel.leaderboardState.collectAsStateWithLifecycle()
    val mySupportTicketsState by viewModel.mySupportTicketsState.collectAsStateWithLifecycle()
    val allSupportTicketsState by viewModel.allSupportTicketsState.collectAsStateWithLifecycle()

    val selectedTournament by viewModel.selectedTournament.collectAsStateWithLifecycle()
    val selectedMyRegistration by viewModel.selectedMyRegistration.collectAsStateWithLifecycle()
    val selectedSlots by viewModel.selectedTournamentSlots.collectAsStateWithLifecycle()
    val selectedResults by viewModel.selectedTournamentResults.collectAsStateWithLifecycle()
    val selectedAdminRegistrations by viewModel.selectedAdminRegistrations.collectAsStateWithLifecycle()
    val roomCredential by viewModel.roomCredential.collectAsStateWithLifecycle()
    val roomCredentialError by viewModel.roomCredentialError.collectAsStateWithLifecycle()
    val isActionLoading by viewModel.isActionLoading.collectAsStateWithLifecycle()

    val showMainNav = currentRoute in bottomTabs.map { it.route }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(ObsidianBlack)) {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = ObsidianBlack,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                if (showMainNav && !useRail) {
                    NavigationBar(
                        containerColor = CarbonSurface,
                        contentColor = PureWhite,
                        windowInsets = WindowInsets.navigationBars
                    ) {
                        bottomTabs.forEach { tab ->
                            val selected = currentRoute == tab.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    if (currentRoute != tab.route) {
                                        navController.navigate(tab.route) {
                                            popUpTo(ROUTE_HOME) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label, style = MaterialTheme.typography.labelSmall) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MoltenGold,
                                    selectedTextColor = MoltenGold,
                                    indicatorColor = CrimsonContainer,
                                    unselectedIconColor = MutedSilver,
                                    unselectedTextColor = MutedSilver
                                )
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (showMainNav && useRail) {
                    NavigationRail(
                        containerColor = CarbonSurface,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        bottomTabs.forEach { tab ->
                            val selected = currentRoute == tab.route
                            NavigationRailItem(
                                selected = selected,
                                onClick = {
                                    if (currentRoute != tab.route) {
                                        navController.navigate(tab.route) {
                                            popUpTo(ROUTE_HOME) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = MoltenGold,
                                    selectedTextColor = MoltenGold,
                                    indicatorColor = CrimsonContainer,
                                    unselectedIconColor = MutedSilver,
                                    unselectedTextColor = MutedSilver
                                )
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .let { if (showMainNav) it.statusBarsPadding() else it }
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = ROUTE_HOME
                    ) {
                        composable(ROUTE_HOME) {
                            HomeScreen(
                                userProfile = userProfile,
                                tournamentsState = tournamentsState,
                                isOwnerAdmin = viewModel.isOwnerAdmin,
                                isActionLoading = isActionLoading,
                                onTournamentClick = { id ->
                                    viewModel.selectTournament(id)
                                    navController.navigate("tournament_detail/$id")
                                },
                                onWalletClick = {
                                    navController.navigate(ROUTE_WALLET) {
                                        popUpTo(ROUTE_HOME) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                onAdminClick = { navController.navigate(ROUTE_ADMIN) },
                                onSeedTournaments = { viewModel.seedStarterTournaments() }
                            )
                        }
                        composable(ROUTE_MATCHES) {
                            MyMatchesScreen(
                                userProfile = userProfile,
                                registrationsState = userRegistrationsState,
                                isOwnerAdmin = viewModel.isOwnerAdmin,
                                onTournamentClick = { id ->
                                    viewModel.selectTournament(id)
                                    navController.navigate("tournament_detail/$id")
                                },
                                onWalletClick = {
                                    navController.navigate(ROUTE_WALLET) {
                                        popUpTo(ROUTE_HOME) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                onAdminClick = { navController.navigate(ROUTE_ADMIN) }
                            )
                        }
                        composable(ROUTE_LEADERBOARD) {
                            LeaderboardScreen(
                                userProfile = userProfile,
                                leaderboardState = leaderboardState,
                                isOwnerAdmin = viewModel.isOwnerAdmin,
                                onWalletClick = {
                                    navController.navigate(ROUTE_WALLET) {
                                        popUpTo(ROUTE_HOME) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                onAdminClick = { navController.navigate(ROUTE_ADMIN) }
                            )
                        }
                        composable(ROUTE_WALLET) {
                            WalletScreen(
                                userProfile = userProfile,
                                transactionsState = walletTransactionsState,
                                isOwnerAdmin = viewModel.isOwnerAdmin,
                                isActionLoading = isActionLoading,
                                onClaimDemoCoins = { viewModel.claimDemoCoins() },
                                onAdminClick = { navController.navigate(ROUTE_ADMIN) }
                            )
                        }
                        composable(ROUTE_PROFILE) {
                            ProfileScreen(
                                userProfile = userProfile,
                                isOwnerAdmin = viewModel.isOwnerAdmin,
                                isActionLoading = isActionLoading,
                                onWalletClick = {
                                    navController.navigate(ROUTE_WALLET) {
                                        popUpTo(ROUTE_HOME) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                onAdminClick = { navController.navigate(ROUTE_ADMIN) },
                                onSupportPolicyClick = { navController.navigate(ROUTE_SUPPORT_POLICY) },
                                onUpdateProfile = { name, phone, ffUid, ffIgn ->
                                    viewModel.updateProfile(name, phone, ffUid, ffIgn)
                                },
                                onRedeemReferral = { code -> viewModel.redeemReferral(code) },
                                onCopiedMessage = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } },
                                onSignOut = onSignOut
                            )
                        }
                        composable(ROUTE_TOURNAMENT_DETAIL) { backStack ->
                            val tid = backStack.arguments?.getString("tournamentId")
                            LaunchedEffect(tid) {
                                if (tid != null) viewModel.selectTournament(tid)
                            }
                            TournamentDetailScreen(
                                tournament = selectedTournament,
                                userProfile = userProfile,
                                myRegistration = selectedMyRegistration,
                                slots = selectedSlots,
                                results = selectedResults,
                                roomCredential = roomCredential,
                                roomCredentialError = roomCredentialError,
                                isOwnerAdmin = viewModel.isOwnerAdmin,
                                isActionLoading = isActionLoading,
                                onBack = { navController.popBackStack() },
                                onLoadRoomCredentials = { id -> viewModel.loadRoomCredentials(id) },
                                onRegister = { ffUid, ffIgn, team, partner ->
                                    val t = selectedTournament ?: return@TournamentDetailScreen
                                    viewModel.registerForTournament(t, userProfile, ffUid, ffIgn, team, partner)
                                },
                                onCopiedMessage = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } }
                            )
                        }
                        composable(ROUTE_SUPPORT_POLICY) {
                            SupportAndPolicyScreen(
                                playerIgn = userProfile.freeFireIgn,
                                ticketsState = mySupportTicketsState,
                                isActionLoading = isActionLoading,
                                onBack = { navController.popBackStack() },
                                onSubmitTicket = { cat, sub, msg ->
                                    viewModel.submitSupportTicket(userProfile.freeFireIgn, cat, sub, msg)
                                }
                            )
                        }
                        composable(ROUTE_ADMIN) {
                            AdminDashboardScreen(
                                isOwnerAdmin = viewModel.isOwnerAdmin,
                                tournamentsState = tournamentsState,
                                selectedTournament = selectedTournament,
                                selectedRegistrations = selectedAdminRegistrations,
                                roomCredential = roomCredential,
                                allSupportTicketsState = allSupportTicketsState,
                                isActionLoading = isActionLoading,
                                onBack = { navController.popBackStack() },
                                onSelectTournament = { id -> viewModel.selectTournament(id) },
                                onLoadRoomCredentials = { id -> viewModel.loadRoomCredentials(id) },
                                onSeedTournaments = { viewModel.seedStarterTournaments() },
                                onSaveTournament = { existing, title, mode, cat, map, entry, prize, kill, slots, time, status, rules ->
                                    viewModel.saveTournament(existing, title, mode, cat, map, entry, prize, kill, slots, time, status, rules)
                                },
                                onDeleteTournament = { id -> viewModel.deleteTournament(id) },
                                onApproveRegistration = { tid, reg, slot ->
                                    viewModel.approveRegistration(tid, reg, slot)
                                },
                                onRejectRegistration = { tid, reg ->
                                    viewModel.rejectRegistration(tid, reg)
                                },
                                onPublishRoom = { tid, rId, rPass, region, notes ->
                                    viewModel.publishRoomDetails(tid, rId, rPass, region, notes)
                                },
                                onPublishResult = { tid, tTitle, pUid, ign, ffUid, team, rank, kills, pts, prize ->
                                    viewModel.publishMatchResult(tid, tTitle, pUid, ign, ffUid, team, rank, kills, pts, prize)
                                },
                                onResolveTicket = { ticketId, reply ->
                                    viewModel.resolveTicket(ticketId, reply)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
