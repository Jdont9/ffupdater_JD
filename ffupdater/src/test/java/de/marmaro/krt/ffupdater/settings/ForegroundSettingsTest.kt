package de.marmaro.krt.ffupdater.settings

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import com.github.ivanshafran.sharedpreferencesmock.SPMockBuilder
import de.marmaro.krt.ffupdater.BaseTest
import de.marmaro.krt.ffupdater.device.DeviceSdkTester
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockkObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Duration

@ExtendWith(MockKExtension::class)
class ForegroundSettingsTest : BaseTest() {

    private lateinit var sharedPreferences: SharedPreferences

    @BeforeEach
    fun setUp() {
        sharedPreferences = SPMockBuilder().createSharedPreferences()
        ForegroundSettings.init(sharedPreferences)
        mockkObject(DeviceSdkTester)
    }

    @Test
    fun isUpdateCheckOnMeteredAllowed() {
        SettingsTestHelper.testBooleanSetting(
            sharedPreferences,
            "foreground__update_check__metered",
            true
        ) { ForegroundSettings.isUpdateCheckOnMeteredAllowed }
    }

    @Test
    fun isUseCacheWhenMeteredBlocked() {
        SettingsTestHelper.testBooleanSetting(
            sharedPreferences,
            "foreground__update_check__use_cache_when_metered_blocked",
            true
        ) { ForegroundSettings.isUseCacheWhenMeteredBlocked }
    }

    @Test
    fun recentCacheDuration() {
        assertEquals(Duration.ofMinutes(60), ForegroundSettings.recentCacheDuration, "Default value is incorrect")

        sharedPreferences.edit().putString("foreground__update_check__cache_duration", "15").apply()
        assertEquals(Duration.ofMinutes(15), ForegroundSettings.recentCacheDuration)

        sharedPreferences.edit().putString("foreground__update_check__cache_duration", "1440").apply()
        assertEquals(Duration.ofDays(1), ForegroundSettings.recentCacheDuration)
    }

    @Test
    fun offlineCacheDuration() {
        assertEquals(
            Duration.ofDays(2),
            ForegroundSettings.offlineCacheDuration,
            "Default value is incorrect"
        )

        sharedPreferences.edit().putString("foreground__update_check__offline_cache_duration", "10080").apply()
        assertEquals(Duration.ofDays(7), ForegroundSettings.offlineCacheDuration)

        sharedPreferences.edit().putString("foreground__update_check__offline_cache_duration", "60").apply()
        assertEquals(Duration.ofHours(1), ForegroundSettings.offlineCacheDuration)
    }

    @Test
    fun isDownloadOnMeteredAllowed() {
        SettingsTestHelper.testBooleanSetting(
            sharedPreferences,
            "foreground__download__metered",
            true
        ) { ForegroundSettings.isDownloadOnMeteredAllowed }
    }

    @Test
    fun isDeleteUpdateIfInstallSuccessful() {
        SettingsTestHelper.testBooleanSetting(
            sharedPreferences,
            "foreground__delete_cache_if_install_successful",
            true
        ) { ForegroundSettings.isDeleteUpdateIfInstallSuccessful }
    }

    @Test
    fun isDeleteUpdateIfInstallFailed() {
        SettingsTestHelper.testBooleanSetting(
            sharedPreferences,
            "foreground__delete_cache_if_install_failed",
            true
        ) { ForegroundSettings.isDeleteUpdateIfInstallFailed }
    }

    @Test
    fun getThemePreference_userHasNotChangedSetting_AndroidPAndBelow_returnDefaultValue() {
        every { DeviceSdkTester.supportsAndroid10Q29() } returns false
        assertEquals(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY, ForegroundSettings.themePreference)
    }

    @Test
    fun getThemePreference_userHasNotChangedSetting_AndroidQAndHigher_returnDefaultValue() {
        every { DeviceSdkTester.supportsAndroid10Q29() } returns true
        assertEquals(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, ForegroundSettings.themePreference)
    }

    @Test
    fun getThemePreference_withInvalidValue_null_returnDefault() {
        sharedPreferences.edit().putString("foreground__theme_preference", null).commit()

        every { DeviceSdkTester.supportsAndroid10Q29() } returns false
        assertEquals(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY, ForegroundSettings.themePreference)

        every { DeviceSdkTester.supportsAndroid10Q29() } returns true
        assertEquals(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, ForegroundSettings.themePreference)
    }

    @Test
    fun getThemePreference_withInvalidValue_emptyString_returnDefault() {
        sharedPreferences.edit().putString("foreground__theme_preference", "").commit()

        every { DeviceSdkTester.supportsAndroid10Q29() } returns false
        assertEquals(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY, ForegroundSettings.themePreference)

        every { DeviceSdkTester.supportsAndroid10Q29() } returns true
        assertEquals(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, ForegroundSettings.themePreference)
    }

    @Test
    fun getThemePreference_withInvalidValue_text_returnDefault() {
        sharedPreferences.edit().putString("foreground__theme_preference", "lorem ipsum").commit()

        every { DeviceSdkTester.supportsAndroid10Q29() } returns false
        assertEquals(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY, ForegroundSettings.themePreference)

        every { DeviceSdkTester.supportsAndroid10Q29() } returns true
        assertEquals(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, ForegroundSettings.themePreference)
    }

    @Test
    fun getThemePreference_withInvalidValue_nonExistingNumber_returnDefault() {
        sharedPreferences.edit().putString("foreground__theme_preference", "6").commit()

        every { DeviceSdkTester.supportsAndroid10Q29() } returns false
        assertEquals(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY, ForegroundSettings.themePreference)

        every { DeviceSdkTester.supportsAndroid10Q29() } returns true
        assertEquals(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, ForegroundSettings.themePreference)
    }

    @Test
    fun getThemePreference_withValidValue_MODE_NIGHT_FOLLOW_SYSTEM_returnValue() {
        sharedPreferences.edit().putString("foreground__theme_preference", "-1").commit()

        every { DeviceSdkTester.supportsAndroid10Q29() } returns false
        assertEquals(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, ForegroundSettings.themePreference)

        every { DeviceSdkTester.supportsAndroid10Q29() } returns true
        assertEquals(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, ForegroundSettings.themePreference)
    }

    @Test
    fun getThemePreference_withValidValue_MODE_NIGHT_NO_returnValue() {
        sharedPreferences.edit().putString("foreground__theme_preference", "1").commit()

        every { DeviceSdkTester.supportsAndroid10Q29() } returns false
        assertEquals(AppCompatDelegate.MODE_NIGHT_NO, ForegroundSettings.themePreference)

        every { DeviceSdkTester.supportsAndroid10Q29() } returns true
        assertEquals(AppCompatDelegate.MODE_NIGHT_NO, ForegroundSettings.themePreference)
    }

    @Test
    fun getThemePreference_withValidValue_MODE_NIGHT_YES_returnValue() {
        sharedPreferences.edit().putString("foreground__theme_preference", "2").commit()

        every { DeviceSdkTester.supportsAndroid10Q29() } returns false
        assertEquals(AppCompatDelegate.MODE_NIGHT_YES, ForegroundSettings.themePreference)

        every { DeviceSdkTester.supportsAndroid10Q29() } returns true
        assertEquals(AppCompatDelegate.MODE_NIGHT_YES, ForegroundSettings.themePreference)
    }

    @Test
    fun getThemePreference_withValidValue_MODE_NIGHT_AUTO_BATTERY_returnValue() {
        sharedPreferences.edit().putString("foreground__theme_preference", "3").commit()

        every { DeviceSdkTester.supportsAndroid10Q29() } returns false
        assertEquals(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY, ForegroundSettings.themePreference)

        every { DeviceSdkTester.supportsAndroid10Q29() } returns true
        assertEquals(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY, ForegroundSettings.themePreference)
    }
}