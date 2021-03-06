// Copyright 2011 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package xyz.aprildown.chromemenu;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.SystemClock;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.core.util.Pair;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * Shows a popup of menuitems anchored to a host view. When a item is selected we call
 * AppMenuPropertiesDelegate.onMenuItemClicked with the appropriate MenuItem.
 * - Only visible MenuItems are shown.
 * - Disabled items are grayed out.
 */
class AppMenu implements OnItemClickListener, OnKeyListener, AppMenuClickHandler {

    private static final float LAST_ITEM_SHOW_FRACTION = 0.5f;
    @VisibleForTesting
    static final long RECENT_SELECTED_MENUITEM_EXPIRATION_MS = 10 * DateUtils.SECOND_IN_MILLIS;

    private final Menu mMenu;
    private final int mItemRowHeight;
    private final int mVerticalFadeDistance;
    private final int mNegativeSoftwareVerticalOffset;
    private final int mNegativeVerticalOffsetNotTopAnchored;
    private final int[] mTempLocation;
    private final boolean mIconBeforeItem;

    private PopupWindow mPopup;
    private ListView mListView;
    private AppMenuAdapter mAdapter;
    @VisibleForTesting
    AppMenuHandlerImpl mHandler;
    private boolean mIsByPermanentButton;
    private AnimatorSet mMenuItemEnterAnimator;

    // Selected menu item id and the timestamp.
    private final Queue<Pair<Integer, Long>> mRecentSelectedMenuItems = new ArrayDeque<>();

    /**
     * Creates and sets up the App Menu.
     *
     * @param menu              Original menu created by the framework.
     * @param itemRowHeight     Desired height for each app menu row.
     * @param handler           AppMenuHandlerImpl receives callbacks from AppMenu.
     * @param res               Resources object used to get dimensions and style attributes.
     * @param iconBeforeItem    Whether icon is shown before the text.
     */
    AppMenu(Menu menu, int itemRowHeight, AppMenuHandlerImpl handler,
            Resources res, boolean iconBeforeItem) {
        mMenu = menu;

        mItemRowHeight = itemRowHeight;
        if (mItemRowHeight <= 0) {
            throw new IllegalArgumentException("ItemRowHeight must be positive");
        }

        mHandler = handler;

        mNegativeSoftwareVerticalOffset =
                res.getDimensionPixelSize(R.dimen.cm_menu_negative_software_vertical_offset);
        mVerticalFadeDistance = res.getDimensionPixelSize(R.dimen.cm_menu_vertical_fade_distance);
        mNegativeVerticalOffsetNotTopAnchored =
                res.getDimensionPixelSize(R.dimen.cm_menu_negative_vertical_offset_not_top_anchored);

        mTempLocation = new int[2];

        mIconBeforeItem = iconBeforeItem;
    }

    /**
     * Shows a toast anchored on a view.
     *
     * @param context     The context to use for the toast.
     * @param view        The view to anchor the toast.
     * @param description The string shown in the toast.
     * @return Whether a toast has been shown successfully.
     */
    @SuppressLint("RtlHardcoded")
    private static boolean showAnchoredToast(Context context, View view, CharSequence description) {
        if (description == null) return false;

        final int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        final int screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        final int[] screenPos = new int[2];
        view.getLocationOnScreen(screenPos);
        final int width = view.getWidth();
        final int height = view.getHeight();

        final int horizontalGravity =
                (screenPos[0] < screenWidth / 2) ? Gravity.LEFT : Gravity.RIGHT;
        final int xOffset = (screenPos[0] < screenWidth / 2)
                ? screenPos[0] + width / 2
                : screenWidth - screenPos[0] - width / 2;
        final int yOffset = (screenPos[1] < screenHeight / 2) ? screenPos[1] + height / 2
                : screenPos[1] - height * 3 / 2;

        Toast toast = Toast.makeText(context, description, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP | horizontalGravity, xOffset, yOffset);
        toast.show();
        return true;
    }

    /**
     * Notifies the menu that the contents of the menu item specified by {@code menuRowId} have
     * changed.  This should be called if icons, titles, etc. are changing for a particular menu
     * item while the menu is open.
     *
     * @param menuRowId The id of the menu item to change.  This must be a row id and not a child
     *                  id.
     */
    void menuItemContentChanged(int menuRowId) {
        // Make sure we have all the valid state objects we need.
        if (mAdapter == null || mMenu == null || mPopup == null || mListView == null) {
            return;
        }

        // Calculate the item index.
        int index = -1;
        int menuSize = mMenu.size();
        for (int i = 0; i < menuSize; i++) {
            if (mMenu.getItem(i).getItemId() == menuRowId) {
                index = i;
                break;
            }
        }
        if (index == -1) return;

        // Check if the item is visible.
        int startIndex = mListView.getFirstVisiblePosition();
        int endIndex = mListView.getLastVisiblePosition();
        if (index < startIndex || index > endIndex) return;

        // Grab the correct View.
        View view = mListView.getChildAt(index - startIndex);
        if (view == null) return;

        // Cause the Adapter to re-populate the View.
        mListView.getAdapter().getView(index, view, mListView);
    }

    private static int[] getPopupPosition(int[] tempLocation, boolean isByPermanentButton,
                                          int negativeSoftwareVerticalOffset, int negativeVerticalOffsetNotTopAnchored,
                                          int screenRotation, Rect appRect, Rect padding, View anchorView, int popupWidth,
                                          int popupHeight, int viewLayoutDirection) {
        anchorView.getLocationInWindow(tempLocation);
        int anchorViewX = tempLocation[0];
        int anchorViewY = tempLocation[1];

        int[] offsets = new int[2];
        // If we have a hardware menu button, locate the app menu closer to the estimated
        // hardware menu button location.
        if (isByPermanentButton) {
            int horizontalOffset = -anchorViewX;
            switch (screenRotation) {
                case Surface.ROTATION_0:
                case Surface.ROTATION_180:
                    horizontalOffset += (appRect.width() - popupWidth) / 2;
                    break;
                case Surface.ROTATION_90:
                    horizontalOffset += appRect.width() - popupWidth;
                    break;
                case Surface.ROTATION_270:
                    break;
                default:
                    throw new IllegalStateException("Unknown screenRotation: " + screenRotation);
            }
            offsets[0] = horizontalOffset;
            // The menu is displayed above the anchored view, so shift the menu up by the bottom
            // padding of the background.
            offsets[1] = -padding.bottom;
        } else {
            offsets[1] = -negativeSoftwareVerticalOffset;
            if (viewLayoutDirection != View.LAYOUT_DIRECTION_RTL) {
                offsets[0] = anchorView.getWidth() - popupWidth;
            }
        }

        int xPos = anchorViewX + offsets[0];
        int yPos = anchorViewY + offsets[1];
        return new int[]{xPos, yPos};
    }

    /**
     * Handles clicks on the AppMenu popup.
     *
     * @param menuItem The menu item in the popup that was clicked.
     */
    @Override
    public void onItemClick(MenuItem menuItem) {
        if (menuItem.isEnabled()) {
            recordSelectedMenuItem(menuItem.getItemId(), SystemClock.elapsedRealtime());
            dismiss();
            mHandler.onOptionsItemSelected(menuItem);
        }
    }

    /**
     * Creates and shows the app menu anchored to the specified view.
     *
     * @param context             The context of the AppMenu (ensure the proper theme is set on
     *                            this context).
     * @param anchorView          The anchor {@link View} of the {@link PopupWindow}.
     * @param screenRotation      Current device screen rotation.
     * @param visibleDisplayFrame The display area rect in which AppMenu is supposed to fit in.
     * @param screenHeight        Current device screen height.
     * @param footerResourceId    The resource id for a view to add as a fixed view at the bottom
     *                            of the menu.  Can be 0 if no such view is required.  The footer
     *                            is always visible and overlays other app menu items if
     *                            necessary.
     * @param headerResourceId    The resource id for a view to add as the first item in menu
     *                            list. Can be null if no such view is required. See
     *                            {@link ListView#addHeaderView(View)}.
     * @param highlightedItemId   The resource id of the menu item that should be highlighted.
     *                            Can be {@code null} if no item should be highlighted.  Note that
     *                            {@code 0} is dedicated to custom menu items and can be declared
     *                            by external apps.
     * @param groupDividerResourceId     The resource id of divider menu items. This will be used to
     *         determine the number of dividers that appear in the menu.
     * @param customViewBinders   See {@link AppMenuPropertiesDelegate#getCustomViewBinders()}.
     */
    void show(Context context, final View anchorView,
              int screenRotation, Rect visibleDisplayFrame, int screenHeight,
              @IdRes int footerResourceId, @IdRes int headerResourceId,
              @IdRes int groupDividerResourceId, Integer highlightedItemId,
              @Nullable List<CustomViewBinder> customViewBinders) {
        mPopup = new PopupWindow(context);
        mPopup.setFocusable(true);
        mPopup.setInputMethodMode(PopupWindow.INPUT_METHOD_NOT_NEEDED);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // The window layout type affects the z-index of the popup window on M+.
            mPopup.setWindowLayoutType(WindowManager.LayoutParams.TYPE_APPLICATION_SUB_PANEL);
        }

        mPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (anchorView instanceof ImageButton) {
                    anchorView.setSelected(false);
                }

                if (mMenuItemEnterAnimator != null) mMenuItemEnterAnimator.cancel();

                mHandler.appMenuDismissed();
                mHandler.onMenuVisibilityChanged(false);

                mPopup = null;
                mAdapter = null;
                mListView = null;
                mMenuItemEnterAnimator = null;
            }
        });

        // Some OEMs don't actually let us change the background... but they still return the
        // padding of the new background, which breaks the menu height.  If we still have a
        // drawable here even though our style says @null we should use this padding instead...
        Drawable originalBgDrawable = mPopup.getBackground();

        // Need to explicitly set the background here.  Relying on it being set in the style caused
        // an incorrectly drawn background.
        mPopup.setBackgroundDrawable(ApiCompatibilityUtils.getDrawable(
                context.getResources(), R.drawable.cm_popup_bg_tinted)
        );
        mPopup.setAnimationStyle(R.style.CmOverflowMenuAnim);

        // Turn off window animations for low end devices.
        if (SysUtils.isLowEndDevice()) mPopup.setAnimationStyle(0);

        Rect bgPadding = new Rect();
        mPopup.getBackground().getPadding(bgPadding);

        int menuWidth = context.getResources().getDimensionPixelSize(R.dimen.cm_menu_width);
        int popupWidth = menuWidth + bgPadding.left + bgPadding.right;

        mPopup.setWidth(popupWidth);

        mIsByPermanentButton = false;

        // Extract visible items from the Menu.
        List<MenuItem> menuItems = new ArrayList<>();
        List<Integer> heightList = new ArrayList<>();
        for (int i = 0; i < mMenu.size(); ++i) {
            MenuItem item = mMenu.getItem(i);
            if (item.isVisible()) {
                menuItems.add(item);
                heightList.add(getMenuItemHeight(item, context, customViewBinders));
            }
        }

        Rect sizingPadding = new Rect(bgPadding);

        // A List adapter for visible items in the Menu. The first row is added as a header to the
        // list view.
        mAdapter = new AppMenuAdapter(
                this, menuItems, LayoutInflater.from(context), highlightedItemId, customViewBinders, mIconBeforeItem);

        @SuppressLint("InflateParams") ViewGroup contentView =
                (ViewGroup) LayoutInflater.from(context).inflate(R.layout.cm_app_menu_layout, null);
        mListView = contentView.findViewById(R.id.app_menu_list);

        int footerHeight = inflateFooter(footerResourceId, contentView, menuWidth);

        int headerHeight = inflateHeader(headerResourceId, contentView, menuWidth);

        if (highlightedItemId != null
                && (highlightedItemId == footerResourceId
                || highlightedItemId == headerResourceId)) {
            View viewToHighlight = contentView.findViewById(highlightedItemId);
            ViewHighlighter.turnOnRectangularHighlight(viewToHighlight);
        }

        // Set the adapter after the header is added to avoid crashes on JellyBean.
        // See crbug.com/761726.
        mListView.setAdapter(mAdapter);

        int popupHeight = setMenuHeight(menuItems, heightList, visibleDisplayFrame, screenHeight,
                sizingPadding, footerHeight, headerHeight, anchorView, groupDividerResourceId);
        int[] popupPosition = getPopupPosition(mTempLocation, mIsByPermanentButton,
                mNegativeSoftwareVerticalOffset, mNegativeVerticalOffsetNotTopAnchored,
                screenRotation, visibleDisplayFrame, sizingPadding, anchorView, popupWidth,
                popupHeight, anchorView.getRootView().getLayoutDirection());

        mPopup.setContentView(contentView);
        mPopup.showAtLocation(
                anchorView.getRootView(), Gravity.NO_GRAVITY, popupPosition[0], popupPosition[1]);

        mListView.setOnItemClickListener(this);
        mListView.setItemsCanFocus(true);
        mListView.setOnKeyListener(this);

        mHandler.onMenuVisibilityChanged(true);

        if (mVerticalFadeDistance > 0) {
            mListView.setVerticalFadingEdgeEnabled(true);
            mListView.setFadingEdgeLength(mVerticalFadeDistance);
        }

        // Don't animate the menu items for low end devices.
        if (!SysUtils.isLowEndDevice()) {
            mListView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                           int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    mListView.removeOnLayoutChangeListener(this);
                    runMenuItemEnterAnimations();
                }
            });
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        onItemClick(mAdapter.getItem(position));
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (mListView == null) return false;

        if (event.getKeyCode() == KeyEvent.KEYCODE_MENU) {
            if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
                event.startTracking();
                v.getKeyDispatcherState().startTracking(event, this);
                return true;
            } else if (event.getAction() == KeyEvent.ACTION_UP) {
                v.getKeyDispatcherState().handleUpEvent(event);
                if (event.isTracking() && !event.isCanceled()) {
                    dismiss();
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Dismisses the app menu and cancels the drag-to-scroll if it is taking place.
     */
    void dismiss() {
        if (isShowing()) {
            mPopup.dismiss();
        }
    }

    /**
     * @return Whether the app menu is currently showing.
     */
    boolean isShowing() {
        if (mPopup == null) {
            return false;
        }
        return mPopup.isShowing();
    }

    /**
     * @return {@link PopupWindow} that displays all the menu options and optional footer.
     */
    PopupWindow getPopup() {
        return mPopup;
    }

    /**
     * @return {@link ListView} that contains all of the menu options.
     */
    ListView getListView() {
        return mListView;
    }

    /**
     * @return The menu instance inside of this class.
     */
    Menu getMenu() {
        return mMenu;
    }

    /**
     * Invalidate the app menu data. See {@link AppMenuAdapter#notifyDataSetChanged}.
     */
    void invalidate() {
        if (mAdapter != null) mAdapter.notifyDataSetChanged();
    }

    private int setMenuHeight(List<MenuItem> menuItems, List<Integer> heightList,
                              Rect appDimensions, int screenHeight, Rect padding, int footerHeight, int headerHeight,
                              View anchorView, @IdRes int groupDividerResourceId) {
        anchorView.getLocationOnScreen(mTempLocation);
        int anchorViewY = mTempLocation[1] - appDimensions.top;

        int anchorViewImpactHeight = mIsByPermanentButton ? anchorView.getHeight() : 0;

        // Set appDimensions.height() for abnormal anchorViewLocation.
        if (anchorViewY > screenHeight) {
            anchorViewY = appDimensions.height();
        }
        int availableScreenSpace = Math.max(
                anchorViewY, appDimensions.height() - anchorViewY - anchorViewImpactHeight);

        availableScreenSpace -= (padding.bottom + footerHeight + headerHeight);
        if (mIsByPermanentButton) availableScreenSpace -= padding.top;

        int menuHeight = calculateHeightForItems(
                menuItems, heightList, groupDividerResourceId, availableScreenSpace);
        menuHeight += footerHeight + headerHeight + padding.top + padding.bottom;
        mPopup.setHeight(menuHeight);
        return menuHeight;
    }

    @VisibleForTesting
    int calculateHeightForItems(List<MenuItem> menuItems, List<Integer> heightList,
                                @IdRes int groupDividerResourceId, int availableScreenSpace) {
        int spaceForFullItems = 0;
        for (int i = 0; i < heightList.size(); i++) {
            spaceForFullItems += heightList.get(i);
        }

        int menuHeight;
        // Fade out the last item if we cannot fit all items.
        if (availableScreenSpace < spaceForFullItems) {
            int spaceForItems = 0;
            int lastItem = 0;
            // App menu should show 1 full item at least.
            do {
                spaceForItems += heightList.get(lastItem++);
                if (spaceForItems + heightList.get(lastItem) > availableScreenSpace) {
                    break;
                }
            } while (lastItem < heightList.size() - 1);

            int spaceForPartialItem = (int) (LAST_ITEM_SHOW_FRACTION * heightList.get(lastItem));
            // Determine which item needs hiding. We only show Partial of the last item, if there is
            // not enough screen space to partially show the last identified item, then partially
            // show the second to last item instead. We also do not show the partial divider line.
            assert menuItems.size() == heightList.size();
            while (lastItem > 1
                    && (spaceForItems + spaceForPartialItem > availableScreenSpace
                    || menuItems.get(lastItem).getItemId() == groupDividerResourceId)) {
                spaceForItems -= heightList.get(lastItem - 1);
                spaceForPartialItem =
                        (int) (LAST_ITEM_SHOW_FRACTION * heightList.get(lastItem - 1));
                lastItem--;
            }

            menuHeight = spaceForItems + spaceForPartialItem;
        } else {
            menuHeight = spaceForFullItems;
        }
        return menuHeight;
    }

    private void runMenuItemEnterAnimations() {
        mMenuItemEnterAnimator = new AnimatorSet();
        AnimatorSet.Builder builder = null;

        ViewGroup list = mListView;
        for (int i = 0; i < list.getChildCount(); i++) {
            View view = list.getChildAt(i);
            Object animatorObject = view.getTag(R.id.cm_menu_item_enter_anim_id);
            if (animatorObject != null) {
                if (builder == null) {
                    builder = mMenuItemEnterAnimator.play((Animator) animatorObject);
                } else {
                    builder.with((Animator) animatorObject);
                }
            }
        }

        mMenuItemEnterAnimator.start();
    }

    private int inflateFooter(
            int footerResourceId, View contentView, int menuWidth) {
        if (footerResourceId == 0) {
            return 0;
        }

        ViewStub footerStub = contentView.findViewById(R.id.app_menu_footer_stub);
        footerStub.setLayoutResource(footerResourceId);
        View mFooterView = footerStub.inflate();

        int widthMeasureSpec = MeasureSpec.makeMeasureSpec(menuWidth, MeasureSpec.EXACTLY);
        int heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        mFooterView.measure(widthMeasureSpec, heightMeasureSpec);

        if (mHandler != null) mHandler.onFooterViewInflated(mFooterView);

        return mFooterView.getMeasuredHeight();
    }

    private int inflateHeader(int headerResourceId, View contentView, int menuWidth) {
        if (headerResourceId == 0) return 0;

        View headerView = LayoutInflater.from(contentView.getContext())
                .inflate(headerResourceId, mListView, false);
        mListView.addHeaderView(headerView);

        int widthMeasureSpec = MeasureSpec.makeMeasureSpec(menuWidth, MeasureSpec.EXACTLY);
        int heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        headerView.measure(widthMeasureSpec, heightMeasureSpec);

        if (mHandler != null) mHandler.onHeaderViewInflated(headerView);

        return headerView.getMeasuredHeight();
    }

    /**
     * Handles long clicks on image buttons on the AppMenu popup.
     *
     * @param menuItem The menu item in the popup that was long clicked.
     * @param view     The anchor view of the menu item.
     */
    @Override
    public boolean onItemLongClick(MenuItem menuItem, View view) {
        if (!menuItem.isEnabled()) return false;
        Context context = view.getContext();

        CharSequence titleCondensed = menuItem.getTitleCondensed();
        CharSequence message =
                TextUtils.isEmpty(titleCondensed) ? menuItem.getTitle() : titleCondensed;
        return showToastForItem(message, view);
    }

    private boolean showToastForItem(CharSequence message, View view) {
        Context context = view.getContext().getApplicationContext();
        return showAnchoredToast(context, view, message);
    }

    private int getMenuItemHeight(
            MenuItem item, Context context, @Nullable List<CustomViewBinder> customViewBinders) {
        // Check if |item| is custom type
        if (customViewBinders != null) {
            for (int i = 0; i < customViewBinders.size(); i++) {
                CustomViewBinder binder = customViewBinders.get(i);
                if (binder.getItemViewType(item.getItemId()) != CustomViewBinder.NOT_HANDLED) {
                    return binder.getPixelHeight(context);
                }
            }
        }
        return mItemRowHeight;
    }

    @VisibleForTesting
    void recordSelectedMenuItem(int menuItemId, long timestamp) {
        // Remove the selected MenuItems older than RECENT_SELECTED_MENUITEM_EXPIRATION_MS.
        while (!mRecentSelectedMenuItems.isEmpty()
                && (timestamp - mRecentSelectedMenuItems.peek().second
                > RECENT_SELECTED_MENUITEM_EXPIRATION_MS)) {
            mRecentSelectedMenuItems.remove();
        }
        recordSelectionSequence(menuItemId);

        mRecentSelectedMenuItems.add(new Pair<Integer, Long>(menuItemId, timestamp));
    }

    private void recordSelectionSequence(int menuItemId) {
        for (Pair<Integer, Long> previousSelectedMenuItem : mRecentSelectedMenuItems) {
            if (mHandler.recordAppMenuSimilarSelectionIfNeeded(
                    previousSelectedMenuItem.first, menuItemId)) {
                // Only record the similar selection once for one user action.
                return;
            }
        }
    }
}
