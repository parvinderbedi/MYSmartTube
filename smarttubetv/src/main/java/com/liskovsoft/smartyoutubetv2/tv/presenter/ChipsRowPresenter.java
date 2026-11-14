package com.liskovsoft.smartyoutubetv2.tv.presenter;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;

import com.liskovsoft.smartyoutubetv2.tv.R;

import java.util.ArrayList;
import java.util.List;

/**
 * MyTube: a row of category chips shown above the Home shelves.
 * Plain TextViews in a HorizontalScrollView: cheap to inflate, no RecyclerView, no animations.
 */
public class ChipsRowPresenter extends RowPresenter {
    public interface OnChipClickedListener {
        void onChipClicked(int chipId);
    }

    /** Row model. Chips can be added while the row is on screen. */
    public static class ChipsRow extends Row {
        private final List<Integer> mIds = new ArrayList<>();
        private final List<String> mTitles = new ArrayList<>();
        private int mActiveId;
        private OnChipClickedListener mListener;
        private ViewHolder mBoundHolder;

        public ChipsRow(int activeId) {
            mActiveId = activeId;
        }

        public void setOnChipClickedListener(OnChipClickedListener listener) {
            mListener = listener;
        }

        public boolean hasChip(int id) {
            return mIds.contains(id);
        }

        public void addChip(int id, String title) {
            if (hasChip(id)) {
                return;
            }
            mIds.add(id);
            mTitles.add(title);
            if (mBoundHolder != null) {
                mBoundHolder.addChipView(this, id, title);
            }
        }

        /** Remove all chips except the first one ("All"). */
        public void trimToFirst() {
            while (mIds.size() > 1) {
                mIds.remove(mIds.size() - 1);
                mTitles.remove(mTitles.size() - 1);
            }
            if (mBoundHolder != null) {
                mBoundHolder.rebuild(this);
            }
        }

        public void setActive(int id) {
            mActiveId = id;
            if (mBoundHolder != null) {
                mBoundHolder.updateActive(this);
            }
        }
    }

    public static class ViewHolder extends RowPresenter.ViewHolder {
        private final LinearLayout mContainer;

        ViewHolder(HorizontalScrollView view, LinearLayout container) {
            super(view);
            mContainer = container;
        }

        void rebuild(ChipsRow row) {
            mContainer.removeAllViews();
            for (int i = 0; i < row.mIds.size(); i++) {
                addChipView(row, row.mIds.get(i), row.mTitles.get(i));
            }
            updateActive(row);
        }

        void addChipView(ChipsRow row, int id, String title) {
            Context context = mContainer.getContext();
            Resources res = context.getResources();
            TextView chip = new TextView(context);
            chip.setText(title);
            chip.setSingleLine(true);
            chip.setTextSize(TypedValue.COMPLEX_UNIT_PX, res.getDimension(R.dimen.mytube_chip_text_size));
            chip.setTextColor(ContextCompat.getColorStateList(context, R.color.mytube_chip_text));
            chip.setBackgroundResource(R.drawable.mytube_chip_bg);
            int h = res.getDimensionPixelSize(R.dimen.mytube_chip_padding_h);
            int v = res.getDimensionPixelSize(R.dimen.mytube_chip_padding_v);
            chip.setPadding(h, v, h, v);
            chip.setGravity(Gravity.CENTER);
            chip.setFocusable(true);
            chip.setFocusableInTouchMode(true);
            chip.setTag(id);
            chip.setActivated(id == row.mActiveId);
            chip.setOnClickListener(view -> {
                if (row.mListener != null) {
                    row.mListener.onChipClicked(id);
                }
            });

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            if (mContainer.getChildCount() > 0) {
                lp.setMarginStart(res.getDimensionPixelSize(R.dimen.mytube_chip_spacing));
            }
            mContainer.addView(chip, lp);
        }

        void updateActive(ChipsRow row) {
            for (int i = 0; i < mContainer.getChildCount(); i++) {
                Object tag = mContainer.getChildAt(i).getTag();
                mContainer.getChildAt(i).setActivated(tag instanceof Integer && (Integer) tag == row.mActiveId);
            }
        }
    }

    public ChipsRowPresenter() {
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
        scroll.setPaddingRelative(res.getDimensionPixelSize(R.dimen.mytube_content_padding_start), 0,
                res.getDimensionPixelSize(R.dimen.mytube_content_padding_end), res.getDimensionPixelSize(R.dimen.mytube_chips_grid_gap));
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(Gravity.CENTER_VERTICAL);
        scroll.addView(container, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                res.getDimensionPixelSize(R.dimen.mytube_chips_height)));
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
        ChipsRow row = (ChipsRow) item;
        row.mBoundHolder = (ViewHolder) vh;
        ((ViewHolder) vh).rebuild(row);
    }

    @Override
    protected void onUnbindRowViewHolder(RowPresenter.ViewHolder vh) {
        Object item = vh.getRow();
        if (item instanceof ChipsRow && ((ChipsRow) item).mBoundHolder == vh) {
            ((ChipsRow) item).mBoundHolder = null;
        }
        super.onUnbindRowViewHolder(vh);
    }
}
