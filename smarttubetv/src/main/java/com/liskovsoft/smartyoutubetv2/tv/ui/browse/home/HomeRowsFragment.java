package com.liskovsoft.smartyoutubetv2.tv.ui.browse.home;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.liskovsoft.smartyoutubetv2.common.app.models.data.VideoGroup;
import com.liskovsoft.smartyoutubetv2.tv.R;
import com.liskovsoft.smartyoutubetv2.tv.presenter.ChipsRowPresenter;
import com.liskovsoft.smartyoutubetv2.tv.presenter.ChipsRowPresenter.ChipsRow;
import com.liskovsoft.smartyoutubetv2.tv.presenter.HomeCardPresenter;
import com.liskovsoft.smartyoutubetv2.tv.presenter.VideoCardPresenter;
import com.liskovsoft.smartyoutubetv2.tv.ui.browse.video.VideoRowsFragment;

/**
 * MyTube Home: category chips on top, then one horizontally scrolling shelf per category with large 16:9 cards.
 * Selecting a chip jumps to its shelf ("All" jumps back to the first shelf).
 * Rows/cards are lazy: Leanback only binds what is on screen and pages each shelf as focus nears its end.
 */
public class HomeRowsFragment extends VideoRowsFragment {
    private static final int CHIP_ALL = Integer.MIN_VALUE;
    /** Official TV app Home has no chips row (search box on top instead). Set true to bring the chips back. */
    private static final boolean SHOW_CHIPS = false;
    private ChipsRow mChipsRow;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        if (SHOW_CHIPS) {
            mChipsRow = new ChipsRow(CHIP_ALL);
            mChipsRow.setOnChipClickedListener(this::onChipClicked);
            addTopRow(mChipsRow, new ChipsRowPresenter());
        }

        super.onCreate(savedInstanceState);

        if (mChipsRow != null) {
            mChipsRow.addChip(CHIP_ALL, getString(R.string.mytube_chip_all));
        }
    }

    @Override
    protected VideoCardPresenter createCardPresenter() {
        return new HomeCardPresenter();
    }

    @Override
    protected void onGroupRowAdded(VideoGroup group, int position) {
        if (mChipsRow != null) {
            mChipsRow.addChip(group.getId(), group.getTitle());
        }
    }

    @Override
    protected void onRowsCleared() {
        if (mChipsRow != null) {
            mChipsRow.trimToFirst();
            mChipsRow.setActive(CHIP_ALL);
        }
    }

    private void onChipClicked(int chipId) {
        mChipsRow.setActive(chipId);
        int position = chipId == CHIP_ALL ? 1 : findRowPosition(chipId);
        if (position > 0) {
            setSelectedPosition(position, true);
        }
    }
}
