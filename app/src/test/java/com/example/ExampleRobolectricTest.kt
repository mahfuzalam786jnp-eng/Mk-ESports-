package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.example.model.DEV_TEST_OTP_CODE
import com.example.model.Tournament
import com.example.model.TournamentCategory
import com.example.model.TournamentMode
import com.example.model.TournamentStatus
import com.example.model.UiState
import com.example.model.UserProfile
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MobileOtpOnboardingScreen
import com.example.ui.theme.MkEsportsTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `app name and firestore database id are configured`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals("MK Esports", context.getString(R.string.app_name))
        assertTrue(context.getString(R.string.firestore_database_id).isNotBlank())
    }

    @Test
    fun `home screen filters solo duo squad and displays demo wallet`() {
        val sampleUser = UserProfile(
            userId = "u1",
            displayName = "MK Warrior",
            freeFireIgn = "MK_SHADOW",
            freeFireUid = "123456789",
            demoWalletBalance = 1250L
        )
        val tournaments = listOf(
            Tournament(
                id = "t_solo",
                title = "Bermuda Solo Cup",
                mode = TournamentMode.SOLO.name,
                category = TournamentCategory.FREE.name,
                mapName = "Bermuda",
                prizePoolDemo = 500L,
                maxSlots = 48L,
                filledSlots = 10L,
                scheduledTimeText = "7:00 PM IST",
                status = TournamentStatus.UPCOMING.name
            ),
            Tournament(
                id = "t_squad",
                title = "Kalahari Squad Masters",
                mode = TournamentMode.SQUAD.name,
                category = TournamentCategory.PAID_DEMO.name,
                mapName = "Kalahari",
                entryFeeDemo = 100L,
                prizePoolDemo = 3000L,
                maxSlots = 12L,
                filledSlots = 4L,
                scheduledTimeText = "9:00 PM IST",
                status = TournamentStatus.LIVE.name
            )
        )

        composeTestRule.setContent {
            MkEsportsTheme {
                HomeScreen(
                    userProfile = sampleUser,
                    tournamentsState = UiState.Success(tournaments),
                    isOwnerAdmin = false,
                    isActionLoading = false,
                    onTournamentClick = {},
                    onWalletClick = {},
                    onAdminClick = {},
                    onSeedTournaments = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("1250").assertIsDisplayed()
        composeTestRule.onNodeWithTag("tournament_card_t_solo").assertExists()

        // Filter to SQUAD only
        composeTestRule.onNodeWithTag("filter_mode_squad").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("tournament_card_t_squad").assertExists()
        composeTestRule.onNodeWithTag("tournament_card_t_solo").assertDoesNotExist()
    }

    @Test
    fun `mobile otp onboarding verifies test otp before submitting`() {
        var submittedPhone = ""
        var submittedIgn = ""

        composeTestRule.setContent {
            MkEsportsTheme {
                MobileOtpOnboardingScreen(
                    initialPhone = "+919876543210",
                    initialDisplayName = "Tester",
                    userEmail = "player@example.com",
                    isSubmitting = false,
                    onSubmitOnboarding = { _, phone, _, ign, _ ->
                        submittedPhone = phone
                        submittedIgn = ign
                    },
                    onSignOut = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("send_test_otp_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("otp_code_input").performScrollTo().performTextInput(DEV_TEST_OTP_CODE)
        composeTestRule.onNodeWithTag("verify_otp_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("onboarding_ign_input").performScrollTo().performTextInput("MK_BLAZE")
        composeTestRule.onNodeWithTag("onboarding_uid_input").performScrollTo().performTextInput("887766554")
        composeTestRule.onNodeWithTag("complete_signup_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        assertEquals("+919876543210", submittedPhone)
        assertEquals("MK_BLAZE", submittedIgn)
    }

    @Test
    fun `non owner user is blocked from admin console UI`() {
        composeTestRule.setContent {
            MkEsportsTheme {
                AdminDashboardScreen(
                    isOwnerAdmin = false,
                    tournamentsState = UiState.Success(emptyList()),
                    selectedTournament = null,
                    selectedRegistrations = emptyList(),
                    roomCredential = null,
                    allSupportTicketsState = UiState.Success(emptyList()),
                    isActionLoading = false,
                    onBack = {},
                    onSelectTournament = {},
                    onLoadRoomCredentials = {},
                    onSeedTournaments = {},
                    onSaveTournament = { _, _, _, _, _, _, _, _, _, _, _, _ -> },
                    onDeleteTournament = {},
                    onApproveRegistration = { _, _, _ -> },
                    onRejectRegistration = { _, _ -> },
                    onPublishRoom = { _, _, _, _, _ -> },
                    onPublishResult = { _, _, _, _, _, _, _, _, _, _ -> },
                    onResolveTicket = { _, _ -> }
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Owner Access Only").assertIsDisplayed()
    }
}
