package com.amaze.fileutilities.home_page.welcome_helper;


import android.view.View;
import android.view.View.OnClickListener;
import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.core.view.ViewPropertyAnimatorListener;


abstract class WelcomeViewWrapper implements OnWelcomeScreenPageChangeListener {

    private final View view;
    private int firstPageIndex = 0;
    private int lastPageIndex = 0;
    private boolean animated = true;
    protected boolean isRtl = false;

    WelcomeViewWrapper(android.view.View view) {
        this.view = view;
    }

    public abstract void onPageSelected(int pageIndex, int firstPageIndex, int lastPageIndex);

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        //Not needed, empty so that subclasses can choose whether or not to implement
    }

    @Override
    public void onPageSelected(int position) {
        onPageSelected(position, firstPageIndex, lastPageIndex);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        //Not needed, empty so that subclasses can choose whether or not to implement
    }

    @Override
    public void setup(WelcomeConfiguration config) {
        firstPageIndex = config.firstPageIndex();
        lastPageIndex = config.lastViewablePageIndex();
        animated = config.getAnimateButtons();
        isRtl = config.isRtl();
    }

    protected View getView() {
        return this.view;
    }

    protected void setVisibility(boolean visible) {
        setVisibility(visible, animated);
    }

    protected void setVisibility(boolean visible, boolean animate) {
        view.setEnabled(visible);
        if (visible) {
            show(animate);
        } else {
            hide(animate);
        }
    }

    protected void hide(boolean animate) {
        if (animate) {
            hideWithAnimation();
        } else {
            hideImmediately();
        }
    }

    private void show(boolean animate) {
        if (animate) {
            showWithAnimation();
        } else {
            showImmediately();
        }
    }

    protected void hideImmediately() {
        view.setVisibility(View.INVISIBLE);
    }

    protected void showImmediately() {
        setAlpha(1f);
        view.setVisibility(android.view.View.VISIBLE);
    }

    protected void hideWithAnimation() {
        ViewPropertyAnimatorListener listener = new ViewPropertyAnimatorListener() {

            @Override
            public void onAnimationStart(@NonNull View view) {
                //Nothing needed here
            }

            @Override
            public void onAnimationEnd(@NonNull View view) {
                setAlpha(0f);
                WelcomeViewWrapper.this.view.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel(@NonNull View view) {
                setAlpha(0f);
                WelcomeViewWrapper.this.view.setVisibility(View.INVISIBLE);
            }
        };

        ViewCompat.animate(view).alpha(0f).setListener(listener).start();
    }

    protected void showWithAnimation() {

        ViewPropertyAnimatorListener listener = new ViewPropertyAnimatorListener() {

            @Override
            public void onAnimationStart(@NonNull View view) {
                WelcomeViewWrapper.this.view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(@NonNull View view) {
                setAlpha(1f);
            }

            @Override
            public void onAnimationCancel(@NonNull View view) {
                setAlpha(1f);
            }
        };

        ViewCompat.animate(view).alpha(1f).setListener(listener).start();
    }

    void setOnClickListener(OnClickListener listener) {
        view.setOnClickListener(listener);
    }

    private void setAlpha(float alpha) {
        view.setAlpha(alpha);
    }

}
