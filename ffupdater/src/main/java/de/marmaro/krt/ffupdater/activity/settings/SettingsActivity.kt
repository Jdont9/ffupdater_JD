package de.marmaro.krt.ffupdater.activity.settings

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.annotation.Keep
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.MultiSelectListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.topjohnwu.superuser.Shell
import de.marmaro.krt.ffupdater.R
import de.marmaro.krt.ffupdater.app.App
import de.marmaro.krt.ffupdater.background.BackgroundWork
import de.marmaro.krt.ffupdater.device.DeviceSdkTester
import de.marmaro.krt.ffupdater.installer.entity.Installer
import de.marmaro.krt.ffupdater.network.file.FileDownloader
import de.marmaro.krt.ffupdater.settings.DataStoreHelper
import de.marmaro.krt.ffupdater.settings.ForegroundSettings
import de.marmaro.krt.ffupdater.settings.NetworkSettings.DnsProvider.CUSTOM_SERVER
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku


/**
 * Activity for displaying the settings view.
 */
@Keep
class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        AppCompatDelegate.setDefaultNightMode(ForegroundSettings.themePreference)
        if (savedInstanceState == null) { //https://stackoverflow.com/a/60348385
            supportFragmentManager.beginTransaction().replace(R.id.settings_activity__main_layout, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // SET padding (never add to the previous value). Dialog/IME inset callbacks
        // used to stack status-bar margins and punch a hole under the toolbar.
        val container = findViewById<View>(R.id.settings_activity__main_layout)
        setOnApplyWindowInsetsListener(container) { v: View, insets: WindowInsetsCompat ->
            val bars: Insets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, bars.top, 0, bars.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        private fun findSwitchPref(key: String) = findPreference<SwitchPreferenceCompat>(key)!!
        private fun findListPref(key: String) = findPreference<ListPreference>(key)!!
        private fun findMultiPref(key: String) = findPreference<MultiSelectListPreference>(key)!!
        private fun findTextPref(key: String) = findPreference<EditTextPreference>(key)!!

        // EditTextPreference dialogs (Vanadium branch/prebuilt, DNS, proxy) run in a
        // DialogFragment. With adjustResize the activity/RecyclerView was measured for the
        // IME, then never rebound after OK/Cancel — huge blank gaps until you leave and
        // re-enter Settings. Rebuild the list when that dialog is destroyed.
        private val preferenceDialogLifecycleCallbacks = object : FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
                if (f.tag == PREFERENCE_DIALOG_FRAGMENT_TAG) {
                    relayoutPreferenceList()
                }
            }
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            hideOptionsForLowerApis()
            loadHiddenAppNames()
            loadExcludedAppNames()
            listenForBackgroundJobRestarts()
            listenForThemeChanges()
            deleteFileCacheWhenChange32BitAppsPreference()
            setupInstallerValidator()
            setupNetworkSettingsValidator()
            updateAdvancedVanadiumSettingsVisibility()
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            listView.itemAnimator = null
            parentFragmentManager.registerFragmentLifecycleCallbacks(
                preferenceDialogLifecycleCallbacks,
                false
            )
        }

        override fun onDestroyView() {
            parentFragmentManager.unregisterFragmentLifecycleCallbacks(preferenceDialogLifecycleCallbacks)
            super.onDestroyView()
        }

        override fun onResume() {
            super.onResume()
            // the unlock gesture lives in MainActivity, so re-check every time this screen becomes visible
            updateAdvancedVanadiumSettingsVisibility()
            relayoutPreferenceList()
        }

        private fun relayoutPreferenceList() {
            if (!isAdded || view == null) return
            val rv: RecyclerView = listView ?: return
            rv.itemAnimator = null
            rv.post {
                if (!isAdded) return@post
                rv.adapter?.notifyDataSetChanged()
                rv.invalidateItemDecorations()
                rv.requestLayout()
            }
        }

        private fun updateAdvancedVanadiumSettingsVisibility() {
            val unlocked = DataStoreHelper.advancedVanadiumSettingsUnlocked
            val category = findPreference<PreferenceCategory>("vanadium_advanced_settings_screen")
            category?.isVisible = unlocked
            if (category != null) {
                for (i in 0 until category.preferenceCount) {
                    category.getPreference(i).isVisible = unlocked
                }
            }
        }

        private fun hideOptionsForLowerApis() {
            if (!DeviceSdkTester.supportsAndroid6M23()) {
                findSwitchPref("background__update_check__when_device_idle").summary =
                    getString(R.string.settings__background__update_check__when_device_idle__unsupported)
                findSwitchPref("background__update_check__when_device_idle").isEnabled = false
            }
        }

        private fun loadHiddenAppNames() {
            val visibleApps = App.values().filter { it.findImpl().visibleToUser }
            val hiddenApps = findMultiPref("foreground__hidden_apps")
            hiddenApps.entries = visibleApps.map { getString(it.findImpl().title) }.toTypedArray()
            hiddenApps.entryValues = visibleApps.map { it.name }.toTypedArray()
        }

        private fun loadExcludedAppNames() {
            val visibleApps = App.values().filter { it.findImpl().visibleToUser }
            val excludedApps = findMultiPref("background__update_check__excluded_apps")
            excludedApps.entries = visibleApps.map { getString(it.findImpl().title) }.toTypedArray()
            excludedApps.entryValues = visibleApps.map { it.name }.toTypedArray()
        }

        private fun listenForBackgroundJobRestarts() {
            val listener = Preference.OnPreferenceChangeListener { _, _ ->
                restartBackgroundJobAfterClosingActivity = true
                // reset false warning of non executed background update check
                DataStoreHelper.storeThatBackgroundCheckWasTrigger()
                true
            }
            findSwitchPref("background__update_check__enabled").onPreferenceChangeListener = listener
            findListPref("background__update_check__interval").onPreferenceChangeListener = listener
        }

        private fun listenForThemeChanges() {
            findListPref("foreground__theme_preference").setOnPreferenceChangeListener { _, newValue ->
                AppCompatDelegate.setDefaultNightMode((newValue as String).toInt())
                true
            }
        }

        private fun deleteFileCacheWhenChange32BitAppsPreference() {
            findSwitchPref("device__prefer_32bit_apks").setOnPreferenceChangeListener { _, _ ->
                lifecycleScope.launch(Dispatchers.Main) {
                    App.values().forEach {
                        it.findImpl().deleteFileCache(requireContext())
                    }
                }
                true
            }
        }

        private var restartBackgroundJobAfterClosingActivity = false

        private fun setupInstallerValidator() {
            findListPref("installer__method").setOnPreferenceChangeListener { _, newValue ->
                when (newValue) {
                    Installer.ROOT_INSTALLER.name -> canRootInstallerBeUsed()
                    Installer.SHIZUKU_INSTALLER.name -> canShizukuInstallerBeUsed()
                    else -> true
                }
            }
        }

        private fun canRootInstallerBeUsed(): Boolean {
            Shell.getShell().use {
                if (it.isRoot) {
                    return true
                }
            }
            showBriefMessage(R.string.installer__method__root_not_granted)
            return false
        }

        private fun canShizukuInstallerBeUsed(): Boolean {
            if (!DeviceSdkTester.supportsAndroid6M23()) {
                showBriefMessage(R.string.installer__android_too_old_for_shizuku)
                return false
            }
            return try {
                if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                    Shizuku.requestPermission(42)
                }
                true
            } catch (e: IllegalStateException) {
                showBriefMessage(R.string.installer__method__shizuku_not_installed)
                false
            }
        }

        private fun setupNetworkSettingsValidator() {
            val listener = Preference.OnPreferenceChangeListener { _, _ ->
                FileDownloader.restart()
                true
            }
            val dnsProvider = findListPref("network__dns_provider")
            val customDohServer = findTextPref("network__custom_doh_server")
            val trustUserCA = findSwitchPref("network__trust_user_cas")
            val networkProxy = findTextPref("network__proxy")

            trustUserCA.onPreferenceChangeListener = listener
            dnsProvider.setOnPreferenceChangeListener { pref, newValue ->
                customDohServer.isVisible = (newValue == CUSTOM_SERVER.name)
                listener.onPreferenceChange(pref, newValue)
            }
            customDohServer.isVisible = (dnsProvider.value == CUSTOM_SERVER.name)
            customDohServer.onPreferenceChangeListener = listener
            networkProxy.onPreferenceChangeListener = listener
        }

        @UiThread
        private fun showBriefMessage(message: Int) {
            Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
        }

        override fun onPause() {
            super.onPause()
            if (restartBackgroundJobAfterClosingActivity) {
                restartBackgroundJobAfterClosingActivity = false
                BackgroundWork.forceRestart(requireContext().applicationContext)
            }
        }

        companion object {
            private const val PREFERENCE_DIALOG_FRAGMENT_TAG =
                "androidx.preference.PreferenceFragment.DIALOG"
        }
    }
}
