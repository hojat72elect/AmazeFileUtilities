package com.amaze.fileutilities.home_page

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.speech.RecognizerIntent
import android.view.MotionEvent
import android.view.View
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.ActionBar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.work.ExistingPeriodicWorkPolicy
import com.amaze.fileutilities.BuildConfig
import com.amaze.fileutilities.R
import com.amaze.fileutilities.WifiP2PActivity
import com.amaze.fileutilities.databinding.ActivityMainActionbarBinding
import com.amaze.fileutilities.databinding.ActivityMainActionbarItemSelectedBinding
import com.amaze.fileutilities.databinding.ActivityMainActionbarSearchBinding
import com.amaze.fileutilities.databinding.ActivityMainBinding
import com.amaze.fileutilities.home_page.database.Trial
import com.amaze.fileutilities.home_page.ui.AggregatedMediaFileInfoObserver
import com.amaze.fileutilities.home_page.ui.files.FilesFragment
import com.amaze.fileutilities.home_page.ui.files.FilesViewModel
import com.amaze.fileutilities.home_page.ui.files.SearchListFragment
import com.amaze.fileutilities.home_page.ui.files.TrialValidationApi
import com.amaze.fileutilities.home_page.ui.options.Billing
import com.amaze.fileutilities.home_page.ui.settings.PreferenceActivity
import com.amaze.fileutilities.home_page.ui.transfer.TransferFragment
import com.amaze.fileutilities.home_page.welcome_helper.WelcomeHelper
import com.amaze.fileutilities.utilis.ItemsActionBarFragment
import com.amaze.fileutilities.utilis.PreferencesConstants
import com.amaze.fileutilities.utilis.UpdateChecker
import com.amaze.fileutilities.utilis.Utils
import com.amaze.fileutilities.utilis.getAppCommonSharedPreferences
import com.amaze.fileutilities.utilis.hideFade
import com.amaze.fileutilities.utilis.hideTranslateY
import com.amaze.fileutilities.utilis.showFade
import com.amaze.fileutilities.utilis.showToastInCenter
import com.amaze.fileutilities.utilis.showToastOnBottom
import com.amaze.fileutilities.utilis.showTranslateY
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Date
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MainActivity :
    WifiP2PActivity(),
    AggregatedMediaFileInfoObserver {

    var log: Logger = LoggerFactory.getLogger(MainActivity::class.java)

    private lateinit var binding: ActivityMainBinding
    private lateinit var actionBarBinding: ActivityMainActionbarBinding
    private lateinit var searchActionBarBinding: ActivityMainActionbarSearchBinding
    private lateinit var selectedItemActionBarBinding: ActivityMainActionbarItemSelectedBinding

    //    var showSearchFragment = false
    private lateinit var viewModel: FilesViewModel
    private var isOptionsVisible = false
    private var welcomeScreen: WelcomeHelper? = null
    private var didShowWelcomeScreen = true

    // refers to com.google.android.play.core.install.model.ActivityResult.RESULT_IN_APP_UPDATE_FAILED
    private val RESULT_IN_APP_UPDATE_FAILED = 1

    companion object {
        private const val VOICE_REQUEST_CODE = 1000
        const val KEY_INTENT_AUDIO_PLAYER = "audio_player_intent"
        const val UPDATE_REQUEST_CODE = 123234
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this).get(FilesViewModel::class.java)
        setTheme(R.style.Theme_AmazeFileUtilities)
        super.onCreate(savedInstanceState)
        welcomeScreen = WelcomeHelper(this, WelcomeScreen::class.java)
        if (!welcomeScreen!!.show(savedInstanceState)) {
            didShowWelcomeScreen = false
            invokePermissionCheck()
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        actionBarBinding = ActivityMainActionbarBinding.inflate(layoutInflater)
        searchActionBarBinding = ActivityMainActionbarSearchBinding.inflate(layoutInflater)
        selectedItemActionBarBinding = ActivityMainActionbarItemSelectedBinding
            .inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setBackgroundDrawable(
            ColorDrawable(
                resources
                    .getColor(R.color.navy_blue)
            )
        )
        supportActionBar?.customView = actionBarBinding.root
        supportActionBar?.elevation = 0f
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (isOptionsVisible) {
                isOptionsVisible = !isOptionsVisible
                invalidateOptionsTabs()
            }
            when (destination.id) {
                R.id.navigation_analyse ->
                    actionBarBinding.title.text = resources.getString(R.string.title_analyse)

                R.id.navigation_files ->
                    actionBarBinding.title.text = resources.getString(R.string.title_utilities)

                R.id.navigation_transfer ->
                    actionBarBinding.title.text = resources.getString(R.string.title_transfer)
            }
        }
        navView.setupWithNavController(navController)

        actionBarBinding.searchActionBar.setOnClickListener {
            showSearchFragment()
        }
        actionBarBinding.optionsImage.setOnClickListener {
            isOptionsVisible = !isOptionsVisible
            invalidateOptionsTabs()
        }
        binding.optionsOverlay.setOnClickListener {
            isOptionsVisible = !isOptionsVisible
            invalidateOptionsTabs()
        }
        binding.aboutText.setOnClickListener {
            showAboutActivity(
                isTrialExpired = false,
                isTrialInactive = false,
                isNotConnected = false
            )
        }
        binding.settingsText.setOnClickListener {
            val intent = Intent(this, PreferenceActivity::class.java)
            intent.putExtra(PreferenceActivity.KEY_IS_SETTINGS, true)
            startActivity(intent)
            isOptionsVisible = !isOptionsVisible
            invalidateOptionsTabs()
        }

        viewModel.initAndFetchPathPreferences().observe(this) { pathPreferences ->
            viewModel.usedImagesSummaryTransformations().observe(
                this
            ) { mediaInfoStorageSummaryPair ->
                viewModel.initAnalysisMigrations.observe(this) {
                    if (it) {
                        mediaInfoStorageSummaryPair?.second.let { list ->
                            list?.run {
                                val mediaFileInfoList = ArrayList(this)
                                if (!BuildConfig.IS_VERSION_FDROID) {
                                    viewModel.analyseImageFeatures(
                                        mediaFileInfoList,
                                        pathPreferences
                                    )
                                    viewModel.analyseMemeImages(
                                        mediaFileInfoList,
                                        pathPreferences
                                    )
                                }
                                viewModel.analyseBlurImages(
                                    mediaFileInfoList,
                                    pathPreferences
                                )
                                viewModel.analyseLowLightImages(
                                    mediaFileInfoList,
                                    pathPreferences
                                )
                                viewModel.analyseSimilarImages(
                                    mediaFileInfoList,
                                    pathPreferences
                                )
                            }
                        }
                    }
                }
            }

            val prefs = this.getAppCommonSharedPreferences()
            val searchIdx = prefs.getInt(
                PreferencesConstants.KEY_SEARCH_DUPLICATES_IN,
                PreferencesConstants.DEFAULT_SEARCH_DUPLICATES_IN
            )

            if (searchIdx != PreferencesConstants.DEFAULT_SEARCH_DUPLICATES_IN) {
                viewModel.analyseInternalStorage(
                    searchIdx ==
                            PreferencesConstants.VAL_SEARCH_DUPLICATES_INTERNAL_DEEP
                )
            } else {
                observeMediaInfoLists { isLoading, aggregatedFiles ->
                    if (!isLoading && aggregatedFiles != null) {
                        viewModel.analyseMediaStoreFiles(aggregatedFiles)
                    }
                }
            }
        }

        if (!didShowWelcomeScreen) {
            UpdateChecker.checkForAppUpdates(this)
            viewModel.getUniqueId().observe(this) { deviceId ->
                if (deviceId != null) {
                    viewModel.checkInternetConnection(30000).observe(this) {
                        viewModel.validateTrial(deviceId, it) { trialResponse ->
                            handleValidateTrial(trialResponse)
                        }
                    }
                }
            }
            UpdateChecker.shouldRateApp(this)
        } else {
            // add install time in preferences
            getAppCommonSharedPreferences()
                .edit().putLong(PreferencesConstants.KEY_INSTALL_DATE, Date().time)
                .apply()
        }

        Utils.scheduleQueryAppSizeWorker(this, ExistingPeriodicWorkPolicy.KEEP)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        welcomeScreen?.onSaveInstanceState(outState)
    }

    override fun onPause() {
        super.onPause()
        UpdateChecker.unregisterListener()
        getFilesModel().resetTrashBinConfig()
    }

    override fun getTransferFragment(): TransferFragment? {
        val fragment = getFragmentAtFrame()
        return if (fragment is NavHostFragment) {
            if (fragment.childFragmentManager.fragments.size == 1) {
                val childFragment = fragment.childFragmentManager.fragments[0]
                if (childFragment is TransferFragment) {
                    return childFragment
                }
            }
            null
        } else {
            null
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == VOICE_REQUEST_CODE && resultCode == RESULT_OK) {
            // Populate the wordsList with the String values the recognition engine thought it heard
            val matches: ArrayList<String>? = data?.getStringArrayListExtra(
                RecognizerIntent.EXTRA_RESULTS
            )
            matches?.let {
                if (matches.size > 0) {
                    searchActionBarBinding.actionBarEditText.setText(matches[0])
                }
            }
        } else if (requestCode == UPDATE_REQUEST_CODE) {
            when (resultCode) {
                RESULT_OK -> {
                    log.info("user accepted the update")
                    //  handle user's approval
                    showToastOnBottom(getString(R.string.app_updated))
                }

                RESULT_CANCELED -> {
                    //  handle user's rejection
                    log.info("user rejected the update")
                    showToastOnBottom(getString(R.string.update_cancelled))
                }

                RESULT_IN_APP_UPDATE_FAILED -> {
                    // if you want to request the update again just call checkUpdate()
                    //  handle update failure
                    log.info("failed to update the app")
                    showToastOnBottom(getString(R.string.failed_to_update_app))
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val fragment = getFragmentAtFrame()
        if (selectedItemActionBarBinding.root.isVisible &&
            !searchActionBarBinding.root.isVisible
        ) {
            if (!actionBarBinding.root.isVisible) {
                var abstractListFragment: ItemsActionBarFragment? = null
                fragment?.childFragmentManager?.fragments?.forEach {
                    if (it is ItemsActionBarFragment) {
                        abstractListFragment = it
                    }
                }
                if (abstractListFragment != null) {
                    invalidateSelectedActionBar(
                        false, abstractListFragment!!.hideActionBarOnClick(),
                        abstractListFragment!!.handleBackPressed()
                    )
                    abstractListFragment!!.handleBackPressed().invoke()
                } else {
                    super.onBackPressed()
                }
            } else {
                var didShowTransfer = false
                var isTransferFragment = false
                fragment?.childFragmentManager?.fragments?.forEach {
                    if (it is TransferFragment) {
                        isTransferFragment = true
                        // check if transfer in progress, avoid back press if it is
                        if (it.getTransferViewModel().isTransferInProgress) {
                            didShowTransfer = true
                            it.warnTransferInProgress {
                                super.onBackPressed()
                            }
                        }
                    }
                }
                if (!didShowTransfer || !isTransferFragment) {
                    super.onBackPressed()
                }
            }
        } else if (searchActionBarBinding.root.isVisible) {
            val searchFragment = supportFragmentManager
                .findFragmentByTag(SearchListFragment.FRAGMENT_TAG)
            val transaction = supportFragmentManager.beginTransaction()
            searchFragment?.let {
                transaction.remove(searchFragment)
                transaction.commit()
            }
        } else {
            if (fragment?.childFragmentManager?.fragments?.isNotEmpty() == true &&
                fragment.childFragmentManager
                    .fragments[fragment.childFragmentManager.fragments.size - 1] is FilesFragment
            ) {
                exit()
            } else {
                super.onBackPressed()
            }
        }
        if (isOptionsVisible) {
            isOptionsVisible = !isOptionsVisible
            invalidateOptionsTabs()
        }
    }

    private fun exit() {
        val confirmBeforeExitPref = getAppCommonSharedPreferences().getBoolean(
            PreferencesConstants.KEY_CONFIRM_BEFORE_EXIT,
            PreferencesConstants.DEFAULT_CONFIRM_BEFORE_EXIT
        )
        if (!confirmBeforeExitPref || getFilesModel().backPressedToExitOnce) {
            finish()
        } else {
            getFilesModel().backPressedToExitOnce = true
            showToastInCenter(getString(R.string.press_again))
            object : CountDownTimer(2000, 2000) {
                override fun onTick(millisUntilFinished: Long) {
                    // do nothing
                }

                override fun onFinish() {
                    getFilesModel().backPressedToExitOnce = false
                }
            }.start()
        }
    }

    private fun handleValidateTrial(trialResponse: TrialValidationApi.TrialResponse) {
        this@MainActivity.runOnUiThread {
            if (trialResponse.subscriptionStatus
                == Trial.SUBSCRIPTION_STATUS_DEFAULT
            ) {
                log.debug("user not subscribed {}", trialResponse)
                if (trialResponse.isNotConnected) {
                    val notConnectedCount = getAppCommonSharedPreferences()
                        .getInt(PreferencesConstants.KEY_NOT_CONNECTED_TRIAL_COUNT, 0)
                    getAppCommonSharedPreferences().edit()
                        .putInt(
                            PreferencesConstants.KEY_NOT_CONNECTED_TRIAL_COUNT,
                            notConnectedCount + 1
                        ).apply()
                    if (notConnectedCount > PreferencesConstants.VAL_THRES_NOT_CONNECTED_TRIAL) {
                        showAboutActivity(
                            isTrialExpired = false,
                            isTrialInactive = false,
                            isNotConnected = true
                        )
                    }
                    log.warn("internet not connected count $notConnectedCount")
                } else {
                    getAppCommonSharedPreferences().edit()
                        .putInt(PreferencesConstants.KEY_NOT_CONNECTED_TRIAL_COUNT, 0).apply()
                    getAppCommonSharedPreferences().edit()
                        .putInt(PreferencesConstants.KEY_NOT_CONNECTED_SUBSCRIBED_COUNT, 0).apply()
                    when (trialResponse.getTrialStatusCode()) {
                        TrialValidationApi.TrialResponse.TRIAL_ACTIVE -> {
                            // check if it's first day or last day
                            if (trialResponse.isNewSignup) {
                                Utils.buildTrialStartedDialog(
                                    this,
                                    trialResponse.trialDaysLeft
                                ).create().show()
                            } else if (trialResponse.isLastDay) {
                                Utils.buildLastTrialDayDialog(this) {
                                    // wants to subscribe
                                    Billing.getInstance(
                                        this
                                    )?.initiatePurchaseFlow()
                                }.create().show()
                            }
                        }

                        TrialValidationApi.TrialResponse.TRIAL_EXPIRED,
                        TrialValidationApi.TrialResponse.TRIAL_UNOFFICIAL -> {
                            showAboutActivity(
                                isTrialExpired = true,
                                isTrialInactive = false,
                                isNotConnected = false
                            )
                        }

                        TrialValidationApi.TrialResponse.TRIAL_INACTIVE -> {
                            showAboutActivity(
                                isTrialExpired = false,
                                isTrialInactive = true,
                                isNotConnected = false
                            )
                        }
                    }
                }
            } else {
                log.debug("user subscribed {}", trialResponse)
                if (trialResponse.isNotConnected) {
                    val notConnectedCount = getAppCommonSharedPreferences()
                        .getInt(PreferencesConstants.KEY_NOT_CONNECTED_SUBSCRIBED_COUNT, 0)
                    getAppCommonSharedPreferences().edit()
                        .putInt(
                            PreferencesConstants.KEY_NOT_CONNECTED_SUBSCRIBED_COUNT,
                            notConnectedCount + 1
                        ).apply()
                    if (notConnectedCount > PreferencesConstants
                            .VAL_THRES_NOT_CONNECTED_SUBSCRIBED
                    ) {
                        showAboutActivity(
                            isTrialExpired = false,
                            isTrialInactive = false,
                            isNotConnected = true
                        )
                    }
                    log.warn("subscribed and internet not connected count $notConnectedCount")
                } else {
                    getAppCommonSharedPreferences().edit()
                        .putInt(PreferencesConstants.KEY_NOT_CONNECTED_TRIAL_COUNT, 0).apply()
                    getAppCommonSharedPreferences().edit()
                        .putInt(PreferencesConstants.KEY_NOT_CONNECTED_SUBSCRIBED_COUNT, 0).apply()
                }
            }
        }
    }

    private fun getFragmentAtFrame(): Fragment? {
        return supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main)
    }

    fun setCustomTitle(title: String) {
        if (::actionBarBinding.isInitialized) {
            actionBarBinding.title.text = title
        }
    }

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    fun invalidateSearchBar(showSearch: Boolean): AutoCompleteTextView? {
        if (::searchActionBarBinding.isInitialized) {
            searchActionBarBinding.run {
                return if (showSearch) {
                    actionBarBinding.root.hideFade(200)
                    searchActionBarBinding.root.showFade(300)
                    supportActionBar?.customView = root
                    backActionBar.setOnClickListener {
                        onBackPressed()
                    }
                    actionBarEditText.setOnTouchListener { _, event ->
                        if (event.action == MotionEvent.ACTION_UP) {
                            if (event.rawX >= (
                                        actionBarEditText.right -
                                                actionBarEditText.compoundDrawables[2]
                                                    .bounds.width()
                                        )
                            ) {
                                actionBarEditText.setText("")
                                true
                            }
                        }
                        false
                    }
                    voiceActionBar.setOnClickListener {
                        startVoiceRecognitionActivity()
                    }
                    actionBarEditText
                } else {
                    actionBarEditText.setText("")
                    actionBarEditText.setAdapter(null)
                    searchActionBarBinding.root.hideFade(200)
                    actionBarBinding.root.showFade(300)
                    supportActionBar?.customView = actionBarBinding.root
                    actionBarEditText.setOnEditorActionListener(null)
                    null
                }
            }
        }
        return null
    }

    @SuppressLint("SetTextI18n")
    fun invalidateSelectedActionBar(
        doShow: Boolean,
        hideActionBarOnClick: Boolean,
        onBackPressed: () -> Unit
    ): View? {
        if (::selectedItemActionBarBinding.isInitialized) {
            selectedItemActionBarBinding.run {
                Utils.marqueeAfterDelay(2000, selectedItemActionBarBinding.title)
                return if (doShow) {
                    actionBarBinding.root.hideFade(200)
                    selectedItemActionBarBinding.root.showFade(300)
                    supportActionBar?.customView = root
                    backActionBar.setOnClickListener {
                        onBackPressed.invoke()
                        if (hideActionBarOnClick) {
                            invalidateSelectedActionBar(
                                false, hideActionBarOnClick,
                                onBackPressed
                            )
                        } else {
                            onBackPressed()
                        }
                    }
                    title.text = "0"
                    selectedItemActionBarBinding.root
                } else {
                    title.text = "0"
                    searchActionBarBinding.root.hideFade(200)
                    actionBarBinding.root.showFade(300)
                    supportActionBar?.customView = actionBarBinding.root
                    null
                }
            }
        }
        return null
    }

    fun invalidateBottomBar(doShow: Boolean) {
        if (::binding.isInitialized) {
            if (doShow) {
                binding.navView.showTranslateY(500)
            } else {
                binding.navView.hideTranslateY(500)
            }
        }
    }

    private fun showAboutActivity(
        isTrialExpired: Boolean,
        isTrialInactive: Boolean,
        isNotConnected: Boolean
    ) {
        val intent = Intent(this, PreferenceActivity::class.java)
        intent.putExtra(PreferenceActivity.KEY_IS_SETTINGS, false)
        intent.putExtra(PreferenceActivity.KEY_IS_TRIAL_EXPIRED, isTrialExpired)
        intent.putExtra(PreferenceActivity.KEY_IS_TRIAL_INACTIVE, isTrialInactive)
        intent.putExtra(PreferenceActivity.KEY_NOT_CONNECTED, isNotConnected)
        startActivity(intent)
        isOptionsVisible = !isOptionsVisible
        invalidateOptionsTabs()
    }

    private fun showSearchFragment() {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(
            R.id.nav_host_fragment_activity_main, SearchListFragment(),
            SearchListFragment.FRAGMENT_TAG
        )
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun startVoiceRecognitionActivity() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Voice recognition Demo...")
        try {
            startActivityForResult(intent, VOICE_REQUEST_CODE)
        } catch (e: ActivityNotFoundException) {
            log.warn("voice recognition activity not found", e)
            this.showToastInCenter(resources.getString(R.string.unsupported_operation))
        }
    }

    private fun invalidateOptionsTabs() {
        binding.run {
            if (!isOptionsVisible) {
                optionsOverlay.hideFade(400)
                optionsRoot.hideFade(200)
                optionsRoot.visibility = View.GONE
            } else {
                optionsOverlay.showFade(500)
                optionsRoot.showFade(200)
                optionsRoot.visibility = View.VISIBLE
            }
        }
    }

    override fun getFilesModel(): FilesViewModel {
        return viewModel
    }

    override fun lifeCycleOwner(): LifecycleOwner {
        return this
    }
}
