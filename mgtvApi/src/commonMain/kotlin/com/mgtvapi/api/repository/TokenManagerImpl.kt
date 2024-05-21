package com.mgtvapi.api.repository

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.Settings
import com.russhwolf.settings.coroutines.getStringOrNullFlow
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.Flow

class TokenManagerImpl() : TokenManager {
    private val settings: Settings by lazy { Settings() }
    private val observableSettings: ObservableSettings by lazy { settings as ObservableSettings }
    @OptIn(ExperimentalSettingsApi::class)
    override val observableCredentials: Flow<String?>
        get() = observableSettings.getStringOrNullFlow(StorageKeys.EMAIL.key)

    override var email: String?
        get() = settings[StorageKeys.EMAIL.key]
        set(value) {
            settings[StorageKeys.EMAIL.key] = value
        }
    override var password: String?
        get() = settings[StorageKeys.PASSWORD.key]
        set(value) {
            settings[StorageKeys.PASSWORD.key] = value
        }

    override fun cleanStorage() {
        settings.clear()
    }
}