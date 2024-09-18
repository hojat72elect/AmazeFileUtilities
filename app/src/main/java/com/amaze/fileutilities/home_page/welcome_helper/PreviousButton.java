package com.amaze.fileutilities.home_page.welcome_helper;

import android.view.View;

/**
 * Created by stephentuso on 1/30/16.
 */
/* package */ class PreviousButton extends WelcomeViewWrapper {

    private boolean shouldShow = false;

    public PreviousButton(android.view.View button) {
        super(button);
    }

    @Override
    public void setup(WelcomeConfiguration config) {
        super.setup(config);
        this.shouldShow = config.getShowPrevButton();
    }

    @Override
    public void onPageSelected(int pageIndex, int firstPageIndex, int lastPageIndex) {
        setVisibility(shouldShow && pageIndex != firstPageIndex);
    }


}
