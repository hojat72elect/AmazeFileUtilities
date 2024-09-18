package com.amaze.fileutilities.home_page.welcome_helper;

import androidx.viewpager.widget.ViewPager;

/**
 * Implemented by library components to respond to page scroll events
 * and initial setup
 * <p>
 * Created by stephentuso on 11/16/15.
 */
/* package */ interface OnWelcomeScreenPageChangeListener extends ViewPager.OnPageChangeListener {
    void setup(WelcomeConfiguration config);
}

