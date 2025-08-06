package ru.otus.pandina.tests

import android.graphics.Color
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withText
import ml.docilealligator.infinityforreddit.activities.MainActivity
import org.junit.Test
import ru.otus.pandina.screens.navigation.settings.interfaceScreen.CustomizeTabsScreen
import ru.otus.pandina.screens.MainScreen
import ru.otus.pandina.screens.navigation.NavigationViewLayout
import ru.otus.pandina.screens.navigation.settings.ActionPanel
import ru.otus.pandina.screens.navigation.settings.SettingsScreen
import ru.otus.pandina.screens.navigation.settings.ThemeScreen
import ru.otus.pandina.screens.navigation.settings.font.FontPreviewScreen
import ru.otus.pandina.screens.navigation.settings.font.FontScreen
import ru.otus.pandina.screens.navigation.settings.interfaceScreen.InterfaceScreen
import ru.otus.pandina.screens.navigation.settings.notification.NotificationScreen

class SettingsTest : BaseTest() {

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
    fun disableNotificationTest() {
        before {
            openSettings()
        }.after {

        }.run {
            step("Open notifications screen and disable notifications") {
                SettingsScreen.notification.click()
                NotificationScreen {
                    screenTitle {
                        isVisible()
                        hasText("Notification")
                    }
                    enableNotifications.isVisible()
                    notificationInterval.isVisible()
                    notificationSwitch.click()
                    notificationInterval.doesNotExist()
                    notificationSwitch.click()
                    notificationInterval.isVisible()
                }
            }
        }
    }

    @Test
    fun setFontTest() {
        before {
            openSettings()
        }.after {

        }.run {
            step("Open interface screen") {
                SettingsScreen.interfaceSetting.click()
                InterfaceScreen {
                    screenTitle {
                        isVisible()
                        hasText("Interface")
                    }
                    font.isVisible()
                }
            }
            step("Open font screen and set font") {
                InterfaceScreen.font.click()
                FontScreen {
                    screenTitle {
                        isVisible()
                        hasText("Font")
                    }
                    fontPreview {
                        isVisible()
                        click()
                    }
                }
                FontPreviewScreen {
                    screenTitle {
                        isVisible()
                        hasText("Font Preview")
                    }
                }
            }
        }
    }

    @Test
    fun customizeTabsInMainPage() {
        before {
            openSettings()
        }.after {
        }.run {
            step("Open interface screen") {
                SettingsScreen.interfaceSetting.click()
                InterfaceScreen {
                    screenTitle {
                        isVisible()
                        hasText("Interface")
                    }
                    customizeTabs.isVisible()
                }
            }
            step("Open and Customize Tabs in Main Page") {
                InterfaceScreen.customizeTabs.click()
                CustomizeTabsScreen {
                    screenTitle {
                        isVisible()
                        hasText("Customize Tabs in Main Page")
                    }
                    tabCountTitle {
                        isVisible()
                        hasText("Tab Count")
                        click()
                    }
                }

                // Skip ActionPanel verification and go directly to selection
                Thread.sleep(500)
                onView(withText("2")).perform(click())

                // Wait for UI to update
                Thread.sleep(500)

                CustomizeTabsScreen {
                    tabCountSummary.hasText("2")
                }
            }
            step("Restart app") {
                activityRule.scenario.close()
                ActivityScenario.launch(MainActivity::class.java, null)
            }
            step("Check tabs") {
                MainScreen {
                    tabLayout {
                        hasDescendant {
                            withText("Home")
                        }
                        hasDescendant {
                            withText("Popular")
                        }
                        hasNotDescendant {
                            withText("All")
                        }
                    }
                }
            }
        }
    }

    @Test
    fun setDarkThemeTest() {
        before {
            openSettings()
        }.after {
        }.run {
            step("Open Theme screen and set dark theme") {
                SettingsScreen.theme.click()

                ThemeScreen {
                    themeRecycler {
                        // Click on the "Theme" preference item
                        childWith<ThemeScreen.ThemeRecyclerItem> {
                            withDescendant { withText("Theme") }
                        }.click()
                    }
                }

                // Wait for dialog and select Dark Theme
                Thread.sleep(1000)
                onView(withText("Dark Theme")).perform(click())

                // Give the UI time to update
                Thread.sleep(500)

                ThemeScreen {
                    themeRecycler {
                        // Verify the Theme item now shows "Dark Theme" in summary
                        childWith<ThemeScreen.ThemeRecyclerItem> {
                            withDescendant { withText("Theme") }
                        }.summary.hasText("Dark Theme")
                    }
                }
            }
        }
    }

    @Test
    fun setLightThemeTest() {
        before {
            openSettings()
        }.after {
        }.run {
            step("Open Theme screen and set light theme") {
                SettingsScreen.theme.click()

                ThemeScreen {
                    themeRecycler {
                        // Click on the "Theme" preference item
                        childWith<ThemeScreen.ThemeRecyclerItem> {
                            withDescendant { withText("Theme") }
                        }.click()
                    }
                }

                // Wait for dialog and select Light Theme
                Thread.sleep(1000)
                onView(withText("Light Theme")).perform(click())
                // Remove this line - no OK button needed:
                // onView(withText("OK")).perform(click())

                // Give the UI time to update
                Thread.sleep(500)

                ThemeScreen {
                    themeRecycler {
                        // Verify the Theme item now shows "Light Theme" in summary
                        childWith<ThemeScreen.ThemeRecyclerItem> {
                            withDescendant { withText("Theme") }
                        }.summary.hasText("Light Theme")
                    }
                    frame.hasBackgroundColor(Color.WHITE)
                }
            }
        }
    }
}
