package com.liskovsoft.smartyoutubetv2.tv.presenter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.liskovsoft.sharedutils.helpers.Helpers;
import com.liskovsoft.sharedutils.mylogger.Log;
import com.liskovsoft.smartyoutubetv2.common.app.models.data.Video;
import com.liskovsoft.smartyoutubetv2.common.prefs.MainUIData;
import com.liskovsoft.smartyoutubetv2.tv.R;
import com.liskovsoft.smartyoutubetv2.tv.presenter.base.LongClickPresenter;
import com.liskovsoft.smartyoutubetv2.tv.ui.browse.video.GridFragmentHelper;
import com.liskovsoft.smartyoutubetv2.tv.util.ViewUtil;

public class ChannelCardPresenter extends LongClickPresenter {
    private static final String TAG = VideoCardPresenter.class.getSimpleName();
    private int mDefaultBackgroundColor;
    private int mDefaultTextColor;
    private int mSelectedBackgroundColor;
    private int mNewContentBackgroundColor;
    private int mSelectedTextColor;
    private int mAccentColor;
    private int mWidth;
    private int mHeight;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        Context context = parent.getContext();

        mDefaultBackgroundColor =
                ContextCompat.getColor(context, Helpers.getThemeAttr(context, R.attr.cardDefaultBackground));
        mDefaultTextColor =
                ContextCompat.getColor(context, R.color.card_default_text);
        mNewContentBackgroundColor =
                ContextCompat.getColor(context, R.color.dark_red);
        mSelectedBackgroundColor =
                ContextCompat.getColor(context, Helpers.getThemeAttr(context, R.attr.cardSelectedBackground));
        mSelectedTextColor =
                ContextCompat.getColor(context, R.color.card_selected_text_grey);

        updateDimensions(context);

        @SuppressLint("InflateParams")
        View container = LayoutInflater.from(context).inflate(R.layout.channel_card, null);
        // MyTube: plain row, white rounded pill when focused (TV app channel list)
        container.setBackgroundResource(R.drawable.mytube_rail_pill);
        mDefaultTextColor = ContextCompat.getColor(context, R.color.mytube_text_primary);
        mSelectedTextColor = ContextCompat.getColor(context, R.color.mytube_text_on_focus);
        mAccentColor = ContextCompat.getColor(context, R.color.mytube_accent);
        //if (VERSION.SDK_INT >= 23 && MainUIData.instance(context).isUiTweakEnabled(MainUIData.UI_TWEAK_ROUNDED_CORNERS)) {
        //    container.setForeground(ContextCompat.getDrawable(context, R.drawable.lb_card_outline));
        //}

        TextView textView = container.findViewById(R.id.channel_title);
        textView.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        textView.setTextColor(mDefaultTextColor);

        boolean autoScrollEnabled = isCardTextAutoScrollEnabled(context);
        if (autoScrollEnabled) {
            ViewUtil.setTextScrollSpeed(textView, getCardTextScrollSpeed(context));
        }

        ImageView iconView = container.findViewById(R.id.channel_image);

        container.setOnFocusChangeListener((v, hasFocus) -> {
            textView.setTextColor(hasFocus ? mSelectedTextColor : mDefaultTextColor);
            tintLocalIcon(iconView, hasFocus);

            if (!autoScrollEnabled) {
                return;
            }

            if (hasFocus) {
                ViewUtil.enableMarquee(textView);
            } else {
                ViewUtil.disableMarquee(textView);
            }
        });

        return new ViewHolder(container);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        super.onBindViewHolder(viewHolder, item);

        Context context = viewHolder.view.getContext();
        Video video = (Video) item;

        ViewUtil.setDimensions(viewHolder.view.findViewById(R.id.channel_card_wrapper), mWidth, -1); // don't do auto height

        TextView textView = viewHolder.view.findViewById(R.id.channel_title);
        // MyTube: new videos = small accent dot after the name (instead of a red background)
        if (video.hasNewContent && video.getTitle() != null) {
            android.text.SpannableString title = new android.text.SpannableString(video.getTitle() + "  \u25CF");
            title.setSpan(new android.text.style.ForegroundColorSpan(mAccentColor), title.length() - 1, title.length(),
                    android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            textView.setText(title);
        } else {
            textView.setText(video.getTitle());
        }
        textView.setTextColor(viewHolder.view.hasFocus() ? mSelectedTextColor : mDefaultTextColor);

        ImageView imageView = viewHolder.view.findViewById(R.id.channel_image);
        if (video.cardImageUrl == null) {
            // MyTube: entries without a channel (the "All" entry): list icon, tinted like the text
            Glide.with(context).clear(imageView);
            imageView.setTag(R.id.channel_new_content, true);
            imageView.setImageResource(R.drawable.icon_playlist);
            imageView.setVisibility(View.VISIBLE);
            tintLocalIcon(imageView, viewHolder.view.hasFocus());
            return;
        }
        imageView.setTag(R.id.channel_new_content, null);
        imageView.clearColorFilter();
        imageView.setVisibility(View.VISIBLE);

        Glide.with(context)
                .load(video.cardImageUrl)
                .apply(ViewUtil.thumbnailOptions().circleCrop())
                .listener(mErrorListener)
                //.error(R.drawable.card_placeholder) // R.color.lb_grey
                .into(imageView);
    }

    /**
     * MyTube: local icons (not avatars) follow the text color, so they stay visible on the white focus pill.
     */
    private void tintLocalIcon(ImageView imageView, boolean hasFocus) {
        if (imageView.getTag(R.id.channel_new_content) == null) {
            return;
        }

        imageView.setColorFilter(hasFocus ? mSelectedTextColor : mDefaultTextColor);
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {
        // Remove references to images so that the garbage collector can free up memory.
        ImageView imageView = viewHolder.view.findViewById(R.id.channel_image);
        imageView.setImageDrawable(null);
    }

    private void updateDimensions(Context context) {
        Pair<Integer, Integer> dimens = getCardDimensPx(context);

        mWidth = dimens.first;
        mHeight = dimens.second;
    }

    protected Pair<Integer, Integer> getCardDimensPx(Context context) {
        return GridFragmentHelper.getCardDimensPx(
                context, R.dimen.channel_card_width,
                R.dimen.channel_card_height,
                MainUIData.instance(context).getVideoGridScale(),
                true);
    }

    protected boolean isCardTextAutoScrollEnabled(Context context) {
        return MainUIData.instance(context).isCardTextAutoScrollEnabled();
    }

    protected float getCardTextScrollSpeed(Context context) {
        return MainUIData.instance(context).getCardTextScrollSpeed();
    }

    private final RequestListener<Drawable> mErrorListener = new RequestListener<Drawable>() {
        @Override
        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
            Log.e(TAG, "Glide load failed: " + e);
            return false;
        }

        @Override
        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
            return false;
        }
    };
}
