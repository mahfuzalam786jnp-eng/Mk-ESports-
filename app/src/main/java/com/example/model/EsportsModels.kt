package com.example.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

const val OWNER_ADMIN_EMAIL = "mahfuzalam786jnp@gmail.com"
const val DEV_TEST_OTP_CODE = "123456"
const val INITIAL_DEMO_COINS = 1000L
const val REFERRAL_BONUS_DEMO_COINS = 250L
const val SIMULATED_TOPUP_AMOUNT = 500L

enum class TournamentMode(val label: String, val defaultMaxSlots: Long) {
    SOLO("Solo", 48L),
    DUO("Duo", 24L),
    SQUAD("Squad", 12L);

    companion object {
        fun fromRaw(raw: String): TournamentMode =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: SOLO
    }
}

enum class TournamentCategory(val label: String) {
    FREE("Free"),
    PAID_DEMO("Paid Demo");

    companion object {
        fun fromRaw(raw: String): TournamentCategory =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: FREE
    }
}

enum class TournamentStatus(val label: String) {
    UPCOMING("Upcoming"),
    LIVE("Live"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");

    companion object {
        fun fromRaw(raw: String): TournamentStatus =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: UPCOMING
    }
}

enum class RegistrationStatus(val label: String) {
    PENDING("Pending"),
    APPROVED("Approved"),
    REJECTED("Rejected");

    companion object {
        fun fromRaw(raw: String): RegistrationStatus =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: PENDING
    }
}

enum class TransactionType(val label: String) {
    WELCOME_BONUS("Welcome Bonus"),
    DEMO_TOPUP("Simulated Top-Up"),
    REFERRAL_BONUS("Referral Bonus"),
    ENTRY_FEE("Tournament Entry"),
    PRIZE_REWARD("Prize Reward"),
    REFUND("Entry Refund");

    companion object {
        fun fromRaw(raw: String): TransactionType =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: DEMO_TOPUP
    }
}

enum class SupportCategory(val label: String) {
    TOURNAMENT("Tournament"),
    ROOM_ISSUE("Room ID/Pass"),
    DEMO_WALLET("Demo Wallet"),
    ACCOUNT("Account"),
    OTHER("Other");

    companion object {
        fun fromRaw(raw: String): SupportCategory =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: OTHER
    }
}

@Keep
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val phoneVerified: Boolean = false,
    val freeFireUid: String = "",
    val freeFireIgn: String = "",
    val demoWalletBalance: Long = 0L,
    val referralCode: String = "",
    val referredByCode: String = "",
    val referralCount: Long = 0L,
    val matchesPlayed: Long = 0L,
    val totalKills: Long = 0L,
    val booyahs: Long = 0L,
    val totalWinningsDemo: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class WalletTransaction(
    @DocumentId val id: String = "",
    val userId: String = "",
    val type: String = TransactionType.DEMO_TOPUP.name,
    val amountDemo: Long = 0L,
    val credit: Boolean = true,
    val title: String = "",
    val description: String = "",
    val tournamentId: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class Tournament(
    @DocumentId val id: String = "",
    val title: String = "",
    val mode: String = TournamentMode.SOLO.name,
    val category: String = TournamentCategory.FREE.name,
    val mapName: String = "Bermuda",
    val entryFeeDemo: Long = 0L,
    val prizePoolDemo: Long = 0L,
    val perKillRewardDemo: Long = 0L,
    val maxSlots: Long = 48L,
    val filledSlots: Long = 0L,
    val scheduledTimeText: String = "",
    val status: String = TournamentStatus.UPCOMING.name,
    val rulesSummary: String = "",
    val roomPublished: Boolean = false,
    val roomPublishNote: String = "",
    val winnerSummary: String = "",
    val createdBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val modeEnum: TournamentMode get() = TournamentMode.fromRaw(mode)
    val categoryEnum: TournamentCategory get() = TournamentCategory.fromRaw(category)
    val statusEnum: TournamentStatus get() = TournamentStatus.fromRaw(status)
}

@Keep
@IgnoreExtraProperties
data class TournamentRegistration(
    @DocumentId val id: String = "",
    val userId: String = "",
    val tournamentId: String = "",
    val tournamentTitle: String = "",
    val playerName: String = "",
    val freeFireUid: String = "",
    val freeFireIgn: String = "",
    val mode: String = TournamentMode.SOLO.name,
    val teamName: String = "",
    val partnerInfo: String = "",
    val entryFeeDemo: Long = 0L,
    val status: String = RegistrationStatus.PENDING.name,
    val slotNumber: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val statusEnum: RegistrationStatus get() = RegistrationStatus.fromRaw(status)
}

@Keep
@IgnoreExtraProperties
data class UserRegistration(
    @DocumentId val id: String = "",
    val userId: String = "",
    val tournamentId: String = "",
    val tournamentTitle: String = "",
    val mode: String = TournamentMode.SOLO.name,
    val category: String = TournamentCategory.FREE.name,
    val entryFeeDemo: Long = 0L,
    val freeFireUid: String = "",
    val freeFireIgn: String = "",
    val teamName: String = "",
    val partnerInfo: String = "",
    val status: String = RegistrationStatus.PENDING.name,
    val slotNumber: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val statusEnum: RegistrationStatus get() = RegistrationStatus.fromRaw(status)
    val modeEnum: TournamentMode get() = TournamentMode.fromRaw(mode)
}

@Keep
@IgnoreExtraProperties
data class TournamentSlot(
    @DocumentId val id: String = "",
    val userId: String = "",
    val freeFireIgn: String = "",
    val teamName: String = "",
    val mode: String = TournamentMode.SOLO.name,
    val status: String = RegistrationStatus.PENDING.name,
    val slotNumber: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val statusEnum: RegistrationStatus get() = RegistrationStatus.fromRaw(status)
}

@Keep
@IgnoreExtraProperties
data class RoomCredential(
    @DocumentId val id: String = "",
    val tournamentId: String = "",
    val roomId: String = "",
    val roomPassword: String = "",
    val serverRegion: String = "",
    val instructions: String = "",
    val publishedBy: String = "",
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class MatchResult(
    @DocumentId val id: String = "",
    val tournamentId: String = "",
    val playerUserId: String = "",
    val freeFireIgn: String = "",
    val teamName: String = "",
    val placement: Long = 1L,
    val kills: Long = 0L,
    val points: Long = 0L,
    val prizeDemo: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class LeaderboardEntry(
    @DocumentId val id: String = "",
    val userId: String = "",
    val freeFireIgn: String = "",
    val freeFireUid: String = "",
    val totalKills: Long = 0L,
    val booyahs: Long = 0L,
    val matchesPlayed: Long = 0L,
    val points: Long = 0L,
    val demoWinnings: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class SupportTicket(
    @DocumentId val id: String = "",
    val userId: String = "",
    val playerIgn: String = "",
    val category: String = SupportCategory.OTHER.name,
    val subject: String = "",
    val message: String = "",
    val status: String = "OPEN",
    val adminReply: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}
