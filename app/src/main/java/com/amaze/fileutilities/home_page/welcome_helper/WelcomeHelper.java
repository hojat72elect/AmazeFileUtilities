package com.amaze.fileutilities.home_page.welcome_helper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by stephentuso on 11/15/15.
 * Class that handles showing a welcome screen
 */
public class WelcomeHelper {

    public static final int DEFAULT_WELCOME_SCREEN_REQUEST = 1;

    private static final String KEY_WELCOME_SCREEN_STARTED = "com.stephentuso.welcome.welcome_screen_started";

    private android.app.Activity mActivity;
    private Class<? extends com.amaze.fileutilities.home_page.welcome_helper.WelcomeActivity> mActivityClass;
    private boolean welcomeScreenStarted = false;

    /**
     * @param activity An activity
     * @param activityClass Class of your welcome screen. An Activity that extends {@link WelcomeActivity WelcomeActivity}
     */
    public WelcomeHelper(android.app.Activity activity, Class<? extends com.amaze.fileutilities.home_page.welcome_helper.WelcomeActivity> activityClass) {
        mActivity = activity;
        mActivityClass = activityClass;
    }

    private boolean getWelcomeScreenStarted(android.os.Bundle savedInstanceState) {
        if (!welcomeScreenStarted) {
            welcomeScreenStarted = savedInstanceState != null && savedInstanceState.getBoolean(KEY_WELCOME_SCREEN_STARTED, false);
        }
        return welcomeScreenStarted;
    }

    private boolean shouldShow(android.os.Bundle savedInstanceState) {
        return !getWelcomeScreenStarted(savedInstanceState) &&
                !WelcomeSharedPreferencesHelper.welcomeScreenCompleted(mActivity, WelcomeUtils.getKey(mActivityClass));
    }

    /**
     * Shows the welcome screen if it hasn't already been started or completed yet
     * @param savedInstanceState Saved instance state Bundle
     * @return true if the welcome screen was shown, false if it wasn't
     */
    public boolean show(android.os.Bundle savedInstanceState) {
        return show(savedInstanceState, DEFAULT_WELCOME_SCREEN_REQUEST);
    }

    /**
     * Shows the welcome screen if it hasn't already been started or completed yet
     * @param savedInstanceState Saved instance state Bundle
     * @param requestCode The request code that will be returned with the result of the welcome screen
     *                    in your Activity's onActivityResult
     * @return true if the welcome screen was shown, false if it wasn't
     */
    public boolean show(android.os.Bundle savedInstanceState, int requestCode) {
        boolean shouldShow = shouldShow(savedInstanceState);
        if (shouldShow) {
            welcomeScreenStarted = true;
            startActivity(requestCode);
        }
        return shouldShow;
    }

    /**
     * Always shows the welcome screen
     */
    public void forceShow() {
        forceShow(DEFAULT_WELCOME_SCREEN_REQUEST);
    }

    /**
     * Always show the welcome screen
     * @param requestCode The request code that will be returned with the result of the welcome screen
     *                    in your Activity's onActivityResult
     */
    public void forceShow(int requestCode) {
        startActivity(requestCode);
    }

    public void onSaveInstanceState(android.os.Bundle outState) {
        outState.putBoolean(KEY_WELCOME_SCREEN_STARTED, welcomeScreenStarted);
    }

    private void startActivity(int requestCode) {
        android.content.Intent intent = new android.content.Intent(mActivity, mActivityClass);
        mActivity.startActivityForResult(intent, requestCode);
    }

}
