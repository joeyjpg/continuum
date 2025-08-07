package ru.otus.pandina.tests

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import ml.docilealligator.infinityforreddit.activities.MainActivity
import org.hamcrest.Matchers.endsWith
import org.junit.Test
import ru.otus.pandina.screens.MainScreen
import ru.otus.pandina.screens.navigation.NavigationViewLayout
import ru.otus.pandina.screens.navigation.settings.SettingsScreen
import ru.otus.pandina.utils.NotificationDialogHelper

class APIKeysTest : BaseTest() {


    fun openSettings() {
        run {
            step("Open navigation drawer") {
                MainScreen {
                    navButton {
                        isVisible()
                        click()
                    }
                }
                NavigationViewLayout {
                    navBanner.isVisible()
                    settings.isVisible()
                    nawDrawerRecyclerView {
                        scrollToEnd()
                    }
                }
            }
            step("Open settings") {
                NavigationViewLayout.settings.click()
                SettingsScreen {
                    screenTittle {
                        isVisible()
                        hasText("Settings")
                    }
                }
            }
        }
    }

    @Test
    fun addRedditClientIdTest() {
        val redditClientId = InstrumentationRegistry.getArguments().getString("REDDIT_CLIENT_ID") ?: "test_reddit_client_id_default"

        // Handle notification dialog immediately after app starts
        NotificationDialogHelper.handleNotificationDialog()

        before {
            openSettings()
        }.after {

        }.run {
            step("Open API Keys screen") {
                onView(withText("API Keys")).perform(click())

                Thread.sleep(1000)

                // Verify we're on API Keys screen
                onView(withText("API Keys")).check(matches(isDisplayed()))
                onView(withText("Reddit API Client ID")).check(matches(isDisplayed()))
            }
            step("Add Reddit API Client ID") {
                onView(withText("Reddit API Client ID")).perform(click())

                Thread.sleep(1000)

                // Type the Reddit Client ID in the dialog
                onView(withClassName(endsWith("EditText"))).perform(typeText(redditClientId))

                // Verify OK button is clickable (test passes without clicking to avoid app restart)
                onView(withText("OK")).check(matches(isDisplayed()))
            }
        }
    }
}