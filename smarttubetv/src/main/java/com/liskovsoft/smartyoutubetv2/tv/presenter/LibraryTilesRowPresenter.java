package com.liskovsoft.smartyoutubetv2.tv.presenter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;

import com.liskovsoft.smartyoutubetv2.tv.R;

import java.util.ArrayList;
import java.util.List;

/**
 * MyTube: the row of colored shortcut tiles on top of the Library page (History, Watch later, ...),
 * as on the TV app. Plain TextViews in a HorizontalScrollView, like {@link ChipsRowPresenter}.
 */
public class LibraryTilesRowPresenter extends RowPresenter {
    public interface OnTileClickedListener {
        void onTileClicked(int tileId);
    }

    private static class Tile {
        final int id;
        final String title;
        final int iconResId;
        final int startColorResId;
        final int endColorResId;

        Tile(int id, String title, int iconResId, int startColorResId, int endColorResId) {
            this.id = id;
            this.title = title;
            this.iconResId = iconResId;
            this.startColorResId = startColorResId;
            this.endColorResId = endColorResId;
        }
    }

    /** Row model. Add all tiles before the row is shown. */
    public static class LibraryTilesRow extends Row {
        private final List<Tile> mTiles = new ArrayList<>();
        private OnTileClickedListener mListener;

        public void setOnTileClickedListener(OnTileClickedListener listener) {
            mListener = listener;
        }

        public void addTile(int id, String title, int iconResId, int startColorResId, int endColorResId) {
            mTiles.add(new Tile(id, title, iconResId, startColorResId, endColorResId));
        }
    }

    public static class ViewHolder extends RowPresenter.ViewHolder {
        private final LinearLayout mContainer;

        ViewHolder(HorizontalScrollView view, LinearLayout container) {
            super(view);
            mContainer = container;
        }

        void rebuild(LibraryTilesRow row) {
            mContainer.removeAllViews();
            for (Tile tile : row.mTiles) {
                addTileView(row, tile);
            }
        }

        private void addTileView(LibraryTilesRow row, Tile tile) {
            Context context = mContainer.getContext();
            Resources res = context.getResources();
            int padding = res.getDimensionPixelSize(R.dimen.mytube_library_tile_padding);
            int white = ContextCompat.getColor(context, R.color.white);

            // Icon at the top-left, title at the bottom-left
            FrameLayout tileView = new FrameLayout(context);
            tileView.setBackground(createBackground(context, tile));
            tileView.setPadding(padding, padding, padding, padding);

            ImageView iconView = new ImageView(context);
            iconView.setImageResource(tile.iconResId);
            iconView.setColorFilter(white);
            iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            int iconSize = res.getDimensionPixelSize(R.dimen.mytube_library_tile_icon_size);
            tileView.addView(iconView, new FrameLayout.LayoutParams(iconSize, iconSize, Gravity.TOP | Gravity.START));

            TextView titleView = new TextView(context);
            titleView.setText(tile.title);
            titleView.setMaxLines(2);
            titleView.setEllipsize(TextUtils.TruncateAt.END);
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, res.getDimension(R.dimen.mytube_library_tile_text_size));
            titleView.setTextColor(white);
            titleView.setTypeface(Typeface.DEFAULT_BOLD);
            tileView.addView(titleView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM | Gravity.START));

            tileView.setFocusable(true);
            tileView.setFocusableInTouchMode(true);
            tileView.setOnClickListener(v -> {
                if (row.mListener != null) {
                    row.mListener.onTileClicked(tile.id);
                }
            });

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    res.getDimensionPixelSize(R.dimen.mytube_library_tile_width),
                    res.getDimensionPixelSize(R.dimen.mytube_library_tile_height));
            if (mContainer.getChildCount() > 0) {
                lp.setMarginStart(res.getDimensionPixelSize(R.dimen.mytube_library_tile_spacing));
            }
            mContainer.addView(tileView, lp);
        }

        private static Drawable createBackground(Context context, Tile tile) {
            StateListDrawable background = new StateListDrawable();
            background.addState(new int[] {android.R.attr.state_focused}, createGradient(context, tile, true));
            background.addState(new int[] {}, createGradient(context, tile, false));
            return background;
        }

        private static GradientDrawable createGradient(Context context, Tile tile, boolean focused) {
            Resources res = context.getResources();
            GradientDrawable gradient = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[] {
                    ContextCompat.getColor(context, tile.startColorResId),
                    ContextCompat.getColor(context, tile.endColorResId)
            });
            gradient.setCornerRadius(res.getDimension(R.dimen.mytube_library_tile_radius));
            if (focused) {
                gradient.setStroke(res.getDimensionPixelSize(R.dimen.mytube_library_tile_focus_stroke),
                        ContextCompat.getColor(context, R.color.mytube_focus_ring));
            }
            return gradient;
        }
    }

    public LibraryTilesRowPresenter() {
        setHeaderPresenter(null);
        setSelectEffectEnabled(false);
    }

    @Override
    protected RowPresenter.ViewHolder createRowViewHolder(ViewGroup parent) {
        Context context = parent.getContext();
        Resources res = context.getResources();
        HorizontalScrollView scroll = new HorizontalScrollView(context);
        scroll.setHorizontalScrollBarEnabled(false);
        scroll.setClipToPadding(false);
        int focusStroke = res.getDimensionPixelSize(R.dimen.mytube_library_tile_focus_stroke);
        scroll.setPaddingRelative(res.getDimensionPixelSize(R.dimen.mytube_content_padding_start), focusStroke,
                res.getDimensionPixelSize(R.dimen.mytube_content_padding_end), res.getDimensionPixelSize(R.dimen.mytube_library_tiles_bottom_gap));
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(Gravity.CENTER_VERTICAL);
        scroll.addView(container, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        scroll.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return new ViewHolder(scroll, container);
    }

    @Override
    public boolean isUsingDefaultSelectEffect() {
        return false;
    }

    @Override
    protected void onBindRowViewHolder(RowPresenter.ViewHolder vh, Object item) {
        super.onBindRowViewHolder(vh, item);
        ((ViewHolder) vh).rebuild((LibraryTilesRow) item);
    }
}
