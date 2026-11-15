package com.liskovsoft.smartyoutubetv2.tv.presenter;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Pair;

import com.liskovsoft.smartyoutubetv2.tv.R;

/**
 * MyTube: card for the Home shelves. Large 16:9 thumbnails sized so ~4 cards fit between the icon rail and the
 * right edge, with a peek of the next card so it is obvious the shelf scrolls sideways. Title is always 2 lines, metadata 1 line, no marquee (marquee redraws the focused
 * card every frame, which is wasted GPU time on Fire TV).
 */
public class HomeCardPresenter extends VideoCardPresenter {
    private Pair<Integer, Integer> mDimens;

    @Override
    protected Pair<Integer, Integer> getCardDimensPx(Context context) {
        if (mDimens == null) {
            mDimens = calculateDimens(context);
        }
        return mDimens;
    }

    /** Card width as a fraction of the screen width (official TV app, regular shelves: 27.5%) */
    public static final float CARD_WIDTH_FRACTION = 0.275f;

    public static Pair<Integer, Integer> calculateDimens(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int screenWidth = Math.max(dm.widthPixels, dm.heightPixels);
        int width = Math.round(screenWidth * CARD_WIDTH_FRACTION);
        int height = width * 9 / 16;
        return new Pair<>(width, height);
    }

    @Override
    protected boolean isCardMultilineTitleEnabled(Context context) {
        return true;
    }

    @Override
    protected boolean isCardMultilineSubtitleEnabled(Context context) {
        return false;
    }

    @Override
    protected boolean isCardTextAutoScrollEnabled(Context context) {
        return false;
    }
}
