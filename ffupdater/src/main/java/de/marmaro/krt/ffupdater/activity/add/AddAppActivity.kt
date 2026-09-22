package de.marmaro.krt.ffupdater.activity.add

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.annotation.Keep
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.marmaro.krt.ffupdater.R
import de.marmaro.krt.ffupdater.app.App
import de.marmaro.krt.ffupdater.app.entity.DisplayCategory.BASED_ON_FIREFOX
import de.marmaro.krt.ffupdater.app.entity.DisplayCategory.BETTER_THAN_GOOGLE_CHROME
import de.marmaro.krt.ffupdater.app.entity.DisplayCategory.EOL
import de.marmaro.krt.ffupdater.app.entity.DisplayCategory.FROM_MOZILLA
import de.marmaro.krt.ffupdater.app.entity.DisplayCategory.GOOD_PRIVACY_BROWSER
import de.marmaro.krt.ffupdater.app.entity.DisplayCategory.GOOD_SECURITY_BROWSER
import de.marmaro.krt.ffupdater.app.entity.DisplayCategory.OTHER
import de.marmaro.krt.ffupdater.app.entity.DisplayCategory.values
import de.marmaro.krt.ffupdater.app.ReleaseAgeHelper
import de.marmaro.krt.ffupdater.device.DeviceAbiExtractor
import de.marmaro.krt.ffupdater.settings.ForegroundSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

@Keep
class AddAppActivity : AppCompatActivity() {
    private var finishActivityOnNextResume = false

    override fun onCreate(savedInstanceState: Bundle?) {
        // edge to edge is already supported by this layout
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_app)
        AppCompatDelegate.setDefaultNightMode(ForegroundSettings.themePreference)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        lifecycleScope.launch(Dispatchers.IO) {
            addAppsToUserInterface()
        }
    }

    override fun onResume() {
        super.onResume()
        if (finishActivityOnNextResume) {
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    @UiThread
    private suspend fun addAppsToUserInterface() {
        val notInstalledApps = App.values()
            .map { it.findImpl() }
            .filter { it.installableByUser }
            .filter { DeviceAbiExtractor.supportsOneOf(it.supportedAbis) }
            .filter { !it.isInstalledWithoutFingerprintVerification(applicationContext.packageManager) }

        // An app whose latest known release is old (see ReleaseAgeHelper) is shown under the EOL section
        // below instead of its usual category, even though nothing had to hardcode it there - same
        // condition as the "Outdated" badge shown for installed apps on the main screen.
        val staleApps = notInstalledApps
            .map { app ->
                lifecycleScope.async(Dispatchers.IO) {
                    val isStale = try {
                        ReleaseAgeHelper.isStale(app.findStatusOrUseOldCache(applicationContext).latestVersion)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        false // network/parsing failure: don't move the app on unreliable information
                    }
                    app.app to isStale
                }
            }
            .awaitAll()
            .filter { it.second }
            .map { it.first }
            .toSet()

        val items = mutableListOf<AddRecyclerView.ItemWrapper>()

        for (displayCategory in values()) {
            val categoryApps = notInstalledApps
                .filter { displayCategory in it.displayCategory || (displayCategory == EOL && it.app in staleApps) }
                .filter { if (displayCategory == EOL) true else (EOL !in it.displayCategory && it.app !in staleApps) }
                .map { AddRecyclerView.WrappedApp(it.app) }
            if (categoryApps.isEmpty()) {
                continue
            }

            val titleText = when (displayCategory) {
                FROM_MOZILLA -> getString(R.string.add_app_activity__title_from_mozilla)
                BASED_ON_FIREFOX -> getString(R.string.add_app_activity__title_based_on_firefox)
                GOOD_PRIVACY_BROWSER -> getString(R.string.add_app_activity__title_good_privacy_browsers)
                GOOD_SECURITY_BROWSER -> getString(R.string.add_app_activity__title_good_security_browsers)
                BETTER_THAN_GOOGLE_CHROME -> getString(R.string.add_app_activity__title_better_than_google_chrome)
                OTHER -> getString(R.string.add_app_activity__title_other_applications)
                EOL -> getString(R.string.add_app_activity__title_end_of_live_browser)
            }
            items.add(AddRecyclerView.WrappedTitle(titleText))
            items.addAll(categoryApps)
        }

        withContext(Dispatchers.Main) {
            val view = findViewById<RecyclerView>(R.id.add_app_activity__recycler_view)
            view.adapter = AddRecyclerView(items, this@AddAppActivity)
            view.layoutManager = LinearLayoutManager(this@AddAppActivity)
        }
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, AddAppActivity::class.java)
            // intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

}