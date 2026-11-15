package com.liskovsoft.smartyoutubetv2.tv.ui.browse.library;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.liskovsoft.mediaserviceinterfaces.data.MediaGroup;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.BrowsePresenter;
import com.liskovsoft.smartyoutubetv2.tv.R;
import com.liskovsoft.smartyoutubetv2.tv.presenter.LibraryTilesRowPresenter;
import com.liskovsoft.smartyoutubetv2.tv.presenter.LibraryTilesRowPresenter.LibraryTilesRow;
import com.liskovsoft.smartyoutubetv2.tv.ui.browse.video.VideoRowsFragment;

/**
 * MyTube Library: a row of colored shortcut tiles (History, Watch later, Liked videos, Playlists, Music),
 * then the playlist shelves, as on the TV app.
 */
public class LibraryRowsFragment extends VideoRowsFragment {
    private static final int TILE_HISTORY = 0;
    private static final int TILE_WATCH_LATER = 1;
    private static final int TILE_LIKED = 2;
    private static final int TILE_PLAYLISTS = 3;
    private static final int TILE_MUSIC = 4;
    private static final String PLAYLIST_WATCH_LATER = "WL";
    private static final String PLAYLIST_LIKED = "LL";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        LibraryTilesRow tilesRow = new LibraryTilesRow();
        tilesRow.addTile(TILE_HISTORY, getString(R.string.header_history), R.drawable.icon_history,
                R.color.mytube_tile_history_start, R.color.mytube_tile_history_end);
        tilesRow.addTile(TILE_WATCH_LATER, getString(R.string.mytube_library_watch_later), R.drawable.icon_hourglass_bottom,
                R.color.mytube_tile_watch_later_start, R.color.mytube_tile_watch_later_end);
        tilesRow.addTile(TILE_LIKED, getString(R.string.mytube_library_liked), R.drawable.lb_ic_thumb_up,
                R.color.mytube_tile_liked_start, R.color.mytube_tile_liked_end);
        tilesRow.addTile(TILE_PLAYLISTS, getString(R.string.header_playlists), R.drawable.icon_playlist,
                R.color.mytube_tile_playlists_start, R.color.mytube_tile_playlists_end);
        tilesRow.addTile(TILE_MUSIC, getString(R.string.header_music), R.drawable.icon_music,
                R.color.mytube_tile_music_start, R.color.mytube_tile_music_end);
        tilesRow.setOnTileClickedListener(this::onTileClicked);
        addTopRow(tilesRow, new LibraryTilesRowPresenter());

        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getStartPosition() {
        // Start on the tiles, as on the TV app
        return 0;
    }

    private void onTileClicked(int tileId) {
        switch (tileId) {
            case TILE_HISTORY:
                BrowsePresenter.instance(getContext()).selectSection(MediaGroup.TYPE_HISTORY);
                break;
            case TILE_MUSIC:
                BrowsePresenter.instance(getContext()).selectSection(MediaGroup.TYPE_MUSIC);
                break;
            case TILE_WATCH_LATER:
                selectRow(findRowPositionByPlaylistId(PLAYLIST_WATCH_LATER));
                break;
            case TILE_LIKED:
                selectRow(findRowPositionByPlaylistId(PLAYLIST_LIKED));
                break;
            case TILE_PLAYLISTS:
                selectRow(1);
                break;
        }
    }

    /**
     * Jump to the given shelf; the first shelf if that playlist isn't shown (or not loaded yet).
     */
    private void selectRow(int position) {
        if (position <= 0) {
            position = 1;
        }

        if (getAdapter() != null && position < getAdapter().size()) {
            setSelectedPosition(position, true);
        }
    }
}
