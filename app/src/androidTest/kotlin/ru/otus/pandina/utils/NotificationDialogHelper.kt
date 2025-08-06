package ru.otus.pandina.utils

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector

object NotificationDialogHelper {
    
    fun handleNotificationDialog() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
                
        Thread.sleep(3000) // Wait for app and dialog to fully load
                
        // Try to find and click "Don't allow" or similar buttons first
        val dontAllowButton = device.findObject(UiSelector().text("Don't allow"))
        val allowButton = device.findObject(UiSelector().text("Allow"))
                    
        when {
            dontAllowButton.exists() -> {
                dontAllowButton.click()
                Thread.sleep(1000)
            }
            allowButton.exists() -> {
                // If only "Allow" is visible, click it to proceed (we can test without notifications)
                allowButton.click()
                Thread.sleep(1000)
            }
            else -> {
                // If no dialog buttons found, the dialog might have been dismissed already
                Thread.sleep(1000)
            }
        }
    }
}