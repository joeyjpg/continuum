package ru.otus.pandina.tests

import androidx.test.espresso.web.webdriver.Locator
import org.junit.Test
import ru.otus.pandina.screens.MainScreen
import ru.otus.pandina.screens.UserAgreementFragment
import ru.otus.pandina.screens.navigation.LoginScreen
import ru.otus.pandina.screens.navigation.NavigationViewLayout
import ru.otus.pandina.utils.NotificationDialogHelper


class LoginTest : BaseTest() {

    @Test
    fun loginTest() {
        // Handle notification dialog immediately after app starts
        NotificationDialogHelper.handleNotificationDialog()

        run {
            step("Open navigation") {
                MainScreen {
                    navButton {
                        isVisible()
                        click()
                    }
                }
                NavigationViewLayout {
                    navBanner.isVisible()
                }
            }
            step("Go to login form") {
                NavigationViewLayout {
                    accountNameTextView {
                        isVisible()
                        hasText("Anonymous")
                    }
                    karmaTextView {
                        isVisible()
                        hasText("Press here to login")
                    }
                    accountSwitcher {
                        isVisible()
                        click()
                    }
                    addAccountTextView {
                        isVisible()
                        hasText("Add an account")
                    }
                    addAccountButton {
                        isVisible()
                        longClick()
                    }
                }
            }
            step("Check if user agreement appears and handle it") {
                try {
                    UserAgreementFragment {
                        alertTitle {
                            isVisible()
                            hasText("User Agreement")
                        }
                        dontAgreeButton.isVisible()
                        agreeButton {
                            isVisible()
                            click()
                        }
                    }
                } catch (e: Exception) {
                    // User agreement dialog may not appear if already accepted or not required
                    // Continue with login process
                }
            }
            step("Enter Login and password") {
                LoginScreen {
                    webView {
                        withElement(
                            Locator.XPATH,
                            "//h1"
                        ) {
                            containsText("Log In")
                        }
                        withElement(
                            Locator.XPATH,
                            "//*[@id='login-username']"
                        ) {
                            containsText("Email or username")
                            keys("*****")
                        }
                    }
                }
            }
        }
    }
}
