package com.example.data

import android.content.Context
import com.example.R
import com.example.model.INITIAL_DEMO_COINS
import com.example.model.LeaderboardEntry
import com.example.model.MatchResult
import com.example.model.REFERRAL_BONUS_DEMO_COINS
import com.example.model.RegistrationStatus
import com.example.model.RoomCredential
import com.example.model.SIMULATED_TOPUP_AMOUNT
import com.example.model.SupportCategory
import com.example.model.SupportTicket
import com.example.model.Tournament
import com.example.model.TournamentCategory
import com.example.model.TournamentMode
import com.example.model.TournamentRegistration
import com.example.model.TournamentSlot
import com.example.model.TournamentStatus
import com.example.model.TransactionType
import com.example.model.UserProfile
import com.example.model.UserRegistration
import com.example.model.WalletTransaction
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class MkEsportsRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("User must be signed in before accessing Firestore.")
    }

    private fun generateReferralCode(uid: String, ign: String): String {
        val cleanIgn = ign.uppercase().replace(Regex("[^A-Z0-9]"), "").take(4).ifEmpty { "MK" }
        val uidSuffix = uid.uppercase().replace(Regex("[^A-Z0-9]"), "").takeLast(4).ifEmpty { "2026" }
        return "MK$cleanIgn$uidSuffix".take(12)
    }

    // --- Observation Streams ---

    fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
        require(userId.isNotBlank()) { "User ID must not be blank" }
        val path = "users/$userId"
        emitAll(
            db.collection("users").document(userId)
                .snapshots()
                .map { snapshot ->
                    if (!snapshot.exists()) null
                    else snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    if (auth.currentUser?.uid == userId) {
                        handleFirestoreError(firestoreError ?: error, OperationType.GET, path)
                    }
                    throw error
                }
        )
    }

    fun observeTournaments(): Flow<List<Tournament>> = flow {
        val path = "tournaments"
        emitAll(
            db.collection(path)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(Tournament::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    if (auth.currentUser != null) {
                        handleFirestoreError(firestoreError ?: error, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeTournament(tournamentId: String): Flow<Tournament?> = flow {
        val path = "tournaments/$tournamentId"
        emitAll(
            db.collection("tournaments").document(tournamentId)
                .snapshots()
                .map { snapshot ->
                    if (!snapshot.exists()) null
                    else snapshot.toObject(Tournament::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    if (auth.currentUser != null) {
                        handleFirestoreError(firestoreError ?: error, OperationType.GET, path)
                    }
                    throw error
                }
        )
    }

    fun observeMyTournamentRegistration(tournamentId: String, userId: String): Flow<TournamentRegistration?> = flow {
        val path = "tournaments/$tournamentId/registrations/$userId"
        emitAll(
            db.collection("tournaments").document(tournamentId)
                .collection("registrations").document(userId)
                .snapshots()
                .map { snapshot ->
                    if (!snapshot.exists()) null
                    else snapshot.toObject(TournamentRegistration::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    if (auth.currentUser?.uid == userId) {
                        handleFirestoreError(firestoreError ?: error, OperationType.GET, path)
                    }
                    throw error
                }
        )
    }

    fun observeTournamentSlots(tournamentId: String): Flow<List<TournamentSlot>> = flow {
        val path = "tournaments/$tournamentId/slots"
        emitAll(
            db.collection("tournaments").document(tournamentId)
                .collection("slots")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(TournamentSlot::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedWith(compareBy<TournamentSlot> { if (it.slotNumber == 0L) Long.MAX_VALUE else it.slotNumber }
                            .thenBy { it.createdAt ?: Timestamp(0, 0) })
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    if (auth.currentUser != null) {
                        handleFirestoreError(firestoreError ?: error, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeTournamentResults(tournamentId: String): Flow<List<MatchResult>> = flow {
        val path = "tournaments/$tournamentId/results"
        emitAll(
            db.collection("tournaments").document(tournamentId)
                .collection("results")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(MatchResult::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.placement }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    if (auth.currentUser != null) {
                        handleFirestoreError(firestoreError ?: error, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeUserRegistrations(userId: String): Flow<List<UserRegistration>> = flow {
        val path = "users/$userId/registrations"
        emitAll(
            db.collection("users").document(userId)
                .collection("registrations")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(UserRegistration::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    if (auth.currentUser?.uid == userId) {
                        handleFirestoreError(firestoreError ?: error, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeWalletTransactions(userId: String): Flow<List<WalletTransaction>> = flow {
        val path = "users/$userId/transactions"
        emitAll(
            db.collection("users").document(userId)
                .collection("transactions")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(WalletTransaction::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    if (auth.currentUser?.uid == userId) {
                        handleFirestoreError(firestoreError ?: error, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeLeaderboard(): Flow<List<LeaderboardEntry>> = flow {
        val path = "leaderboard"
        emitAll(
            db.collection(path)
                .orderBy("points", Query.Direction.DESCENDING)
                .limit(50)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(LeaderboardEntry::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    if (auth.currentUser != null) {
                        handleFirestoreError(firestoreError ?: error, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeMySupportTickets(userId: String): Flow<List<SupportTicket>> = flow {
        val path = "supportTickets"
        emitAll(
            db.collection(path)
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(SupportTicket::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    if (auth.currentUser?.uid == userId) {
                        handleFirestoreError(firestoreError ?: error, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    // Admin Streams
    fun observeAllTournamentRegistrations(tournamentId: String): Flow<List<TournamentRegistration>> = flow {
        val path = "tournaments/$tournamentId/registrations"
        emitAll(
            db.collection("tournaments").document(tournamentId)
                .collection("registrations")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(TournamentRegistration::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    if (auth.currentUser != null) {
                        handleFirestoreError(firestoreError ?: error, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeAllSupportTickets(): Flow<List<SupportTicket>> = flow {
        val path = "supportTickets"
        emitAll(
            db.collection(path)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(SupportTicket::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    if (auth.currentUser != null) {
                        handleFirestoreError(firestoreError ?: error, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    // --- One-Shot Reads ---

    suspend fun fetchRoomCredentials(tournamentId: String): Result<RoomCredential?> {
        val path = "tournaments/$tournamentId/roomCredentials/secret"
        return try {
            requireUserId()
            val snap = db.collection("tournaments").document(tournamentId)
                .collection("roomCredentials").document("secret")
                .get()
                .await()
            val credential = if (snap.exists()) {
                snap.toObject(RoomCredential::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            } else null
            Result.success(credential)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, path)
            Result.failure(e)
        }
    }

    // --- Player Onboarding & Profile Mutations ---

    suspend fun completePlayerOnboarding(
        displayName: String,
        email: String,
        phoneNumber: String,
        freeFireUid: String,
        freeFireIgn: String,
        optionalReferralCode: String
    ): Result<Unit> {
        var path = "users"
        return try {
            val uid = requireUserId()
            val cleanName = displayName.trim().ifBlank { freeFireIgn.trim() }.take(50)
            val cleanPhone = phoneNumber.trim()
            val cleanFfUid = freeFireUid.trim()
            val cleanIgn = freeFireIgn.trim()
            require(cleanPhone.length in 7..20) { "Enter a valid mobile number (7-20 characters)." }
            require(cleanFfUid.length in 6..16 && cleanFfUid.all { it.isDigit() }) {
                "Free Fire MAX UID must be 6-16 digits."
            }
            require(cleanIgn.length in 2..30) { "In-Game Name must be 2-30 characters." }

            val generatedCode = generateReferralCode(uid, cleanIgn)
            val batch = db.batch()

            val userRef = db.collection("users").document(uid)
            path = userRef.path
            batch.set(
                userRef,
                mapOf(
                    "userId" to uid,
                    "displayName" to cleanName,
                    "email" to email.trim().take(120),
                    "phoneNumber" to cleanPhone,
                    "phoneVerified" to true,
                    "freeFireUid" to cleanFfUid,
                    "freeFireIgn" to cleanIgn,
                    "demoWalletBalance" to INITIAL_DEMO_COINS,
                    "referralCode" to generatedCode,
                    "referredByCode" to "",
                    "referralCount" to 0L,
                    "matchesPlayed" to 0L,
                    "totalKills" to 0L,
                    "booyahs" to 0L,
                    "totalWinningsDemo" to 0L,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )

            val welcomeTxRef = userRef.collection("transactions").document()
            batch.set(
                welcomeTxRef,
                mapOf(
                    "userId" to uid,
                    "type" to TransactionType.WELCOME_BONUS.name,
                    "amountDemo" to INITIAL_DEMO_COINS,
                    "credit" to true,
                    "title" to "MK Esports Welcome Bonus",
                    "description" to "Simulated demo coins to join Free & Paid-Demo tournaments.",
                    "tournamentId" to "",
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )

            val refCodeRef = db.collection("referralCodes").document(generatedCode)
            batch.set(
                refCodeRef,
                mapOf(
                    "code" to generatedCode,
                    "ownerUid" to uid,
                    "ownerIgn" to cleanIgn,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )

            val lbRef = db.collection("leaderboard").document(uid)
            batch.set(
                lbRef,
                mapOf(
                    "userId" to uid,
                    "freeFireIgn" to cleanIgn,
                    "freeFireUid" to cleanFfUid,
                    "totalKills" to 0L,
                    "booyahs" to 0L,
                    "matchesPlayed" to 0L,
                    "points" to 0L,
                    "demoWinnings" to 0L,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )

            batch.commit().await()

            val trimmedInvite = optionalReferralCode.trim().uppercase()
            if (trimmedInvite.isNotEmpty() && trimmedInvite != generatedCode) {
                redeemReferralCode(trimmedInvite)
            }

            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun updatePlayerProfile(
        displayName: String,
        phoneNumber: String,
        freeFireUid: String,
        freeFireIgn: String
    ): Result<Unit> {
        var path = "users"
        return try {
            val uid = requireUserId()
            val cleanName = displayName.trim()
            val cleanPhone = phoneNumber.trim()
            val cleanFfUid = freeFireUid.trim()
            val cleanIgn = freeFireIgn.trim()
            require(cleanName.length in 1..50) { "Display name must be 1-50 characters." }
            require(cleanPhone.length in 7..20) { "Enter a valid mobile number." }
            require(cleanFfUid.length in 6..16 && cleanFfUid.all { it.isDigit() }) {
                "Free Fire MAX UID must be 6-16 digits."
            }
            require(cleanIgn.length in 2..30) { "In-Game Name must be 2-30 characters." }

            val userRef = db.collection("users").document(uid)
            path = userRef.path
            val batch = db.batch()
            batch.update(
                userRef,
                mapOf(
                    "displayName" to cleanName,
                    "phoneNumber" to cleanPhone,
                    "phoneVerified" to true,
                    "freeFireUid" to cleanFfUid,
                    "freeFireIgn" to cleanIgn,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.update(
                db.collection("leaderboard").document(uid),
                mapOf(
                    "freeFireIgn" to cleanIgn,
                    "freeFireUid" to cleanFfUid,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    // --- Demo Wallet & Referrals ---

    suspend fun claimSimulatedDemoCoins(): Result<Unit> {
        var path = "users"
        return try {
            val uid = requireUserId()
            val userRef = db.collection("users").document(uid)
            path = userRef.path
            val txRef = userRef.collection("transactions").document()
            val batch = db.batch()
            batch.update(
                userRef,
                mapOf(
                    "demoWalletBalance" to FieldValue.increment(SIMULATED_TOPUP_AMOUNT),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.set(
                txRef,
                mapOf(
                    "userId" to uid,
                    "type" to TransactionType.DEMO_TOPUP.name,
                    "amountDemo" to SIMULATED_TOPUP_AMOUNT,
                    "credit" to true,
                    "title" to "Simulated Demo Coin Faucet",
                    "description" to "Virtual practice credits added (no real money).",
                    "tournamentId" to "",
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun redeemReferralCode(rawCode: String): Result<Unit> {
        var path = "referralCodes"
        return try {
            val uid = requireUserId()
            val code = rawCode.trim().uppercase()
            require(code.length in 4..20 && code.matches(Regex("^[A-Za-z0-9_\\-]+$"))) {
                "Enter a valid referral code (4-20 alphanumeric characters)."
            }
            val codeSnap = db.collection("referralCodes").document(code).get().await()
            path = "referralCodes/$code"
            if (!codeSnap.exists()) {
                return Result.failure(IllegalArgumentException("Referral code '$code' not found."))
            }
            val ownerUid = codeSnap.getString("ownerUid") ?: ""
            if (ownerUid == uid) {
                return Result.failure(IllegalArgumentException("You cannot redeem your own referral code."))
            }

            val userRef = db.collection("users").document(uid)
            val userSnap = userRef.get().await()
            val existingRef = userSnap.getString("referredByCode") ?: ""
            if (existingRef.isNotBlank()) {
                return Result.failure(IllegalStateException("Referral bonus already claimed ($existingRef)."))
            }

            val txRef = userRef.collection("transactions").document()
            val batch = db.batch()
            batch.update(
                userRef,
                mapOf(
                    "referredByCode" to code,
                    "demoWalletBalance" to FieldValue.increment(REFERRAL_BONUS_DEMO_COINS),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.set(
                txRef,
                mapOf(
                    "userId" to uid,
                    "type" to TransactionType.REFERRAL_BONUS.name,
                    "amountDemo" to REFERRAL_BONUS_DEMO_COINS,
                    "credit" to true,
                    "title" to "Referral Bonus ($code)",
                    "description" to "Demo coin bonus for joining via friend invite.",
                    "tournamentId" to "",
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    // --- Tournament Registration ---

    suspend fun registerForTournament(
        tournament: Tournament,
        userProfile: UserProfile,
        freeFireUid: String,
        freeFireIgn: String,
        teamName: String,
        partnerInfo: String
    ): Result<Unit> {
        var path = "tournaments/${tournament.id}/registrations"
        return try {
            val uid = requireUserId()
            val cleanFfUid = freeFireUid.trim()
            val cleanIgn = freeFireIgn.trim()
            val cleanTeam = teamName.trim().take(40)
            val cleanPartner = partnerInfo.trim().take(120)

            require(tournament.id.isNotBlank()) { "Tournament ID missing." }
            require(cleanFfUid.length in 6..16 && cleanFfUid.all { it.isDigit() }) {
                "Free Fire MAX UID must be 6-16 digits."
            }
            require(cleanIgn.length in 2..30) { "In-Game Name must be 2-30 characters." }
            if (tournament.statusEnum !in listOf(TournamentStatus.UPCOMING, TournamentStatus.LIVE)) {
                return Result.failure(IllegalStateException("Registrations are closed for this tournament."))
            }
            if (tournament.filledSlots >= tournament.maxSlots) {
                return Result.failure(IllegalStateException("All slots are full."))
            }
            val entryFee = if (tournament.categoryEnum == TournamentCategory.FREE) 0L else tournament.entryFeeDemo
            if (entryFee > userProfile.demoWalletBalance) {
                return Result.failure(IllegalStateException("Insufficient MK Demo Coins. Claim free simulated coins in Wallet."))
            }

            val existingReg = db.collection("tournaments").document(tournament.id)
                .collection("registrations").document(uid)
                .get().await()
            if (existingReg.exists()) {
                return Result.failure(IllegalStateException("You are already registered for this tournament."))
            }

            val tourneyRef = db.collection("tournaments").document(tournament.id)
            val tourneyRegRef = tourneyRef.collection("registrations").document(uid)
            val slotRef = tourneyRef.collection("slots").document(uid)
            val userRef = db.collection("users").document(uid)
            val userRegRef = userRef.collection("registrations").document(tournament.id)
            path = tourneyRegRef.path

            val batch = db.batch()
            batch.set(
                tourneyRegRef,
                mapOf(
                    "userId" to uid,
                    "tournamentId" to tournament.id,
                    "tournamentTitle" to tournament.title.take(80),
                    "playerName" to userProfile.displayName.ifBlank { cleanIgn }.take(50),
                    "freeFireUid" to cleanFfUid,
                    "freeFireIgn" to cleanIgn,
                    "mode" to tournament.mode,
                    "teamName" to cleanTeam,
                    "partnerInfo" to cleanPartner,
                    "entryFeeDemo" to entryFee,
                    "status" to RegistrationStatus.PENDING.name,
                    "slotNumber" to 0L,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.set(
                userRegRef,
                mapOf(
                    "userId" to uid,
                    "tournamentId" to tournament.id,
                    "tournamentTitle" to tournament.title.take(80),
                    "mode" to tournament.mode,
                    "category" to tournament.category,
                    "entryFeeDemo" to entryFee,
                    "freeFireUid" to cleanFfUid,
                    "freeFireIgn" to cleanIgn,
                    "teamName" to cleanTeam,
                    "partnerInfo" to cleanPartner,
                    "status" to RegistrationStatus.PENDING.name,
                    "slotNumber" to 0L,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.set(
                slotRef,
                mapOf(
                    "userId" to uid,
                    "freeFireIgn" to cleanIgn,
                    "teamName" to cleanTeam,
                    "mode" to tournament.mode,
                    "status" to RegistrationStatus.PENDING.name,
                    "slotNumber" to 0L,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.update(
                tourneyRef,
                mapOf(
                    "filledSlots" to FieldValue.increment(1L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )

            if (entryFee > 0L) {
                batch.update(
                    userRef,
                    mapOf(
                        "demoWalletBalance" to FieldValue.increment(-entryFee),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                val txRef = userRef.collection("transactions").document()
                batch.set(
                    txRef,
                    mapOf(
                        "userId" to uid,
                        "type" to TransactionType.ENTRY_FEE.name,
                        "amountDemo" to entryFee,
                        "credit" to false,
                        "title" to "Entry: ${tournament.title.take(55)}",
                        "description" to "Simulated demo entry fee (${tournament.mode}).",
                        "tournamentId" to tournament.id,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
            }

            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    // --- Support Tickets ---

    suspend fun submitSupportTicket(
        playerIgn: String,
        category: SupportCategory,
        subject: String,
        message: String
    ): Result<Unit> {
        var path = "supportTickets"
        return try {
            val uid = requireUserId()
            val cleanSubject = subject.trim()
            val cleanMessage = message.trim()
            require(cleanSubject.length in 3..80) { "Subject must be 3-80 characters." }
            require(cleanMessage.length in 5..500) { "Message must be 5-500 characters." }

            val docRef = db.collection("supportTickets").document()
            path = docRef.path
            docRef.set(
                mapOf(
                    "userId" to uid,
                    "playerIgn" to playerIgn.trim().ifBlank { "Player" }.take(30),
                    "category" to category.name,
                    "subject" to cleanSubject,
                    "message" to cleanMessage,
                    "status" to "OPEN",
                    "adminReply" to "",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    // --- Owner Admin Operations (Protected by Firestore Security Rules isAdmin()) ---

    suspend fun createOrUpdateTournament(
        existingTournament: Tournament?,
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
    ): Result<Unit> {
        var path = "tournaments"
        return try {
            val uid = requireUserId()
            val cleanTitle = title.trim()
            val cleanMap = mapName.trim()
            val cleanTime = scheduledTimeText.trim()
            require(cleanTitle.length in 3..80) { "Title must be 3-80 characters." }
            require(cleanMap.length in 2..40) { "Map name must be 2-40 characters." }
            require(cleanTime.length in 3..60) { "Schedule time must be 3-60 characters." }
            require(maxSlots in 2L..100L) { "Max slots must be between 2 and 100." }
            val actualEntry = if (category == TournamentCategory.FREE) 0L else entryFeeDemo.coerceAtLeast(1L)

            if (existingTournament == null) {
                val docRef = db.collection("tournaments").document()
                path = docRef.path
                docRef.set(
                    mapOf(
                        "title" to cleanTitle,
                        "mode" to mode.name,
                        "category" to category.name,
                        "mapName" to cleanMap,
                        "entryFeeDemo" to actualEntry,
                        "prizePoolDemo" to prizePoolDemo.coerceAtLeast(0L),
                        "perKillRewardDemo" to perKillRewardDemo.coerceAtLeast(0L),
                        "maxSlots" to maxSlots,
                        "filledSlots" to 0L,
                        "scheduledTimeText" to cleanTime,
                        "status" to status.name,
                        "rulesSummary" to rulesSummary.trim().take(600),
                        "roomPublished" to false,
                        "roomPublishNote" to "",
                        "winnerSummary" to "",
                        "createdBy" to uid,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                val docRef = db.collection("tournaments").document(existingTournament.id)
                path = docRef.path
                docRef.update(
                    mapOf(
                        "title" to cleanTitle,
                        "mode" to mode.name,
                        "category" to category.name,
                        "mapName" to cleanMap,
                        "entryFeeDemo" to actualEntry,
                        "prizePoolDemo" to prizePoolDemo.coerceAtLeast(0L),
                        "perKillRewardDemo" to perKillRewardDemo.coerceAtLeast(0L),
                        "maxSlots" to maxSlots,
                        "scheduledTimeText" to cleanTime,
                        "status" to status.name,
                        "rulesSummary" to rulesSummary.trim().take(600),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteTournament(tournamentId: String): Result<Unit> {
        val path = "tournaments/$tournamentId"
        return try {
            requireUserId()
            db.collection("tournaments").document(tournamentId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun seedStarterTournaments(): Result<Unit> {
        val path = "tournaments"
        return try {
            val uid = requireUserId()
            val starters = listOf(
                mapOf(
                    "title" to "MK Bermuda Solo Showdown",
                    "mode" to TournamentMode.SOLO.name,
                    "category" to TournamentCategory.FREE.name,
                    "mapName" to "Bermuda",
                    "entryFeeDemo" to 0L,
                    "prizePoolDemo" to 500L,
                    "perKillRewardDemo" to 15L,
                    "maxSlots" to 48L,
                    "filledSlots" to 0L,
                    "scheduledTimeText" to "Daily • 7:00 PM IST",
                    "status" to TournamentStatus.UPCOMING.name,
                    "rulesSummary" to "Free Fire MAX Classic Solo. Mobile only, no emulators or third-party tools. Room ID releases 15 mins before drop.",
                    "roomPublished" to false,
                    "roomPublishNote" to "",
                    "winnerSummary" to "",
                    "createdBy" to uid,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                mapOf(
                    "title" to "Crimson Duo Arena",
                    "mode" to TournamentMode.DUO.name,
                    "category" to TournamentCategory.PAID_DEMO.name,
                    "mapName" to "Purgatory",
                    "entryFeeDemo" to 50L,
                    "prizePoolDemo" to 1500L,
                    "perKillRewardDemo" to 30L,
                    "maxSlots" to 24L,
                    "filledSlots" to 0L,
                    "scheduledTimeText" to "Tonight • 8:30 PM IST",
                    "status" to TournamentStatus.UPCOMING.name,
                    "rulesSummary" to "Paid-Demo Duo Battle (Simulated MK Demo Coins only). Submit both partner Free Fire UIDs on registration.",
                    "roomPublished" to false,
                    "roomPublishNote" to "",
                    "winnerSummary" to "",
                    "createdBy" to uid,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                mapOf(
                    "title" to "MK Golden Squad Masters",
                    "mode" to TournamentMode.SQUAD.name,
                    "category" to TournamentCategory.PAID_DEMO.name,
                    "mapName" to "Kalahari",
                    "entryFeeDemo" to 100L,
                    "prizePoolDemo" to 3000L,
                    "perKillRewardDemo" to 40L,
                    "maxSlots" to 12L,
                    "filledSlots" to 0L,
                    "scheduledTimeText" to "Saturday • 9:00 PM IST",
                    "status" to TournamentStatus.LIVE.name,
                    "rulesSummary" to "4v4 Squad Custom Lobby. Team Captain registers with Squad Name and teammate UIDs. Strict anti-teaming rules.",
                    "roomPublished" to false,
                    "roomPublishNote" to "",
                    "winnerSummary" to "",
                    "createdBy" to uid,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                mapOf(
                    "title" to "NeXTerra Open Squad Scrims",
                    "mode" to TournamentMode.SQUAD.name,
                    "category" to TournamentCategory.FREE.name,
                    "mapName" to "NeXTerra",
                    "entryFeeDemo" to 0L,
                    "prizePoolDemo" to 800L,
                    "perKillRewardDemo" to 20L,
                    "maxSlots" to 12L,
                    "filledSlots" to 0L,
                    "scheduledTimeText" to "Sunday • 6:00 PM IST",
                    "status" to TournamentStatus.UPCOMING.name,
                    "rulesSummary" to "Free entry Squad scrims on NeXTerra. Top 3 squads earn simulated leaderboard points and Demo Coin rewards.",
                    "roomPublished" to false,
                    "roomPublishNote" to "",
                    "winnerSummary" to "",
                    "createdBy" to uid,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            val batch = db.batch()
            starters.forEach { payload ->
                batch.set(db.collection("tournaments").document(), payload)
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun approveRegistration(
        tournamentId: String,
        registration: TournamentRegistration,
        assignedSlot: Long
    ): Result<Unit> {
        val path = "tournaments/$tournamentId/registrations/${registration.userId}"
        return try {
            requireUserId()
            require(assignedSlot in 1L..100L) { "Slot number must be 1-100." }
            val batch = db.batch()
            batch.update(
                db.collection("tournaments").document(tournamentId)
                    .collection("registrations").document(registration.userId),
                mapOf(
                    "status" to RegistrationStatus.APPROVED.name,
                    "slotNumber" to assignedSlot,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.update(
                db.collection("tournaments").document(tournamentId)
                    .collection("slots").document(registration.userId),
                mapOf(
                    "status" to RegistrationStatus.APPROVED.name,
                    "slotNumber" to assignedSlot,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.update(
                db.collection("users").document(registration.userId)
                    .collection("registrations").document(tournamentId),
                mapOf(
                    "status" to RegistrationStatus.APPROVED.name,
                    "slotNumber" to assignedSlot,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun rejectRegistration(
        tournamentId: String,
        registration: TournamentRegistration
    ): Result<Unit> {
        val path = "tournaments/$tournamentId/registrations/${registration.userId}"
        return try {
            requireUserId()
            val batch = db.batch()
            batch.update(
                db.collection("tournaments").document(tournamentId)
                    .collection("registrations").document(registration.userId),
                mapOf(
                    "status" to RegistrationStatus.REJECTED.name,
                    "slotNumber" to 0L,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.update(
                db.collection("tournaments").document(tournamentId)
                    .collection("slots").document(registration.userId),
                mapOf(
                    "status" to RegistrationStatus.REJECTED.name,
                    "slotNumber" to 0L,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.update(
                db.collection("users").document(registration.userId)
                    .collection("registrations").document(tournamentId),
                mapOf(
                    "status" to RegistrationStatus.REJECTED.name,
                    "slotNumber" to 0L,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            if (registration.entryFeeDemo > 0L && registration.statusEnum != RegistrationStatus.REJECTED) {
                val userRef = db.collection("users").document(registration.userId)
                batch.update(
                    userRef,
                    mapOf(
                        "demoWalletBalance" to FieldValue.increment(registration.entryFeeDemo),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                batch.set(
                    userRef.collection("transactions").document(),
                    mapOf(
                        "userId" to registration.userId,
                        "type" to TransactionType.REFUND.name,
                        "amountDemo" to registration.entryFeeDemo,
                        "credit" to true,
                        "title" to "Refund: ${registration.tournamentTitle.take(55)}",
                        "description" to "Simulated entry fee refunded after registration rejection.",
                        "tournamentId" to tournamentId,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun publishRoomCredentials(
        tournamentId: String,
        roomId: String,
        roomPassword: String,
        serverRegion: String,
        instructions: String
    ): Result<Unit> {
        val path = "tournaments/$tournamentId/roomCredentials/secret"
        return try {
            val uid = requireUserId()
            val cleanRoomId = roomId.trim()
            val cleanPass = roomPassword.trim()
            require(cleanRoomId.length in 1..40) { "Room ID is required." }
            require(cleanPass.length in 1..40) { "Room Password is required." }

            val batch = db.batch()
            val credRef = db.collection("tournaments").document(tournamentId)
                .collection("roomCredentials").document("secret")
            batch.set(
                credRef,
                mapOf(
                    "tournamentId" to tournamentId,
                    "roomId" to cleanRoomId,
                    "roomPassword" to cleanPass,
                    "serverRegion" to serverRegion.trim().ifBlank { "India" }.take(40),
                    "instructions" to instructions.trim().take(240),
                    "publishedBy" to uid,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.update(
                db.collection("tournaments").document(tournamentId),
                mapOf(
                    "roomPublished" to true,
                    "roomPublishNote" to "Room ID & Password live for approved players",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun publishMatchResult(
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
    ): Result<Unit> {
        val path = "tournaments/$tournamentId/results"
        return try {
            requireUserId()
            val cleanIgn = freeFireIgn.trim()
            require(cleanIgn.isNotBlank()) { "Player IGN is required." }
            require(placement in 1L..100L) { "Placement must be 1-100." }
            require(kills in 0L..99L) { "Kills must be 0-99." }

            val targetPlayerId = playerUserId.trim().ifBlank { "guest_${cleanIgn.lowercase().replace(Regex("[^a-z0-9]"), "")}" }
            val resultRef = db.collection("tournaments").document(tournamentId)
                .collection("results").document()
            val tourneyRef = db.collection("tournaments").document(tournamentId)

            val userSnap = db.collection("users").document(targetPlayerId).get().await()
            val lbSnap = db.collection("leaderboard").document(targetPlayerId).get().await()

            val batch = db.batch()
            batch.set(
                resultRef,
                mapOf(
                    "tournamentId" to tournamentId,
                    "playerUserId" to targetPlayerId,
                    "freeFireIgn" to cleanIgn.take(30),
                    "teamName" to teamName.trim().take(40),
                    "placement" to placement,
                    "kills" to kills,
                    "points" to points.coerceAtLeast(0L),
                    "prizeDemo" to prizeDemo.coerceAtLeast(0L),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )

            if (placement == 1L) {
                val winnerText = if (teamName.isNotBlank()) "$teamName ($cleanIgn) • $kills Kills" else "$cleanIgn • $kills Kills"
                batch.update(
                    tourneyRef,
                    mapOf(
                        "status" to TournamentStatus.COMPLETED.name,
                        "winnerSummary" to winnerText.take(160),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }

            val booyahDelta = if (placement == 1L) 1L else 0L
            val safePrize = prizeDemo.coerceAtLeast(0L)

            if (userSnap.exists()) {
                val userRef = db.collection("users").document(targetPlayerId)
                val userUpdates = mutableMapOf<String, Any>(
                    "matchesPlayed" to FieldValue.increment(1L),
                    "totalKills" to FieldValue.increment(kills),
                    "booyahs" to FieldValue.increment(booyahDelta),
                    "totalWinningsDemo" to FieldValue.increment(safePrize),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                if (safePrize > 0L) {
                    userUpdates["demoWalletBalance"] = FieldValue.increment(safePrize)
                    batch.set(
                        userRef.collection("transactions").document(),
                        mapOf(
                            "userId" to targetPlayerId,
                            "type" to TransactionType.PRIZE_REWARD.name,
                            "amountDemo" to safePrize,
                            "credit" to true,
                            "title" to "Prize #$placement: ${tournamentTitle.take(45)}",
                            "description" to "Simulated prize reward ($kills kills).",
                            "tournamentId" to tournamentId,
                            "createdAt" to FieldValue.serverTimestamp()
                        )
                    )
                }
                batch.update(userRef, userUpdates)
            }

            val lbRef = db.collection("leaderboard").document(targetPlayerId)
            if (lbSnap.exists()) {
                batch.update(
                    lbRef,
                    mapOf(
                        "freeFireIgn" to cleanIgn.take(30),
                        "totalKills" to FieldValue.increment(kills),
                        "booyahs" to FieldValue.increment(booyahDelta),
                        "matchesPlayed" to FieldValue.increment(1L),
                        "points" to FieldValue.increment(points.coerceAtLeast(0L)),
                        "demoWinnings" to FieldValue.increment(safePrize),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            } else {
                val safeUid = freeFireUid.trim().take(16).let { if (it.length >= 6) it else "1000000" }
                batch.set(
                    lbRef,
                    mapOf(
                        "userId" to targetPlayerId,
                        "freeFireIgn" to cleanIgn.take(30),
                        "freeFireUid" to safeUid,
                        "totalKills" to kills,
                        "booyahs" to booyahDelta,
                        "matchesPlayed" to 1L,
                        "points" to points.coerceAtLeast(0L),
                        "demoWinnings" to safePrize,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }

            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun resolveSupportTicket(ticketId: String, adminReply: String): Result<Unit> {
        val path = "supportTickets/$ticketId"
        return try {
            requireUserId()
            db.collection("supportTickets").document(ticketId)
                .update(
                    mapOf(
                        "status" to "RESOLVED",
                        "adminReply" to adminReply.trim().take(300),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }
}
