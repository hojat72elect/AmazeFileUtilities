package com.amaze.fileutilities.home_page.ui.settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import com.amaze.fileutilities.BuildConfig
import com.amaze.fileutilities.R
import com.amaze.fileutilities.home_page.database.AppDatabase
import com.amaze.fileutilities.home_page.database.PathPreferences
import com.amaze.fileutilities.home_page.database.PathPreferencesDao
import com.amaze.fileutilities.utilis.PreferencesConstants
import com.amaze.fileutilities.utilis.getAppCommonSharedPreferences
import com.amaze.fileutilities.utilis.showFolderChooserDialog
import java.io.File
import java.lang.ref.WeakReference
import kotlin.collections.set
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class PathPreferencesFragment : PreferenceFragmentCompat() {

    var log: Logger = LoggerFactory.getLogger(PathPreferencesFragment::class.java)

    private var preferencesList: PreferenceCategory? = null
    private var featureName: Int? = null
    private lateinit var dao: PathPreferencesDao
    private lateinit var sharedPrefs: SharedPreferences

    private val preferenceDbMap: MutableMap<Preference, PathPreferences> = HashMap()

    private val itemOnDeleteListener = { it: PathSwitchPreference ->
        showDeletePathDialog(it)
    }

    companion object {

        const val KEY_FEATURE_NAME = "feature_name"

        fun newInstance(featureName: Int): PathPreferencesFragment {
            val pathPrefsFragment = PathPreferencesFragment()
            pathPrefsFragment.apply {
                val bundle = Bundle()
                bundle.putInt(KEY_FEATURE_NAME, featureName)
                arguments = bundle
            }
            return pathPrefsFragment
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.path_prefs, rootKey)
        featureName = arguments?.getInt(KEY_FEATURE_NAME)
        dao = AppDatabase.getInstance(requireContext()).pathPreferencesDao()
        sharedPrefs = requireContext().getAppCommonSharedPreferences()

        findPreference<Preference>("add_pref_path")?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                showCreateDialog()
                true
            }
        preferencesList = findPreference("prefs_list")
        if (featureName!! != PathPreferences.FEATURE_AUDIO_PLAYER) {
            preferencesList?.order = 2
            val enablePreference = addEnablePreference()
            preferenceScreen.addPreference(enablePreference)
            if (PathPreferences.MIGRATION_PREF_MAP.containsKey(featureName)) {
                val resetPreference = addResetAnalysisPreference()
                preferenceScreen.addPreference(resetPreference)
                resetPreference.dependency = PathPreferences.getEnablePreferenceKey(featureName!!)
            }
            preferencesList?.dependency = PathPreferences.getEnablePreferenceKey(featureName!!)
        } else {
            preferencesList?.title = resources.getString(R.string.paths_excluded)
        }
        reload()
    }

    private fun addEnablePreference(): Preference {
        val preference = CheckBoxPreference(preferenceScreen.context)
        preference.title = getString(R.string.show_analysis)
        preference.key = PathPreferences.getEnablePreferenceKey(featureName!!)
        preference.setDefaultValue(
            sharedPrefs.getBoolean(
                PathPreferences
                    .getEnablePreferenceKey(featureName!!),
                PreferencesConstants.DEFAULT_ENABLED_ANALYSIS
            )
        )
        val onChange = Preference.OnPreferenceChangeListener { _, newValue ->
            sharedPrefs.edit().putBoolean(
                PathPreferences
                    .getEnablePreferenceKey(featureName!!),
                newValue as Boolean
            ).apply()
            true
        }
        preference.onPreferenceChangeListener = onChange
        preference.order = 0
        if ((
                    featureName!! == PathPreferences.FEATURE_ANALYSIS_IMAGE_FEATURES ||
                            featureName!! == PathPreferences.FEATURE_ANALYSIS_MEME
                    ) &&
            BuildConfig.IS_VERSION_FDROID
        ) {
            preference.isEnabled = false
            preference.isChecked = false
        }
        return preference
    }

    private fun addResetAnalysisPreference(): Preference {
        val preference = Preference(preferenceScreen.context)
        preference.title = getString(R.string.reanalyse)
        preference.summary = getString(R.string.reanalyse_hint)
        preference.key = PathPreferences.getAnalysisMigrationPreferenceKey(featureName!!)
        val onClick = Preference.OnPreferenceClickListener { _ ->
            showReanalyseDialog()
            true
        }
        preference.onPreferenceClickListener = onClick
        preference.order = 1
        return preference
    }

    private fun reload() {
        for (p in preferenceDbMap) {
            preferencesList?.removePreference(p.key)
        }

        preferenceDbMap.clear()
        val preferences = dao.findByFeature(featureName!!)
        preferences.forEach {
            val prefSwitch = PathSwitchPreference(
                requireContext(), null,
                itemOnDeleteListener
            )
            preferenceDbMap[prefSwitch] = it
            prefSwitch.summary = it.path
            preferencesList?.addPreference(prefSwitch)
        }
    }

    private fun showReanalyseDialog() {
        val dialog = AlertDialog.Builder(requireContext()).setTitle(R.string.reanalyse)
            .setPositiveButton(R.string.confirm) { dialog, _ ->
                val context: WeakReference<Context> = try {
                    WeakReference(requireContext())
                } catch (e: IllegalStateException) {
                    log.warn("failed to get context", e)
                    WeakReference(null)
                }
                PathPreferences.deleteAnalysisData(
                    ArrayList(preferenceDbMap.values),
                    context
                )
                dialog.dismiss()
            }
            .setNegativeButton(
                R.string.cancel
            ) { dialog, _ -> dialog.dismiss() }
            .create()
        dialog.show()
    }

    private fun showDeletePathDialog(prefSwitch: PathSwitchPreference) {
        val dialog = AlertDialog.Builder(requireContext()).setTitle(R.string.delete_preference)
            .setPositiveButton(R.string.confirm) { dialog, _ ->
                val prefDb = preferenceDbMap[prefSwitch]
                prefDb?.let { dao.delete(it) }
                preferencesList?.removePreference(prefSwitch)
                preferenceDbMap.remove(prefSwitch)
                dialog.dismiss()
            }
            .setNegativeButton(
                R.string.cancel
            ) { dialog, _ -> dialog.dismiss() }
            .create()
        dialog.show()
    }

    private fun showWarningOverridePathDialog(callback: (approved: Boolean) -> Unit) {
        val dialog = AlertDialog.Builder(requireContext()).setTitle(R.string.delete_preference)
            .setMessage(R.string.delete_pref_override_message)
            .setPositiveButton(R.string.confirm) { dialog, _ ->
                callback.invoke(true)
                dialog.dismiss()
            }
            .setNegativeButton(
                R.string.cancel
            ) { dialog, _ ->
                callback.invoke(false)
                dialog.dismiss()
            }
            .create()
        dialog.show()
    }

    private fun showCreateDialog() {
        requireContext().showFolderChooserDialog { file ->
            val pathPreferences = PathPreferences(
                file.path, featureName!!,
                featureName == PathPreferences.FEATURE_AUDIO_PLAYER
            )
            var overrideExisting = false
            dao.findByFeature(featureName!!).forEach { pathPref ->
                if (file.path.contains(pathPref.path) || pathPref.path.contains(file.path)) {
                    // new analysis path contains existing path preference, clear existing analysis
                    overrideExisting = true
                    showWarningOverridePathDialog {
                        if (it) {
                            val context: WeakReference<Context> = try {
                                WeakReference(requireContext())
                            } catch (e: IllegalStateException) {
                                log.warn("failed to get context", e)
                                WeakReference(null)
                            }
                            PathPreferences.deleteAnalysisData(
                                arrayListOf(pathPref),
                                context
                            )
                            dao.delete(pathPref)
                            addNewPathAndPreference(pathPreferences, file)
                        }
                    }
                }
            }
            if (!overrideExisting) {
                addNewPathAndPreference(pathPreferences, file)
            }
        }
    }

    private fun addNewPathAndPreference(pathPreferences: PathPreferences, file: File) {
        dao.insert(pathPreferences)
        val prefSwitch = PathSwitchPreference(
            requireContext(), null,
            itemOnDeleteListener
        )
        preferenceDbMap[prefSwitch] = pathPreferences
        prefSwitch.summary = file.path
        preferencesList?.addPreference(prefSwitch)
    }
}
