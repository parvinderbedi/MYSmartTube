package com.liskovsoft.smartyoutubetv2.common.app.presenters.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.liskovsoft.mediaserviceinterfaces.oauth.Account;
import com.liskovsoft.smartyoutubetv2.common.R;

import java.util.List;

/**
 * MyTube: full-screen "Who's watching?" profile picker, as on the TV app.<br/>
 * Large round avatars with names (the current one ringed, with its e-mail), "Add account",
 * then "Watch as guest" (no account) and "Manage accounts" pills.<br/>
 * Views are built in code, so the screen needs no layouts or themes from the app module.
 */
public class ProfilePickerDialog {
    private static final int COLOR_BG = 0xFF0F0F0F;
    private static final int COLOR_TEXT = 0xFFF1F1F1;
    private static final int COLOR_TEXT_SECONDARY = 0xFFAAAAAA;
    private static final int COLOR_TEXT_ON_FOCUS = 0xFF0F0F0F;
    private static final int COLOR_SURFACE = 0xFF272727;
    private static final int COLOR_FOCUS_RING = 0xFFFFFFFF;
    private static final int COLOR_CURRENT_RING = 0xFF1EC8A5;
    private static final int[] PLACEHOLDER_COLORS = {0xFF3F51B5, 0xFF00897B, 0xFFF57C00, 0xFF8E24AA, 0xFFC62828, 0xFF1565C0};

    public interface Callback {
        void onAccountSelected(Account account);
        void onAddAccount();
        void onManageAccounts();
        void onDismiss();
    }

    private final Activity mActivity;
    private final Callback mCallback;
    private Dialog mDialog;
    private boolean mHandled;

    public ProfilePickerDialog(Activity activity, Callback callback) {
        mActivity = activity;
        mCallback = callback;
    }

    /**
     * @param icons circle-cropped avatars in the same order as the accounts (may be null or hold nulls)
     */
    public void show(List<Account> accounts, List<Drawable> icons) {
        mDialog = new Dialog(mActivity, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(createContent(accounts, icons));
        mDialog.setOnDismissListener(dialog -> mCallback.onDismiss());
        mDialog.show();
    }

    private void close() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    private View createContent(List<Account> accounts, List<Drawable> icons) {
        Context context = mActivity;

        FrameLayout root = new FrameLayout(context);
        root.setBackgroundColor(COLOR_BG);

        LinearLayout column = new LinearLayout(context);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setGravity(Gravity.CENTER_HORIZONTAL);
        root.addView(column, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));

        TextView title = createText(context, context.getString(R.string.mytube_whos_watching), 34, COLOR_TEXT);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        column.addView(title, wrap());

        HorizontalScrollView scroll = new HorizontalScrollView(context);
        scroll.setHorizontalScrollBarEnabled(false);
        scroll.setClipToPadding(false);
        scroll.setPadding(dp(48), dp(40), dp(48), dp(32));
        LinearLayout profiles = new LinearLayout(context);
        profiles.setOrientation(LinearLayout.HORIZONTAL);
        profiles.setGravity(Gravity.TOP);
        scroll.addView(profiles, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        column.addView(scroll, wrap());

        View focusTarget = null;

        if (accounts != null) {
            for (int i = 0; i < accounts.size(); i++) {
                Account account = accounts.get(i);
                Drawable icon = icons != null && icons.size() == accounts.size() ? icons.get(i) : null;
                View item = createProfileItem(context, account, icon, i);
                profiles.addView(item, itemParams(profiles));

                if (account.isSelected() || focusTarget == null) {
                    focusTarget = item;
                }
            }
        }

        View addItem = createAddItem(context);
        profiles.addView(addItem, itemParams(profiles));

        if (focusTarget == null) {
            focusTarget = addItem;
        }

        LinearLayout pills = new LinearLayout(context);
        pills.setOrientation(LinearLayout.HORIZONTAL);
        pills.setGravity(Gravity.CENTER);

        TextView guest = createPill(context, context.getString(R.string.mytube_watch_as_guest));
        guest.setOnClickListener(v -> handle(() -> mCallback.onAccountSelected(null)));
        pills.addView(guest, wrap());

        TextView manage = createPill(context, context.getString(R.string.mytube_manage_accounts));
        manage.setOnClickListener(v -> handle(mCallback::onManageAccounts));
        LinearLayout.LayoutParams manageParams = wrap();
        manageParams.leftMargin = dp(16);
        pills.addView(manage, manageParams);

        column.addView(pills, wrap());

        View initialFocus = focusTarget;
        root.post(initialFocus::requestFocus);

        return root;
    }

    private View createProfileItem(Context context, Account account, Drawable icon, int index) {
        LinearLayout item = createItemContainer(context);

        FrameLayout avatarFrame = createAvatarFrame(context, account.isSelected());

        if (icon != null) {
            ImageView avatar = new ImageView(context);
            avatar.setImageDrawable(icon);
            avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
            avatarFrame.addView(avatar, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        } else {
            avatarFrame.addView(createLetterAvatar(context, account.getName(), PLACEHOLDER_COLORS[index % PLACEHOLDER_COLORS.length]),
                    new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }

        item.addView(avatarFrame, new LinearLayout.LayoutParams(dp(128), dp(128)));

        TextView name = createText(context, account.getName() != null ? account.getName() : "", 20, COLOR_TEXT);
        name.setMaxWidth(dp(176));
        name.setPadding(0, dp(12), 0, 0);
        item.addView(name, wrap());

        if (account.isSelected() && account.getEmail() != null) {
            TextView email = createText(context, account.getEmail(), 15, COLOR_TEXT_SECONDARY);
            email.setMaxWidth(dp(176));
            item.addView(email, wrap());
        }

        item.setOnClickListener(v -> handle(() -> mCallback.onAccountSelected(account)));

        return item;
    }

    private View createAddItem(Context context) {
        LinearLayout item = createItemContainer(context);

        FrameLayout avatarFrame = createAvatarFrame(context, false);
        TextView plus = createText(context, "+", 56, COLOR_TEXT);
        plus.setGravity(Gravity.CENTER);
        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setColor(COLOR_SURFACE);
        plus.setBackground(circle);
        avatarFrame.addView(plus, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        item.addView(avatarFrame, new LinearLayout.LayoutParams(dp(128), dp(128)));

        TextView name = createText(context, context.getString(R.string.mytube_add_account), 20, COLOR_TEXT);
        name.setPadding(0, dp(12), 0, 0);
        item.addView(name, wrap());

        item.setOnClickListener(v -> handle(mCallback::onAddAccount));

        return item;
    }

    private LinearLayout createItemContainer(Context context) {
        LinearLayout item = new LinearLayout(context);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setGravity(Gravity.CENTER_HORIZONTAL);
        item.setFocusable(true);
        item.setFocusableInTouchMode(true);
        item.setPadding(dp(8), dp(8), dp(8), dp(8));
        return item;
    }

    /**
     * Avatar holder with a ring: white when the profile is focused, accent for the current profile.
     */
    private FrameLayout createAvatarFrame(Context context, boolean current) {
        FrameLayout frame = new FrameLayout(context);
        frame.setDuplicateParentStateEnabled(true);
        int ring = dp(5);
        frame.setPadding(ring + dp(3), ring + dp(3), ring + dp(3), ring + dp(3));

        StateListDrawable rings = new StateListDrawable();
        rings.addState(new int[] {android.R.attr.state_focused}, createRing(COLOR_FOCUS_RING, ring));
        if (current) {
            rings.addState(new int[] {}, createRing(COLOR_CURRENT_RING, dp(3)));
        }
        frame.setBackground(rings);

        return frame;
    }

    private static GradientDrawable createRing(int color, int width) {
        GradientDrawable ring = new GradientDrawable();
        ring.setShape(GradientDrawable.OVAL);
        ring.setColor(Color.TRANSPARENT);
        ring.setStroke(width, color);
        return ring;
    }

    private TextView createLetterAvatar(Context context, String name, int color) {
        String letter = TextUtils.isEmpty(name) ? "?" : name.substring(0, 1).toUpperCase();
        TextView avatar = createText(context, letter, 52, Color.WHITE);
        avatar.setGravity(Gravity.CENTER);
        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setColor(color);
        avatar.setBackground(circle);
        return avatar;
    }

    private TextView createPill(Context context, String text) {
        TextView pill = createText(context, text, 20, COLOR_TEXT);
        pill.setGravity(Gravity.CENTER);
        pill.setPadding(dp(28), dp(12), dp(28), dp(12));
        pill.setFocusable(true);
        pill.setFocusableInTouchMode(true);

        StateListDrawable background = new StateListDrawable();
        background.addState(new int[] {android.R.attr.state_focused}, createPillShape(COLOR_FOCUS_RING));
        background.addState(new int[] {}, createPillShape(COLOR_SURFACE));
        pill.setBackground(background);
        pill.setOnFocusChangeListener((v, hasFocus) -> pill.setTextColor(hasFocus ? COLOR_TEXT_ON_FOCUS : COLOR_TEXT));

        return pill;
    }

    private GradientDrawable createPillShape(int color) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(color);
        shape.setCornerRadius(dp(28));
        return shape;
    }

    private TextView createText(Context context, String text, int sizeSp, int color) {
        TextView view = new TextView(context);
        view.setText(text);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp);
        view.setTextColor(color);
        view.setSingleLine(true);
        view.setEllipsize(TextUtils.TruncateAt.END);
        view.setGravity(Gravity.CENTER_HORIZONTAL);
        return view;
    }

    private LinearLayout.LayoutParams itemParams(LinearLayout parent) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (parent.getChildCount() > 0) {
            params.leftMargin = dp(32);
        }
        return params;
    }

    private static LinearLayout.LayoutParams wrap() {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    /**
     * Close the screen and run the action (once; the dismiss callback is delivered afterwards).
     */
    private void handle(Runnable action) {
        if (mHandled) {
            return;
        }

        mHandled = true;
        close();
        action.run();
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, mActivity.getResources().getDisplayMetrics());
    }
}
