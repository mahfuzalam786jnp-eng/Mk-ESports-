const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ADMIN_UID = "owner_admin_uid";
const ALICE_UID = "alice_uid_101";
const BOB_UID = "bob_uid_202";
const SPOOF_UID = "spoof_uid_999";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function pastTime(msOffset = 2000) {
  return new Date(Date.now() - msOffset);
}

function validUserPayload(uid, ign = "MK_Alice", ffUid = "123456789") {
  const ts = pastTime(5000);
  return {
    userId: uid,
    displayName: "Alice Player",
    email: "alice@example.com",
    phoneNumber: "+919876543210",
    phoneVerified: true,
    freeFireUid: ffUid,
    freeFireIgn: ign,
    demoWalletBalance: 1000,
    referralCode: "MKALICE1",
    referredByCode: "",
    referralCount: 0,
    matchesPlayed: 0,
    totalKills: 0,
    booyahs: 0,
    totalWinningsDemo: 0,
    createdAt: ts,
    updatedAt: ts,
  };
}

function validTournamentPayload(createdBy = ADMIN_UID) {
  const ts = pastTime(5000);
  return {
    title: "MK Bermuda Solo Clash",
    mode: "SOLO",
    category: "PAID_DEMO",
    mapName: "Bermuda",
    entryFeeDemo: 50,
    prizePoolDemo: 2000,
    perKillRewardDemo: 25,
    maxSlots: 48,
    filledSlots: 0,
    scheduledTimeText: "Tonight 8:00 PM IST",
    status: "UPCOMING",
    rulesSummary: "No hacks, no teaming. Emulator blocked.",
    roomPublished: false,
    roomPublishNote: "",
    winnerSummary: "",
    createdBy,
    createdAt: ts,
    updatedAt: ts,
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated users cannot read or write any collection", async () => {
  const unauth = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauth.collection("tournaments").get());
  await assertFails(unauth.collection("leaderboard").get());
  await assertFails(unauth.collection("users").doc(ALICE_UID).set(validUserPayload(ALICE_UID)));
});

test("User profile isolation: owner creates with 1000 demo coins; cross-user PII blocked; no stat tampering", async () => {
  const alice = testEnv.authenticatedContext(ALICE_UID, { email: "alice@example.com", email_verified: true }).firestore();
  const bob = testEnv.authenticatedContext(BOB_UID, { email: "bob@example.com", email_verified: true }).firestore();

  // Alice creates valid profile
  await assertSucceeds(alice.collection("users").doc(ALICE_UID).set(validUserPayload(ALICE_UID)));

  // Reject profile creation with inflated demoWalletBalance
  await assertFails(
    bob.collection("users").doc(BOB_UID).set({
      ...validUserPayload(BOB_UID, "MK_Bob", "987654321"),
      demoWalletBalance: 999999,
    })
  );

  // Reject ghost field on profile
  await assertFails(
    bob.collection("users").doc(BOB_UID).set({
      ...validUserPayload(BOB_UID, "MK_Bob", "987654321"),
      isAdmin: true,
    })
  );

  // Bob cannot read Alice's PII profile or list /users
  await assertFails(bob.collection("users").doc(ALICE_UID).get());
  await assertFails(bob.collection("users").get());

  // Alice can do simulated demo top-up <= 1000
  await assertSucceeds(
    alice.collection("users").doc(ALICE_UID).update({
      demoWalletBalance: 1500,
      updatedAt: pastTime(1000),
    })
  );

  // Alice cannot self-topup 5000 coins in one go or inflate kills/booyahs
  await assertFails(
    alice.collection("users").doc(ALICE_UID).update({
      demoWalletBalance: 6500,
      updatedAt: pastTime(1000),
    })
  );
  await assertFails(
    alice.collection("users").doc(ALICE_UID).update({
      totalKills: 99,
      updatedAt: pastTime(1000),
    })
  );
});

test("Owner-only Admin RBAC: only verified owner email can manage tournaments, rooms, and results; spoof blocked", async () => {
  const admin = testEnv.authenticatedContext(ADMIN_UID, { email: "mahfuzalam786jnp@gmail.com", email_verified: true }).firestore();
  const spoofAdmin = testEnv.authenticatedContext(SPOOF_UID, { email: "mahfuzalam786jnp@gmail.com", email_verified: false }).firestore();
  const alice = testEnv.authenticatedContext(ALICE_UID, { email: "alice@example.com", email_verified: true }).firestore();

  // Regular user & unverified email spoof cannot create tournament
  await assertFails(alice.collection("tournaments").doc("tour_1").set(validTournamentPayload(ALICE_UID)));
  await assertFails(spoofAdmin.collection("tournaments").doc("tour_1").set(validTournamentPayload(SPOOF_UID)));

  // Verified owner admin creates tournament
  await assertSucceeds(admin.collection("tournaments").doc("tour_1").set(validTournamentPayload(ADMIN_UID)));

  // Publish room credentials
  await assertSucceeds(
    admin.collection("tournaments").doc("tour_1").collection("roomCredentials").doc("secret").set({
      tournamentId: "tour_1",
      roomId: "45892011",
      roomPassword: "MK99",
      serverRegion: "India",
      instructions: "Join 10 mins early.",
      publishedBy: ADMIN_UID,
      updatedAt: pastTime(1000),
    })
  );
});

test("Tournament registration, slot approval, and secret Room ID/Password access control", async () => {
  const admin = testEnv.authenticatedContext(ADMIN_UID, { email: "mahfuzalam786jnp@gmail.com", email_verified: true }).firestore();
  const alice = testEnv.authenticatedContext(ALICE_UID, { email: "alice@example.com", email_verified: true }).firestore();
  const bob = testEnv.authenticatedContext(BOB_UID, { email: "bob@example.com", email_verified: true }).firestore();

  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    await db.collection("tournaments").doc("tour_1").set(validTournamentPayload(ADMIN_UID));
    await db.collection("tournaments").doc("tour_1").collection("roomCredentials").doc("secret").set({
      tournamentId: "tour_1",
      roomId: "77889900",
      roomPassword: "GOLD",
      serverRegion: "India",
      instructions: "Slot #1",
      publishedBy: ADMIN_UID,
      updatedAt: pastTime(2000),
    });
    await db.collection("users").doc(ALICE_UID).set(validUserPayload(ALICE_UID));
  });

  // Unregistered Bob cannot read roomCredentials
  await assertFails(bob.collection("tournaments").doc("tour_1").collection("roomCredentials").doc("secret").get());

  // Alice registers (PENDING)
  const ts = pastTime(1500);
  await assertSucceeds(
    alice.collection("tournaments").doc("tour_1").collection("registrations").doc(ALICE_UID).set({
      userId: ALICE_UID,
      tournamentId: "tour_1",
      tournamentTitle: "MK Bermuda Solo Clash",
      playerName: "Alice Player",
      freeFireUid: "123456789",
      freeFireIgn: "MK_Alice",
      mode: "SOLO",
      teamName: "",
      partnerInfo: "",
      entryFeeDemo: 50,
      status: "PENDING",
      slotNumber: 0,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  await assertSucceeds(
    alice.collection("users").doc(ALICE_UID).collection("registrations").doc("tour_1").set({
      userId: ALICE_UID,
      tournamentId: "tour_1",
      tournamentTitle: "MK Bermuda Solo Clash",
      mode: "SOLO",
      category: "PAID_DEMO",
      entryFeeDemo: 50,
      freeFireUid: "123456789",
      freeFireIgn: "MK_Alice",
      teamName: "",
      partnerInfo: "",
      status: "PENDING",
      slotNumber: 0,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Alice cannot self-approve
  await assertFails(
    alice.collection("tournaments").doc("tour_1").collection("registrations").doc(ALICE_UID).update({
      status: "APPROVED",
      slotNumber: 1,
      updatedAt: pastTime(1000),
    })
  );

  // Pending Alice still cannot read roomCredentials
  await assertFails(alice.collection("tournaments").doc("tour_1").collection("roomCredentials").doc("secret").get());

  // Admin approves Alice and assigns Slot #1
  await assertSucceeds(
    admin.collection("tournaments").doc("tour_1").collection("registrations").doc(ALICE_UID).update({
      status: "APPROVED",
      slotNumber: 1,
      updatedAt: pastTime(1000),
    })
  );

  // Now approved Alice CAN read roomCredentials, while Bob still CANNOT
  await assertSucceeds(alice.collection("tournaments").doc("tour_1").collection("roomCredentials").doc("secret").get());
  await assertFails(bob.collection("tournaments").doc("tour_1").collection("roomCredentials").doc("secret").get());
});

test("User transactions, referral codes, leaderboard, and support ticket query parity", async () => {
  const admin = testEnv.authenticatedContext(ADMIN_UID, { email: "mahfuzalam786jnp@gmail.com", email_verified: true }).firestore();
  const alice = testEnv.authenticatedContext(ALICE_UID, { email: "alice@example.com", email_verified: true }).firestore();
  const bob = testEnv.authenticatedContext(BOB_UID, { email: "bob@example.com", email_verified: true }).firestore();

  const ts = pastTime(2000);
  await assertSucceeds(
    alice.collection("users").doc(ALICE_UID).collection("transactions").doc("tx_1").set({
      userId: ALICE_UID,
      type: "WELCOME_BONUS",
      amountDemo: 1000,
      credit: true,
      title: "Welcome Bonus",
      description: "Initial MK Demo Coins",
      tournamentId: "",
      createdAt: ts,
    })
  );

  // Owner lists own transactions; Bob cannot
  await assertSucceeds(alice.collection("users").doc(ALICE_UID).collection("transactions").get());
  await assertFails(bob.collection("users").doc(ALICE_UID).collection("transactions").get());

  // Support ticket: Alice creates OPEN ticket, queries filtered by userId; unfiltered list fails for non-admin
  await assertSucceeds(
    alice.collection("supportTickets").doc("ticket_1").set({
      userId: ALICE_UID,
      playerIgn: "MK_Alice",
      category: "ROOM_ISSUE",
      subject: "Room ID check",
      message: "Need confirmation on room schedule.",
      status: "OPEN",
      adminReply: "",
      createdAt: ts,
      updatedAt: ts,
    })
  );
  await assertSucceeds(alice.collection("supportTickets").where("userId", "==", ALICE_UID).get());
  await assertFails(alice.collection("supportTickets").get());
  await assertSucceeds(admin.collection("supportTickets").get());

  // Leaderboard: Alice can initialize at zero, cannot fake points; Admin can update points
  await assertSucceeds(
    alice.collection("leaderboard").doc(ALICE_UID).set({
      userId: ALICE_UID,
      freeFireIgn: "MK_Alice",
      freeFireUid: "123456789",
      totalKills: 0,
      booyahs: 0,
      matchesPlayed: 0,
      points: 0,
      demoWinnings: 0,
      createdAt: ts,
      updatedAt: ts,
    })
  );
  await assertFails(
    alice.collection("leaderboard").doc(ALICE_UID).update({
      points: 500,
      updatedAt: pastTime(500),
    })
  );
  await assertSucceeds(
    admin.collection("leaderboard").doc(ALICE_UID).update({
      totalKills: 8,
      booyahs: 1,
      matchesPlayed: 1,
      points: 180,
      demoWinnings: 500,
      updatedAt: pastTime(500),
    })
  );
});
