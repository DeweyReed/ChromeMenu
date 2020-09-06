// Copyright 2013 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package xyz.aprildown.chromemenu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupMenu;

import java.util.ArrayList;

/**
 * Object responsible for handling the creation, showing, hiding of the AppMenu and notifying the
 * AppMenuObservers about these actions.
 */
class AppMenuHandlerImpl implements AppMenuHandler {
    private final AppMenuDelegate mAppMenuDelegate;
    private AppMenu mAppMenu;
    private AppMenuDragHelper mAppMenuDragHelper;
    private final ArrayList<AppMenuObserver> mObservers;
    private final int mMenuResourceId;

    private final AppMenuPropertiesDelegate mDelegate;
    private Menu mMenu;
    private final View mDecorView;

    /**
     * The resource id of the menu item to highlight when the menu next opens. A value of
     * {@code null} means no item will be highlighted.  This value will be cleared after the menu is
     * opened.
     */
    private Integer mHighlightMenuId;

    /**
     * Whether the highlighted item should use a circle highlight or not.
     */
    private boolean mCircleHighlight;

    /**
     * Constructs an AppMenuHandlerImpl object.
     *
     * @param delegate        Delegate used to check the desired AppMenu properties on show.
     * @param appMenuDelegate The AppMenuDelegate to handle menu item selection.
     * @param menuResourceId  Resource Id that should be used as the source for the menu items.
     *                        It is assumed to have back_menu_id, forward_menu_id, bookmark_this_page_id.
     * @param decorView       The decor {@link View}, e.g. from Window#getDecorView(), for the containing
     *                        activity.
     */
    AppMenuHandlerImpl(AppMenuPropertiesDelegate delegate, AppMenuDelegate appMenuDelegate,
                       int menuResourceId, View decorView) {
        mAppMenuDelegate = appMenuDelegate;
        mDelegate = delegate;
        mDecorView = decorView;
        mObservers = new ArrayList<>();
        mMenuResourceId = menuResourceId;
    }

    /**
     * Called when the containing activity is being destroyed.
     */
    void destroy() {
        // Prevent the menu window from leaking.
        hideAppMenu();
    }

    @Override
    public void menuItemContentChanged(int menuRowId) {
        if (mAppMenu != null) mAppMenu.menuItemContentChanged(menuRowId);
    }

    @Override
    public void clearMenuHighlight() {
        setMenuHighlight(null, false);
    }

    @Override
    public void setMenuHighlight(Integer highlightItemId, boolean circleHighlight) {
        if (mHighlightMenuId == null && highlightItemId == null) return;
        if (mHighlightMenuId != null && mHighlightMenuId.equals(highlightItemId)) return;
        mHighlightMenuId = highlightItemId;
        mCircleHighlight = circleHighlight;
        boolean highlighting = mHighlightMenuId != null;
        for (AppMenuObserver observer : mObservers) observer.onMenuHighlightChanged(highlighting);
    }

    /**
     * Show the app menu.
     *
     * @param anchorView     Anchor view (usually a menu button) to be used for the popup, if null is
     *                       passed then hardware menu button anchor will be used.
     * @param startDragging  Whether dragging is started. For example, if the app menu is showed by
     *                       tapping on a button, this should be false. If it is showed by start
     *                       dragging down on the menu button, this should be true. Note that if
     *                       anchorView is null, this must be false since we no longer support
     *                       hardware menu button dragging.
     * @param showFromBottom Whether the menu should be shown from the bottom up.
     * @return True, if the menu is shown, false, if menu is not shown, example
     * reasons: the menu is not yet available to be shown, or the menu is
     * already showing.
     */
    // TODO(crbug.com/635567): Fix this properly.
    @SuppressLint("ResourceType")
    boolean showAppMenu(View anchorView, boolean startDragging, boolean showFromBottom) {
        if (!shouldShowAppMenu() || isAppMenuShowing()) return false;

        Context context = mDecorView.getContext();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int rotation = wm.getDefaultDisplay().getRotation();
//        if (anchorView == null) {
//            // This fixes the bug where the bottom of the menu starts at the top of
//            // the keyboard, instead of overlapping the keyboard as it should.
//            int displayHeight = context.getResources().getDisplayMetrics().heightPixels;
//            Rect rect = new Rect();
//            mDecorView.getWindowVisibleDisplayFrame(rect);
//            int statusBarHeight = rect.top;
//            mHardwareButtonMenuAnchor.setY((displayHeight - statusBarHeight));
//
//            anchorView = mHardwareButtonMenuAnchor;
//            isByPermanentButton = true;
//        }
        // If the anchor view used to show the popup or the activity's decor view is not attached
        // to window, we don't show the app menu because the window manager might have revoked
        // the window token for this activity. See https://crbug.com/1105831.
        if (!mDecorView.isAttachedToWindow() || !anchorView.isAttachedToWindow()
                || !anchorView.getRootView().isAttachedToWindow()) {
            return false;
        }

        if (mMenu == null) {
            // Use a PopupMenu to create the Menu object. Note this is not the same as the
            // AppMenu (mAppMenu) created below.
            PopupMenu tempMenu = new PopupMenu(context, anchorView);
            tempMenu.inflate(mMenuResourceId);
            mMenu = tempMenu.getMenu();
        }
        mDelegate.prepareMenu(mMenu, this);

        ContextThemeWrapper wrapper = new ContextThemeWrapper(context, R.style.CmOverflowMenuThemeOverlay);

        if (mAppMenu == null) {
            TypedArray a = wrapper.obtainStyledAttributes(new int[]
                    {android.R.attr.listPreferredItemHeightSmall, android.R.attr.listDivider});
            int itemRowHeight = a.getDimensionPixelSize(0, 0);
            Drawable itemDivider = a.getDrawable(1);
            int itemDividerHeight = itemDivider != null ? itemDivider.getIntrinsicHeight() : 0;
            a.recycle();
            mAppMenu = new AppMenu(mMenu, itemRowHeight, itemDividerHeight, this,
                    context.getResources(), mDelegate.shouldShowIconBeforeItem());
            mAppMenuDragHelper = new AppMenuDragHelper(context, mAppMenu, itemRowHeight);
        }

        // Get the height and width of the display.
        Rect appRect = new Rect();
        mDecorView.getWindowVisibleDisplayFrame(appRect);

        // Use full size of window for abnormal appRect.
        if (appRect.left < 0 && appRect.top < 0) {
            appRect.left = 0;
            appRect.top = 0;
            appRect.right = mDecorView.getWidth();
            appRect.bottom = mDecorView.getHeight();
        }
        Point pt = new Point();
        wm.getDefaultDisplay().getSize(pt);

        int footerResourceId = 0;
        if (mDelegate.shouldShowFooter(appRect.height())) {
            footerResourceId = mDelegate.getFooterResourceId();
        }
        int headerResourceId = 0;
        if (mDelegate.shouldShowHeader(appRect.height())) {
            headerResourceId = mDelegate.getHeaderResourceId();
        }
        mAppMenu.show(wrapper, anchorView, rotation, appRect, pt.y,
                footerResourceId, headerResourceId, mHighlightMenuId, mCircleHighlight,
                showFromBottom, mDelegate.getCustomViewBinders());
        mAppMenuDragHelper.onShow(startDragging);
        clearMenuHighlight();
        return true;
    }

    void appMenuDismissed() {
        mAppMenuDragHelper.finishDragging();
        mDelegate.onMenuDismissed();
    }

    @Override
    public boolean isAppMenuShowing() {
        return mAppMenu != null && mAppMenu.isShowing();
    }

    /**
     * @return The App Menu that the menu handler is interacting with.
     */
    public AppMenu getAppMenu() {
        return mAppMenu;
    }

    AppMenuDragHelper getAppMenuDragHelper() {
        return mAppMenuDragHelper;
    }

    @Override
    public void hideAppMenu() {
        if (mAppMenu != null && mAppMenu.isShowing()) mAppMenu.dismiss();
    }

    @Override
    public AppMenuButtonHelper createAppMenuButtonHelper() {
        return new AppMenuButtonHelperImpl(this);
    }

    @Override
    public void invalidateAppMenu() {
        if (mAppMenu != null) mAppMenu.invalidate();
    }

    @Override
    public void addObserver(AppMenuObserver observer) {
        mObservers.add(observer);
    }

    @Override
    public void removeObserver(AppMenuObserver observer) {
        mObservers.remove(observer);
    }

    void onOptionsItemSelected(MenuItem item) {
        mAppMenuDelegate.onOptionsItemSelected(item, mDelegate.getBundleForMenuItem(item));
    }

    /**
     * Called by AppMenu to report that the App Menu visibility has changed.
     *
     * @param isVisible Whether the App Menu is showing.
     */
    void onMenuVisibilityChanged(boolean isVisible) {
        for (int i = 0; i < mObservers.size(); ++i) {
            mObservers.get(i).onMenuVisibilityChanged(isVisible);
        }
    }

    /**
     * A notification that the header view has been inflated.
     *
     * @param view The inflated view.
     */
    void onHeaderViewInflated(View view) {
        if (mDelegate != null) mDelegate.onHeaderViewInflated(this, view);
    }

    /**
     * A notification that the footer view has been inflated.
     *
     * @param view The inflated view.
     */
    void onFooterViewInflated(View view) {
        if (mDelegate != null) mDelegate.onFooterViewInflated(this, view);
    }

    private boolean shouldShowAppMenu() {
        return mAppMenuDelegate.shouldShowAppMenu();
    }
}