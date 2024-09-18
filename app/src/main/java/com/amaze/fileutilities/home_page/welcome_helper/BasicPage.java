package com.amaze.fileutilities.home_page.welcome_helper;

import android.content.Context;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

/**
 * A page with a large image, header, and description.
 */
public class BasicPage extends WelcomePage<com.amaze.fileutilities.home_page.welcome_helper.BasicPage> {

    private int drawableResId;
    private String title;
    private String description;
    private boolean showParallax = true;
    private String headerTypefacePath = null;
    private String descriptionTypefacePath = null;
    private int headerColor = WelcomeUtils.NO_COLOR_SET;
    private int descriptionColor = WelcomeUtils.NO_COLOR_SET;

    /**
     * A page with a large image, header, and description
     *
     * @param drawableResId Resource id of drawable to show
     * @param title         Title, shown in large font
     * @param description   Description, shown beneath title
     */
    public BasicPage(@DrawableRes int drawableResId, String title, String description) {
        this.drawableResId = drawableResId;
        this.title = title;
        this.description = description;
    }

    /**
     * Whether or not a parallax effect should be shown.
     * If true, the image will move at a faster rate than the text
     * <p>
     * Default: true
     *
     * @param showParallax If parallax effect should be shown
     * @return This BasicPage object to allow method calls to be chained
     */
    public BasicPage parallax(boolean showParallax) {
        this.showParallax = showParallax;
        return this;
    }

    /**
     * Set the typeface of the header
     *
     * @param typefacePath The path to a typeface in the assets folder
     * @return This BasicPage object to allow method calls to be chained
     */
    public BasicPage headerTypeface(String typefacePath) {
        this.headerTypefacePath = typefacePath;
        return this;
    }

    /**
     * Set the typeface of the description
     *
     * @param typefacePath The path to a typeface in the assets folder
     * @return This BasicPage object to allow method calls to be chained
     */
    public BasicPage descriptionTypeface(String typefacePath) {
        this.descriptionTypefacePath = typefacePath;
        return this;
    }

    /**
     * Set the color of the header
     *
     * @param color Color int
     * @return This BasicPage object to allow method calls to be chained
     */
    public BasicPage headerColor(@ColorInt int color) {
        this.headerColor = color;
        return this;
    }

    /**
     * Set the color of the header from a color resource id
     *
     * @param context  Context used to resolve color
     * @param colorRes Resource id of color to set
     * @return This BasicPage object to allow method calls to be chained
     */
    public BasicPage headerColorResource(Context context, @ColorRes int colorRes) {
        this.headerColor = ContextCompat.getColor(context, colorRes);
        return this;
    }

    /**
     * Set the color of the header
     *
     * @param color Color int
     * @return This BasicPage object to allow method calls to be chained
     */
    public BasicPage descriptionColor(@ColorInt int color) {
        this.descriptionColor = color;
        return this;
    }

    /**
     * Set the color of the description from a color resource id
     *
     * @param context  Context used to resolve color
     * @param colorRes Resource id of color to set
     * @return This BasicPage object to allow method calls to be chained
     */
    public com.amaze.fileutilities.home_page.welcome_helper.BasicPage descriptionColorResource(android.content.Context context, @ColorRes int colorRes) {
        this.descriptionColor = androidx.core.content.ContextCompat.getColor(context, colorRes);
        return this;
    }

    /* Package local getters for testing */

    /* package */ int getDrawableResId() {
        return drawableResId;
    }

    /* package */ String getTitle() {
        return title;
    }

    /* package */ String getDescription() {
        return description;
    }

    /* package */ boolean getShowParallax() {
        return showParallax;
    }

    /* package */ String getHeaderTypefacePath() {
        return headerTypefacePath;
    }

    /* package */ String getDescriptionTypefacePath() {
        return descriptionTypefacePath;
    }

    /* package */ int getHeaderColor() {
        return headerColor;
    }

    /* package */ int getDescriptionColor() {
        return descriptionColor;
    }

    @Override
    public void setup(WelcomeConfiguration config) {
        super.setup(config);

        if (this.headerTypefacePath == null) {
            headerTypeface(config.getDefaultHeaderTypefacePath());
        }

        if (this.descriptionTypefacePath == null) {
            descriptionTypeface(config.getDefaultDescriptionTypefacePath());
        }

    }

    @Override
    public Fragment fragment() {
        // TODO: So many arguments...refactor?
        return WelcomeBasicFragment.newInstance(drawableResId,
                title,
                description,
                showParallax,
                headerTypefacePath,
                descriptionTypefacePath,
                headerColor,
                descriptionColor);
    }

}
