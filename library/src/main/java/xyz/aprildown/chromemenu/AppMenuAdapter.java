// Copyright 2014 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package xyz.aprildown.chromemenu;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.IntDef;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.view.MenuItemCompat;
import androidx.core.widget.ImageViewCompat;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ListAdapter to customize the view of items in the list.
 * <p>
 * The icon row in the menu is a special case of a MenuItem that displays multiple buttons in a row.
 * If, for some unfathomable reason, you need to add yet another icon to the row (the current max
 * is five), then you will need to:
 * <p>
 * 1) Update cm_icon_row_menu_item.xml to have as many buttons as you need.
 * 2) Edit the BUTTON_IDS to reference your new button.
 * 3) Hope that the icon row still fits on small phones.
 */
class AppMenuAdapter extends BaseAdapter {

    private final AppMenuClickHandler mAppMenuClickHandler;

    /**
     * IDs of all of the buttons in cm_icon_row_menu_item.xml.
     */
    private static final int[] BUTTON_IDS = {
            R.id.button_one,
            R.id.button_two,
            R.id.button_three,
            R.id.button_four,
            R.id.button_five
    };
    /**
     * MenuItem Animation Constants
     */
    private static final int ENTER_ITEM_DURATION_MS = 350;
    private static final int ENTER_ITEM_BASE_DELAY_MS = 80;
    private static final int ENTER_ITEM_ADDL_DELAY_MS = 30;
    private static final float ENTER_STANDARD_ITEM_OFFSET_Y_DP = -10.f;
    private static final float ENTER_STANDARD_ITEM_OFFSET_X_DP = 10.f;
    private final @Nullable
    List<CustomViewBinder> mCustomViewBinders;
    private final LayoutInflater mInflater;
    private final List<MenuItem> mMenuItems;
    private final int mNumMenuItems;
    private final Integer mHighlightedItemId;
    private final float mDpToPx;
    private final int mCustomViewTypes;
    private final Map<CustomViewBinder, Integer> mViewTypeOffsetMap;
    private final boolean mIconBeforeItem;

    AppMenuAdapter(AppMenuClickHandler appMenuClickHandler, List<MenuItem> menuItems,
                   LayoutInflater inflater, Integer highlightedItemId,
                   @Nullable List<CustomViewBinder> customViewBinders, boolean iconBeforeItem) {
        mAppMenuClickHandler = appMenuClickHandler;
        mMenuItems = menuItems;
        mInflater = inflater;
        mHighlightedItemId = highlightedItemId;
        mCustomViewBinders = customViewBinders;
        mIconBeforeItem = iconBeforeItem;
        mNumMenuItems = menuItems.size();
        mDpToPx = inflater.getContext().getResources().getDisplayMetrics().density;

        mCustomViewTypes = getCustomViewTypeCount(customViewBinders);
        mViewTypeOffsetMap = new HashMap<>();
        populateCustomViewBinderOffsetMap(
                customViewBinders, mViewTypeOffsetMap, MenuItemType.NUM_ENTRIES);
    }

    private static int getCustomViewTypeCount(@Nullable List<CustomViewBinder> customViewBinders) {
        if (customViewBinders == null) return 0;

        int count = 0;
        for (int i = 0; i < customViewBinders.size(); i++) {
            count += customViewBinders.get(i).getViewTypeCount();
        }
        return count;
    }

    @Override
    public int getCount() {
        return mNumMenuItems;
    }

    private static void populateCustomViewBinderOffsetMap(
            @Nullable List<CustomViewBinder> customViewBinders, Map<CustomViewBinder, Integer> map,
            int startingOffset) {
        if (customViewBinders == null) return;

        int currentOffset = startingOffset;
        for (int i = 0; i < customViewBinders.size(); i++) {
            CustomViewBinder binder = customViewBinders.get(i);
            map.put(binder, currentOffset);
            currentOffset += binder.getViewTypeCount();
        }
    }

    @Override
    public int getViewTypeCount() {
        return MenuItemType.NUM_ENTRIES + mCustomViewTypes;
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getItemId();
    }

    @Override
    public MenuItem getItem(int position) {
        if (position == ListView.INVALID_POSITION) return null;
        if (position < 0) {
            throw new IllegalArgumentException("Invalid menu item position " + position);
        } else if (position >= mMenuItems.size()) {
            throw new IllegalArgumentException("Too big menu item position " + position + "/" + mMenuItems.size());
        }
        return mMenuItems.get(position);
    }

    private static boolean isLayoutRtl(final Context context) {
        Configuration configuration = context.getResources().getConfiguration();
        return ApiCompatibilityUtils.getLayoutDirection(configuration)
                == View.LAYOUT_DIRECTION_RTL;
    }

    private void setupCheckBox(AppMenuItemIcon button, final MenuItem item) {
        button.setChecked(item.isChecked());

        // The checkbox must be tinted to make Android consistently style it across OS versions.
        // http://crbug.com/571445
        ApiCompatibilityUtils.setImageTintList(button,
                AppCompatResources.getColorStateList(button.getContext(), R.color.cm_checkbox_tint));

        setupMenuButton(button, item);
    }

    private void setupImageButton(ImageButton button, final MenuItem item) {
        // Store and recover the level of image as button.setimageDrawable
        // resets drawable to default level.
        int currentLevel = item.getIcon().getLevel();
        button.setImageDrawable(item.getIcon());
        ImageViewCompat.setImageTintList(button, MenuItemCompat.getIconTintList(item));
        item.getIcon().setLevel(currentLevel);

        if (item.isChecked()) {
            ApiCompatibilityUtils.setImageTintList(button,
                    AppCompatResources.getColorStateList(
                            button.getContext(), R.color.cm_blue_mode_tint));
        }

        setupMenuButton(button, item);
    }

    @Override
    public @MenuItemType
    int getItemViewType(int position) {
        MenuItem item = getItem(position);
        int viewCount = item.hasSubMenu() ? item.getSubMenu().size() : 1;
        int customItemViewType = getCustomItemViewType(item);
        if (customItemViewType != CustomViewBinder.NOT_HANDLED) {
            return customItemViewType;
        } else if (viewCount == 2) {
            return MenuItemType.TITLE_BUTTON;
        } else if (viewCount == 3) {
            return MenuItemType.THREE_BUTTON;
        } else if (viewCount == 4) {
            return MenuItemType.FOUR_BUTTON;
        } else if (viewCount == 5) {
            return MenuItemType.FIVE_BUTTON;
        }
        return MenuItemType.STANDARD;
    }

    private void setupMenuButton(View button, final MenuItem item) {
        button.setEnabled(item.isEnabled());
        button.setFocusable(item.isEnabled());
        if (TextUtils.isEmpty(item.getTitleCondensed())) {
            button.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        } else {
            button.setContentDescription(item.getTitleCondensed());
            button.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_AUTO);
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAppMenuClickHandler.onItemClick(item);
            }
        });

        button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return mAppMenuClickHandler.onItemLongClick(item, v);
            }
        });

        if (mHighlightedItemId != null && item.getItemId() == mHighlightedItemId) {
            ViewHighlighter.turnOnHighlight(button, true);
        } else {
            ViewHighlighter.turnOffHighlight(button);
        }

        // Menu items may be hidden by command line flags before they get to this point.
        button.setVisibility(item.isVisible() ? View.VISIBLE : View.GONE);
    }

    /**
     * This builds an {@link Animator} for the enter animation of a standard menu item.  This means
     * it will animate the alpha from 0 to 1 and translate the view from -10dp to 0dp on the y axis.
     *
     * @param view     The menu item {@link View} to be animated.
     * @param position The position in the menu.  This impacts the start delay of the animation.
     * @return The {@link Animator}.
     */
    private Animator buildStandardItemEnterAnimator(final View view, int position) {
        final int startDelay = ENTER_ITEM_BASE_DELAY_MS + ENTER_ITEM_ADDL_DELAY_MS * position;

        AnimatorSet animation = new AnimatorSet();
        final float offsetYPx = ENTER_STANDARD_ITEM_OFFSET_Y_DP * mDpToPx;
        animation.playTogether(ObjectAnimator.ofFloat(view, View.ALPHA, 0.f, 1.f),
                ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, offsetYPx, 0.f));
        animation.setStartDelay(startDelay);
        animation.setDuration(ENTER_ITEM_DURATION_MS);
        animation.setInterpolator(BakedBezierInterpolator.FADE_IN_CURVE);

        animation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                view.setAlpha(0.f);
            }
        });
        return animation;
    }

    private void setupStandardMenuItemViewHolder(StandardMenuItemViewHolder holder,
                                                 View convertView, final MenuItem item) {
        // The standard menu item does not support the checkable item.
        assert !item.isChecked();

        // Set up the icon.
        Drawable icon = item.getIcon();
        holder.image.setImageDrawable(icon);
        holder.image.setVisibility(icon == null ? View.GONE : View.VISIBLE);
        holder.text.setText(item.getTitle());
        holder.text.setContentDescription(item.getTitleCondensed());

        boolean isEnabled = item.isEnabled();
        // Set the text color (using a color state list).
        holder.text.setEnabled(isEnabled);
        // This will ensure that the item is not highlighted when selected.
        convertView.setEnabled(isEnabled);

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAppMenuClickHandler.onItemClick(item);
            }
        });
    }

    /**
     * This builds an {@link Animator} for the enter animation of icon row menu items.  This means
     * it will animate the alpha from 0 to 1 and translate the views from 10dp to 0dp on the x axis.
     *
     * @param buttons The list of icons in the menu item that should be animated.
     * @return The {@link Animator}.
     */
    private Animator buildIconItemEnterAnimator(final ImageView[] buttons) {
        final boolean rtl = isLayoutRtl(mInflater.getContext());
        final float offsetXPx = ENTER_STANDARD_ITEM_OFFSET_X_DP * mDpToPx * (rtl ? -1.f : 1.f);
        final int maxViewsToAnimate = buttons.length;

        AnimatorSet animation = new AnimatorSet();
        AnimatorSet.Builder builder = null;
        for (int i = 0; i < maxViewsToAnimate; i++) {
            final int startDelay = ENTER_ITEM_ADDL_DELAY_MS * i;

            ImageView view = buttons[i];
            Animator alpha = ObjectAnimator.ofFloat(view, View.ALPHA, 0.f, 1.f);
            Animator translate = ObjectAnimator.ofFloat(view, View.TRANSLATION_X, offsetXPx, 0);
            alpha.setStartDelay(startDelay);
            translate.setStartDelay(startDelay);
            alpha.setDuration(ENTER_ITEM_DURATION_MS);
            translate.setDuration(ENTER_ITEM_DURATION_MS);

            if (builder == null) {
                builder = animation.play(alpha);
            } else {
                builder.with(alpha);
            }
            builder.with(translate);
        }
        animation.setStartDelay(ENTER_ITEM_BASE_DELAY_MS);
        animation.setInterpolator(BakedBezierInterpolator.FADE_IN_CURVE);

        animation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                for (ImageView button : buttons) {
                    button.setAlpha(0.f);
                }
            }
        });
        return animation;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final MenuItem item = getItem(position);
        int itemViewType = getItemViewType(position);
        switch (itemViewType) {
            case MenuItemType.STANDARD: {
                StandardMenuItemViewHolder holder;
                if (convertView == null
                        || (int) convertView.getTag(R.id.cm_menu_item_view_type)
                        != MenuItemType.STANDARD) {
                    holder = new StandardMenuItemViewHolder();
                    if (mIconBeforeItem) {
                        convertView = mInflater.inflate(
                                R.layout.cm_menu_item_start_with_icon, parent, false);
                    } else {
                        convertView = mInflater.inflate(R.layout.cm_menu_item, parent, false);
                    }
                    holder.text = convertView.findViewById(R.id.menu_item_text);
                    holder.image = convertView.findViewById(R.id.menu_item_icon);
                    convertView.setTag(holder);
                    convertView.setTag(R.id.cm_menu_item_enter_anim_id,
                            buildStandardItemEnterAnimator(convertView, position));
                } else {
                    holder = (StandardMenuItemViewHolder) convertView.getTag();
                }
                setupStandardMenuItemViewHolder(holder, convertView, item);
                break;
            }
            case MenuItemType.THREE_BUTTON:
                convertView = createMenuItemRow(convertView, parent, item, 3, itemViewType);
                break;
            case MenuItemType.FOUR_BUTTON:
                convertView = createMenuItemRow(convertView, parent, item, 4, itemViewType);
                break;
            case MenuItemType.FIVE_BUTTON:
                convertView = createMenuItemRow(convertView, parent, item, 5, itemViewType);
                break;
            case MenuItemType.TITLE_BUTTON: {
                if (!item.hasSubMenu()) {
                    throw new IllegalStateException("No sub menu in a title button");
                }
                final MenuItem titleItem = item.getSubMenu().getItem(0);
                final MenuItem subItem = item.getSubMenu().getItem(1);

                TitleButtonMenuItemViewHolder holder;

                if (convertView == null
                        || (int) convertView.getTag(R.id.cm_menu_item_view_type)
                        != MenuItemType.TITLE_BUTTON) {
                    convertView = mInflater.inflate(R.layout.cm_title_button_menu_item, parent, false);

                    holder = new TitleButtonMenuItemViewHolder();
                    holder.title = convertView.findViewById(R.id.title);
                    holder.checkbox = convertView.findViewById(R.id.checkbox);
                    holder.button = (ChromeImageButton) convertView.findViewById(R.id.button);

                    convertView.setTag(holder);
                    convertView.setTag(R.id.cm_menu_item_enter_anim_id,
                            buildStandardItemEnterAnimator(convertView, position));
                } else {
                    holder = (TitleButtonMenuItemViewHolder) convertView.getTag();
                }

                if (mIconBeforeItem) {
                    Drawable icon = titleItem.getIcon();
                    assert icon != null;
                    holder.title.setCompoundDrawablesRelative(icon, null, null, null);
                }

                holder.title.setText(titleItem.getTitle());
                holder.title.setEnabled(titleItem.isEnabled());
                holder.title.setFocusable(titleItem.isEnabled());
                holder.title.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mAppMenuClickHandler.onItemClick(titleItem);
                    }
                });
                if (TextUtils.isEmpty(titleItem.getTitleCondensed())) {
                    holder.title.setContentDescription(null);
                } else {
                    holder.title.setContentDescription(titleItem.getTitleCondensed());
                }

                if (subItem.isCheckable()) {
                    // Display a checkbox for the MenuItem.
                    holder.checkbox.setVisibility(View.VISIBLE);
                    holder.button.setVisibility(View.GONE);
                    setupCheckBox(holder.checkbox, subItem);
                } else if (subItem.getIcon() != null) {
                    // Display an icon alongside the MenuItem.
                    holder.checkbox.setVisibility(View.GONE);
                    holder.button.setVisibility(View.VISIBLE);
                    setupImageButton(holder.button, subItem);
                } else {
                    // Display just the label of the MenuItem.
                    holder.checkbox.setVisibility(View.GONE);
                    holder.button.setVisibility(View.GONE);
                }

                convertView.setFocusable(false);
                convertView.setEnabled(false);
                break;
            }
            default:
                // If we get into this block, the item must be handled by a custom binder.
                assert mCustomViewBinders != null;

                // Use custom binder.
                boolean bound = false;
                for (int i = 0; i < mCustomViewBinders.size(); i++) {
                    CustomViewBinder binder = mCustomViewBinders.get(i);
                    int customItemViewType = binder.getItemViewType(item.getItemId());
                    if (customItemViewType == CustomViewBinder.NOT_HANDLED) continue;

                    // If the convertView wasn't previously used for the same item view type,
                    // set it back to null so that the custom binder isn't passed a view that it
                    // can't/shouldn't re-use.
                    if (convertView != null
                            && (int) convertView.getTag(R.id.cm_menu_item_view_type) != itemViewType) {
                        convertView = null;
                    }

                    convertView = binder.getView(
                            item, convertView, parent, mInflater, mAppMenuClickHandler, mHighlightedItemId);

                    if (binder.supportsEnterAnimation(item.getItemId())) {
                        convertView.setTag(R.id.cm_menu_item_enter_anim_id,
                                buildStandardItemEnterAnimator(convertView, position));
                    }

                    // This will ensure that the item is not highlighted when selected.
                    convertView.setEnabled(item.isEnabled());

                    bound = true;
                    break;
                }

                if (!bound) throw new AssertionError("No binder found for item.");

                break;
        }

        if (mHighlightedItemId != null && item.getItemId() == mHighlightedItemId) {
            ViewHighlighter.turnOnHighlight(convertView, false);
        } else {
            ViewHighlighter.turnOffHighlight(convertView);
        }
        convertView.setTag(R.id.cm_menu_item_view_type, itemViewType);

        return convertView;
    }

    private View createMenuItemRow(
            View convertView, ViewGroup parent, MenuItem item, int numItems, int itemViewType) {
        RowItemViewHolder holder;
        if (convertView == null
                || (int) convertView.getTag(R.id.cm_menu_item_view_type) != itemViewType) {
            holder = new RowItemViewHolder(numItems);
            convertView = mInflater.inflate(R.layout.cm_icon_row_menu_item, parent, false);

            // Save references to all the buttons.
            for (int i = 0; i < numItems; i++) {
                ImageButton view = convertView.findViewById(BUTTON_IDS[i]);
                holder.buttons[i] = view;
            }

            // Remove unused menu items.
            for (int j = numItems; j < 5; j++) {
                ((ViewGroup) convertView).removeView(convertView.findViewById(BUTTON_IDS[j]));
            }

            convertView.setTag(holder);
            convertView.setTag(R.id.cm_menu_item_enter_anim_id,
                    buildIconItemEnterAnimator(holder.buttons));
        } else {
            holder = (RowItemViewHolder) convertView.getTag();
        }

        for (int i = 0; i < numItems; i++) {
            setupImageButton(holder.buttons[i], item.getSubMenu().getItem(i));
        }
        convertView.setFocusable(false);
        convertView.setEnabled(false);
        return convertView;
    }

    private int getCustomItemViewType(MenuItem item) {
        if (mCustomViewBinders == null) return CustomViewBinder.NOT_HANDLED;
        for (int i = 0; i < mCustomViewBinders.size(); i++) {
            CustomViewBinder binder = mCustomViewBinders.get(i);
            int binderViewType = binder.getItemViewType(item.getItemId());
            if (binderViewType != CustomViewBinder.NOT_HANDLED) {
                return binderViewType + mViewTypeOffsetMap.get(binder);
            }
        }
        return CustomViewBinder.NOT_HANDLED;
    }

    static class StandardMenuItemViewHolder {
        TextView text;
        ChromeImageView image;
    }

    private static class RowItemViewHolder {
        final ImageButton[] buttons;

        RowItemViewHolder(int numButtons) {
            buttons = new ImageButton[numButtons];
        }
    }

    Map<CustomViewBinder, Integer> getViewTypeOffsetMapForTests() {
        return mViewTypeOffsetMap;
    }

    @IntDef({MenuItemType.STANDARD, MenuItemType.TITLE_BUTTON, MenuItemType.THREE_BUTTON,
            MenuItemType.FOUR_BUTTON, MenuItemType.FIVE_BUTTON})
    @Retention(RetentionPolicy.SOURCE)
    private @interface MenuItemType {
        /**
         * Regular Android menu item that contains a title and an icon if icon is specified.
         */
        int STANDARD = 0;
        /**
         * Menu item that has two buttons, the first one is a title and the second one is an icon.
         * It is different from the regular menu item because it contains two separate buttons.
         */
        int TITLE_BUTTON = 1;
        /**
         * Menu item that has three buttons. Every one of these buttons is displayed as an icon.
         */
        int THREE_BUTTON = 2;
        /**
         * Menu item that has four buttons. Every one of these buttons is displayed as an icon.
         */
        int FOUR_BUTTON = 3;
        /**
         * Menu item that has five buttons. Every one of these buttons is displayed as an icon.
         */
        int FIVE_BUTTON = 4;
        /**
         * The number of view types specified above.  If you add a view type you MUST increment
         * this.
         */
        int NUM_ENTRIES = 5;
    }

    private static class TitleButtonMenuItemViewHolder {
        TextViewWithCompoundDrawables title;
        AppMenuItemIcon checkbox;
        ImageButton button;
    }
}
