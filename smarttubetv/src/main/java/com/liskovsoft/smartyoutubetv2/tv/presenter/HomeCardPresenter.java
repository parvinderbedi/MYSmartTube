package com.liskovsoft.smartyoutubetv2.tv.presenter;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Pair;

import com.liskovsoft.smartyoutubetv2.tv.R;

/**
 * MyTube: card for the Home shelves. Large 16:9 thumbnails sized so ~4 cards fit between the icon rail and the
 * right edge, with a peek of the next card so it is obvious the shelf scrolls sideways. Title is always 2 lines, metadata 1 line, no marquee (marquee redraws the focused
 * card every frame, which is wasted GPU time on Fire TV).
 */
public class HomeCardPresenter extends VideoCardPresenter {
    /** Cards visible per shelf: 4 full + a peek of the 5th */
    public static final float VISIBLE_CARDS = 4.3f;
    private Pair<Integer, Integer> mDimens;

    @Override
    protected Pair<Integer, Integer> getCardDimensPx(Context context) {
        if (mDimens == null) {
            mDimens = calculateDimens(context);
        }
        return mDimens;
    }

    public static Pair<Integer, Integer> calculateDimens(Context context) {
        Resources res = context.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        int screenWidth = Math.max(dm.widthPixels, dm.heightPixels);
        int available = screenWidth
                - res.getDimensionPixelSize(R.dimen.mytube_rail_width)
                - res.getDimensionPixelSize(R.dimen.mytube_content_padding_start)
                - res.getDimensionPixelSize(R.dimen.grid_item_horizontal_spacing) * 4;
        int width = (int) (available / VISIBLE_CARDS);
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
