package com.amaze.fileutilities.home_page.welcome_helper;

import android.content.Context;
import android.util.AttributeSet;

/**
 * An extension of {@link SimpleViewPagerIndicator SimpleViewPagerIndicator} that implements
 * the setup method of {@link OnWelcomeScreenPageChangeListener OnWelcomeScreenPageChangeListener}
 */
public class WelcomeViewPagerIndicator extends SimpleViewPagerIndicator implements OnWelcomeScreenPageChangeListener {

    public WelcomeViewPagerIndicator(Context context) {
        super(context);
    }

    public WelcomeViewPagerIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WelcomeViewPagerIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setup(WelcomeConfiguration config) {
        setTotalPages(config.viewablePageCount());
        if (config.isRtl()) {
            setRtl(true);
            if (config.getSwipeToDismiss()) {
                setPageIndexOffset(-1);
            }
        }
    }
}
