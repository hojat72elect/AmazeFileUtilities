package com.amaze.fileutilities.home_page.welcome_helper;


import android.os.Bundle;
import android.view.ViewGroup;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.amaze.fileutilities.R;
import android.view.LayoutInflater;
import android.view.View;

/**
 * A simple fragment that shows an image and a title.
 * Use the {@link WelcomeTitleFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WelcomeTitleFragment extends Fragment implements WelcomePage.OnChangeListener {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_DRAWABLE_ID = "drawable_id";
    private static final String ARG_TITLE = "title";
    private static final String ARG_SHOW_ANIM = "show_anim";
    private static final String ARG_TYPEFACE_PATH = "typeface_path";
    private static final String ARG_TITLE_COLOR = "title_color";

    private boolean showParallaxAnim = true;
    private android.widget.TextView titleView = null;
    private android.widget.ImageView imageView = null;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of WelcomeTitleFragment.
     */
    public static com.amaze.fileutilities.home_page.welcome_helper.WelcomeTitleFragment newInstance(
            @DrawableRes int resId,
            String title,
            boolean showParallaxAnim,
            @Nullable String typefacePath,
            @ColorInt int titleColor) {


        WelcomeTitleFragment fragment = new WelcomeTitleFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_DRAWABLE_ID, resId);
        args.putString(ARG_TITLE, title);
        args.putBoolean(ARG_SHOW_ANIM, showParallaxAnim);
        args.putString(ARG_TYPEFACE_PATH, typefacePath);
        args.putInt(ARG_TITLE_COLOR, titleColor);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wel_fragment_title, container, false);
        imageView = view.findViewById(R.id.wel_image);
        titleView = view.findViewById(R.id.wel_title);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();

        if (args == null)
            return;

        int drawableId = args.getInt(ARG_DRAWABLE_ID);
        String title = args.getString(ARG_TITLE);

        imageView.setImageResource(drawableId);
        titleView.setText(title);

        int titleColor = args.getInt(ARG_TITLE_COLOR, WelcomeUtils.NO_COLOR_SET);
        if (titleColor != WelcomeUtils.NO_COLOR_SET)
            titleView.setTextColor(titleColor);

        showParallaxAnim = args.getBoolean(ARG_SHOW_ANIM, showParallaxAnim);

        WelcomeUtils.setTypeface(titleView, args.getString(ARG_TYPEFACE_PATH), getActivity());
    }

    @Override
    public void onWelcomeScreenPageScrolled(int pageIndex, float offset, int offsetPixels) {
        if (showParallaxAnim && imageView != null) {
            imageView.setTranslationX(-offsetPixels * 0.8f);
        }
    }

    @Override
    public void onWelcomeScreenPageSelected(int pageIndex, int selectedPageIndex) {
        //Not used
    }

    @Override
    public void onWelcomeScreenPageScrollStateChanged(int pageIndex, int state) {
        //Not used
    }
}
