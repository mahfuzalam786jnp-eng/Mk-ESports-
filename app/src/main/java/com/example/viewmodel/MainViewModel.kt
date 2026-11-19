package com.example.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MkEsportsRepository
import com.example.data.extractFirestoreRootException
import com.example.model.LeaderboardEntry
import com.example.model.MatchResult
import com.example.model.OWNER_ADMIN_EMAIL
import com.example.model.RoomCredential
import com.example.model.SupportCategory
import com.example.model.SupportTicket
import com.example.model.Tournament
import com.example.model.TournamentCategory
import com.example.model.TournamentMode
import com.example.model.TournamentRegistration
import com.example.model.TournamentSlot
import com.example.model.TournamentStatus
import com.example.model.UiState
import com.example.model.UserProfile
import com.example.model.UserRegistration
import com.example.model.WalletTransaction
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(
    private val repository: MkEsportsRepository,
    val currentUserId: String,
    val currentUserEmail: String,
    val isEmailVerified: Boolean
) : ViewModel() {

    val isOwnerAdmin: Boolean =
        isEmailVerified && currentUserEmail.equals(OWNER_ADMIN_EMAIL, ignoreCase = true)

    private val _isActionLoading = MutableStateFlow(false)
    val isActionLoading: StateFlow<Boolean> = _isActionLoading.asStateFlow()

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val events = _events.asSharedFlow()

    val userProfileState: StateFlow<UiState<UserProfile?>> =
        repository.observeUserProfile(currentUserId)
            .map<UserProfile?, UiState<UserProfile?>> { UiState.Success(it) }
            .catch { error ->
                val root = extractFirestoreRootException(error)
                Log.w(TAG, "Error observing profile", root)
                emit(UiState.Error(root.message ?: "Failed to load player profile"))
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    val tournamentsState: StateFlow<UiState<List<Tournament>>> =
        repository.observeTournaments()
            .map<List<Tournament>, UiState<List<Tournament>>> { UiState.Success(it) }
            .catch { error ->
                val root = extractFirestoreRootException(error)
                Log.w(TAG, "Error observing tournaments", root)
                emit(UiState.Error(root.message ?: "Failed to load tournaments"))
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    val userRegistrationsState: StateFlow<UiState<List<UserRegistration>>> =
        repository.observeUserRegistrations(currentUserId)
            .map<List<UserRegistration>, UiState<List<UserRegistration>>> { UiState.Success(it) }
            .catch { error ->
                val root = extractFirestoreRootException(error)
                Log.w(TAG, "Error observing user registrations", root)
                emit(UiState.Error(root.message ?: "Failed to load tournament history"))
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    val walletTransactionsState: StateFlow<UiState<List<WalletTransaction>>> =
        repository.observeWalletTransactions(currentUserId)
            .map<List<WalletTransaction>, UiState<List<WalletTransaction>>> { UiState.Success(it) }
            .catch { error ->
                val root = extractFirestoreRootException(error)
                Log.w(TAG, "Error observing wallet transactions", root)
                emit(UiState.Error(root.message ?: "Failed to load transactions"))
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    val leaderboardState: StateFlow<UiState<List<LeaderboardEntry>>> =
        repository.observeLeaderboard()
            .map<List<LeaderboardEntry>, UiState<List<LeaderboardEntry>>> { UiState.Success(it) }
            .catch { error ->
                val root = extractFirestoreRootException(error)
                Log.w(TAG, "Error observing leaderboard", root)
                emit(UiState.Error(root.message ?: "Failed to load leaderboard"))
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    val mySupportTicketsState: StateFlow<UiState<List<SupportTicket>>> =
        repository.observeMySupportTickets(currentUserId)
            .map<List<SupportTicket>, UiState<List<SupportTicket>>> { UiState.Success(it) }
            .catch { error ->
                val root = extractFirestoreRootException(error)
                Log.w(TAG, "Error observing support tickets", root)
                emit(UiState.Error(root.message ?: "Failed to load support tickets"))
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    val allSupportTicketsState: StateFlow<UiState<List<SupportTicket>>> =
        if (isOwnerAdmin) {
            repository.observeAllSupportTickets()
                .map<List<SupportTicket>, UiState<List<SupportTicket>>> { UiState.Success(it) }
                .catch { error ->
                    val root = extractFirestoreRootException(error)
                    Log.w(TAG, "Error observing admin support tickets", root)
                    emit(UiState.Error(root.message ?: "Failed to load admin tickets"))
                }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)
        } else {
            MutableStateFlow<UiState<List<SupportTicket>>>(UiState.Success(emptyList())).asStateFlow()
        }

    // Selected Tournament Detail Streams
    private val _selectedTournamentId = MutableStateFlow<String?>(null)
    val selectedTournamentId: StateFlow<String?> = _selectedTournamentId.asStateFlow()

    val selectedTournament: StateFlow<Tournament?> = _selectedTournamentId
        .flatMapLatest { id ->
            if (id.isNullOrBlank()) flowOf(null)
            else repository.observeTournament(id).catch { extractFirestoreRootException(it); emit(null) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), null)

    val selectedMyRegistration: StateFlow<TournamentRegistration?> = _selectedTournamentId
        .flatMapLatest { id ->
            if (id.isNullOrBlank()) flowOf(null)
            else repository.observeMyTournamentRegistration(id, currentUserId)
                .catch { extractFirestoreRootException(it); emit(null) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), null)

    val selectedTournamentSlots: StateFlow<List<TournamentSlot>> = _selectedTournamentId
        .flatMapLatest { id ->
            if (id.isNullOrBlank()) flowOf(emptyList())
            else repository.observeTournamentSlots(id).catch { extractFirestoreRootException(it); emit(emptyList()) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), emptyList())

    val selectedTournamentResults: StateFlow<List<MatchResult>> = _selectedTournamentId
        .flatMapLatest { id ->
            if (id.isNullOrBlank()) flowOf(emptyList())
            else repository.observeTournamentResults(id).catch { extractFirestoreRootException(it); emit(emptyList()) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), emptyList())

    val selectedAdminRegistrations: StateFlow<List<TournamentRegistration>> = _selectedTournamentId
        .flatMapLatest { id ->
            if (id.isNullOrBlank() || !isOwnerAdmin) flowOf(emptyList())
            else repository.observeAllTournamentRegistrations(id)
                .catch { extractFirestoreRootException(it); emit(emptyList()) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), emptyList())

    private val _roomCredential = MutableStateFlow<RoomCredential?>(null)
    val roomCredential: StateFlow<RoomCredential?> = _roomCredential.asStateFlow()

    private val _roomCredentialError = MutableStateFlow<String?>(null)
    val roomCredentialError: StateFlow<String?> = _roomCredentialError.asStateFlow()

    fun selectTournament(tournamentId: String?) {
        _selectedTournamentId.value = tournamentId
        _roomCredential.value = null
        _roomCredentialError.value = null
    }

    fun loadRoomCredentials(tournamentId: String) {
        viewModelScope.launch {
            _roomCredentialError.value = null
            repository.fetchRoomCredentials(tournamentId)
                .onSuccess { _roomCredential.value = it }
                .onFailure { _roomCredentialError.value = it.localizedMessage ?: "Room credentials locked until approved." }
        }
    }

    fun completeOnboarding(
        displayName: String,
        phoneNumber: String,
        freeFireUid: String,
        freeFireIgn: String,
        referralCode: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isActionLoading.value = true
            repository.completePlayerOnboarding(
                displayName = displayName,
                email = currentUserEmail,
                phoneNumber = phoneNumber,
                freeFireUid = freeFireUid,
                freeFireIgn = freeFireIgn,
                optionalReferralCode = referralCode
            ).onSuccess {
                _events.emit("Welcome to MK Esports! 1,000 Demo Coins credited.")
                onSuccess()
            }.onFailure { err ->
                _events.emit(err.localizedMessage ?: "Failed to create profile.")
            }
            _isActionLoading.value = false
        }
    }

    fun updateProfile(
        displayName: String,
        phoneNumber: String,
        freeFireUid: String,
        freeFireIgn: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isActionLoading.value = true
            repository.updatePlayerProfile(displayName, phoneNumber, freeFireUid, freeFireIgn)
                .onSuccess {
                    _events.emit("Player profile updated.")
                    onSuccess()
                }
                .onFailure { _events.emit(it.localizedMessage ?: "Update failed.") }
            _isActionLoading.value = false
        }
    }

    fun claimDemoCoins() {
        viewModelScope.launch {
            _isActionLoading.value = true
            repository.claimSimulatedDemoCoins()
                .onSuccess { _events.emit("+500 MK Demo Coins added (simulated).") }
                .onFailure { _events.emit(it.localizedMessage ?: "Failed to add demo coins.") }
            _isActionLoading.value = false
        }
    }

    fun redeemReferral(code: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isActionLoading.value = true
            repository.redeemReferralCode(code)
                .onSuccess {
                    _events.emit("Referral redeemed! +250 Demo Coins credited.")
                    onSuccess()
                }
                .onFailure { _events.emit(it.localizedMessage ?: "Invalid referral code.") }
            _isActionLoading.value = false
        }
    }

    fun registerForTournament(
        tournament: Tournament,
        userProfile: UserProfile,
        freeFireUid: String,
        freeFireIgn: String,
        teamName: String,
        partnerInfo: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isActionLoading.value = true
            repository.registerForTournament(
                tournament = tournament,
                userProfile = userProfile,
                freeFireUid = freeFireUid,
                freeFireIgn = freeFireIgn,
                teamName = teamName,
                partnerInfo = partnerInfo
            ).onSuccess {
                _events.emit("Registered! Waiting for Admin slot approval.")
                onSuccess()
            }.onFailure {
                _events.emit(it.localizedMessage ?: "Registration failed.")
            }
            _isActionLoading.value = false
        }
    }

    fun submitSupportTicket(
        playerIgn: String,
        category: SupportCategory,
        subject: String,
        message: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isActionLoading.value = true
            repository.submitSupportTicket(playerIgn, category, subject, message)
                .onSuccess {
                    _events.emit("Support ticket submitted.")
                    onSuccess()
                }
                .onFailure { _events.emit(it.localizedMessage ?: "Could not submit ticket.") }
            _isActionLoading.value = false
        }
    }

    // --- Admin Actions ---

    fun saveTournament(
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
        rulesSummary: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isActionLoading.value = true
            repository.createOrUpdateTournament(
                existing, title, mode, category, mapName, entryFeeDemo,
                prizePoolDemo, perKillRewardDemo, maxSlots, scheduledTimeText, status, rulesSummary
            ).onSuccess {
                _events.emit(if (existing == null) "Tournament created!" else "Tournament updated!")
                onSuccess()
            }.onFailure {
                _events.emit(it.localizedMessage ?: "Admin action failed.")
            }
            _isActionLoading.value = false
        }
    }

    fun deleteTournament(tournamentId: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isActionLoading.value = true
            repository.deleteTournament(tournamentId)
                .onSuccess {
                    _events.emit("Tournament deleted.")
                    onSuccess()
                }
                .onFailure { _events.emit(it.localizedMessage ?: "Delete failed.") }
            _isActionLoading.value = false
        }
    }

    fun seedStarterTournaments() {
        viewModelScope.launch {
            _isActionLoading.value = true
            repository.seedStarterTournaments()
                .onSuccess { _events.emit("4 Free Fire MAX tournaments published!") }
                .onFailure { _events.emit(it.localizedMessage ?: "Seed failed.") }
            _isActionLoading.value = false
        }
    }

    fun approveRegistration(tournamentId: String, reg: TournamentRegistration, slotNumber: Long) {
        viewModelScope.launch {
            _isActionLoading.value = true
            repository.approveRegistration(tournamentId, reg, slotNumber)
                .onSuccess { _events.emit("Approved ${reg.freeFireIgn} at Slot #$slotNumber.") }
                .onFailure { _events.emit(it.localizedMessage ?: "Approval failed.") }
            _isActionLoading.value = false
        }
    }

    fun rejectRegistration(tournamentId: String, reg: TournamentRegistration) {
        viewModelScope.launch {
            _isActionLoading.value = true
            repository.rejectRegistration(tournamentId, reg)
                .onSuccess { _events.emit("Rejected ${reg.freeFireIgn} (demo entry refunded if paid).") }
                .onFailure { _events.emit(it.localizedMessage ?: "Rejection failed.") }
            _isActionLoading.value = false
        }
    }

    fun publishRoomDetails(
        tournamentId: String,
        roomId: String,
        roomPassword: String,
        serverRegion: String,
        instructions: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isActionLoading.value = true
            repository.publishRoomCredentials(tournamentId, roomId, roomPassword, serverRegion, instructions)
                .onSuccess {
                    _events.emit("Room ID & Password published to approved players!")
                    loadRoomCredentials(tournamentId)
                    onSuccess()
                }
                .onFailure { _events.emit(it.localizedMessage ?: "Failed to publish room.") }
            _isActionLoading.value = false
        }
    }

    fun publishMatchResult(
        tournamentId: String,
        tournamentTitle: String,
        playerUserId: String,
        freeFireIgn: String,
        freeFireUid: String,
        teamName: String,
        placement: Long,
        kills: Long,
        points: Long,
        prizeDemo: Long,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isActionLoading.value = true
            repository.publishMatchResult(
                tournamentId, tournamentTitle, playerUserId, freeFireIgn,
                freeFireUid, teamName, placement, kills, points, prizeDemo
            ).onSuccess {
                _events.emit("Match result saved & leaderboard updated!")
                onSuccess()
            }.onFailure {
                _events.emit(it.localizedMessage ?: "Result update failed.")
            }
            _isActionLoading.value = false
        }
    }

    fun resolveTicket(ticketId: String, reply: String) {
        viewModelScope.launch {
            _isActionLoading.value = true
            repository.resolveSupportTicket(ticketId, reply)
                .onSuccess { _events.emit("Support ticket resolved.") }
                .onFailure { _events.emit(it.localizedMessage ?: "Failed to resolve ticket.") }
            _isActionLoading.value = false
        }
    }

    private companion object {
        const val TAG = "MainVM"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
