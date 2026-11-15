package com.liskovsoft.smartyoutubetv2.tv.ui.playback.actions;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import androidx.leanback.widget.Action;
import com.liskovsoft.smartyoutubetv2.tv.R;

/**
 * MyTube: gear button (TV app player). Opens a menu with Quality, Captions and Playback speed.
 */
public class PlayerSettingsAction extends Action {
    public PlayerSettingsAction(Context context) {
        super(R.id.action_player_settings);
        Drawable icon = ContextCompat.getDrawable(context, R.drawable.icon_settings);
        setIcon(icon);
        setLabel1(context.getString(R.string.mytube_player_settings));
    }
}
