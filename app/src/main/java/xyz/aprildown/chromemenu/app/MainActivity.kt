package xyz.aprildown.chromemenu.app

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.SubMenu
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.MenuItemCompat
import kotlinx.android.synthetic.main.activity_main.*
import xyz.aprildown.chromemenu.AbstractAppMenuPropertiesDelegate
import xyz.aprildown.chromemenu.AppMenuCoordinatorFactory
import xyz.aprildown.chromemenu.AppMenuDelegate
import xyz.aprildown.chromemenu.AppMenuHandler
import xyz.aprildown.chromemenu.AppMenuPropertiesDelegate
import xyz.aprildown.chromemenu.CustomViewBinder
import xyz.aprildown.chromemenu.MenuButtonDelegate
import kotlin.random.Random

class MainActivity : AppCompatActivity(), AppMenuPropertiesDelegate {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val coordinator = AppMenuCoordinatorFactory.createAppMenuCoordinator(
            this,
            object : MenuButtonDelegate {
                override fun getMenuButtonView(): View? = btnSimple
                override fun isMenuFromBottom(): Boolean = false
            },
            object : AppMenuDelegate {
                override fun onOptionsItemSelected(item: MenuItem, menuItemData: Bundle?): Boolean {
                    onMenuItemClicked(item)
                    return true
                }

                override fun createAppMenuPropertiesDelegate(): AppMenuPropertiesDelegate {
                    return object : AbstractAppMenuPropertiesDelegate() {
                        override fun getAppMenuLayoutId(): Int = R.menu.menu_simple
                    }
                }

                override fun shouldShowAppMenu(): Boolean = true
            },
            window.decorView
        )

        btnSimple.setOnTouchListener(
            coordinator.appMenuHandler.createAppMenuButtonHelper()
        )

        val coordinator1 = AppMenuCoordinatorFactory.createAppMenuCoordinator(
            this,
            object : MenuButtonDelegate {
                override fun getMenuButtonView(): View? = btnSimple
                override fun isMenuFromBottom(): Boolean = false
            },
            object : AppMenuDelegate {
                override fun onOptionsItemSelected(item: MenuItem, menuItemData: Bundle?): Boolean {
                    onMenuItemClicked(item)
                    return true
                }

                override fun createAppMenuPropertiesDelegate(): AppMenuPropertiesDelegate =
                    this@MainActivity

                override fun shouldShowAppMenu(): Boolean = true
            },
            window.decorView
        )
        btnAdvanced.setOnTouchListener(coordinator1.appMenuHandler.createAppMenuButtonHelper())

        btnMainNight.setOnClickListener {
            val default = AppCompatDelegate.getDefaultNightMode()
            if (default == AppCompatDelegate.MODE_NIGHT_YES) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            recreate()
        }
    }

    private val iconRes = arrayOf(
        R.drawable.ic_audiotrack_black_24dp,
        R.drawable.ic_brightness_5_black_24dp,
        R.drawable.ic_create_black_24dp,
        R.drawable.ic_favorite_black_24dp,
        R.drawable.ic_monetization_on_black_24dp
    )

    private val colorRes = arrayOf(
        android.R.color.holo_blue_dark,
        android.R.color.holo_orange_light,
        android.R.color.holo_red_light,
        android.R.color.holo_green_dark,
        android.R.color.holo_purple
    )

    override fun getCustomViewBinders(): List<CustomViewBinder>? {
        return null
    }

    override fun prepareMenu(menu: Menu, handler: AppMenuHandler) {

        fun MenuItem.randomTint() {
            setIcon(iconRes[Random.nextInt(iconRes.size)])
            MenuItemCompat.setIconTintList(
                this, ColorStateList.valueOf(
                    ContextCompat.getColor(
                        this@MainActivity,
                        colorRes[Random.nextInt(colorRes.size)]
                    )
                )
            )
        }

        fun SubMenu.randomTint() {
            for (i in 0 until size()) {
                getItem(i).randomTint()
            }
        }

        for (i in 0 until menu.size()) {
            val item = menu.getItem(i)
            if (item.hasSubMenu()) {
                item.subMenu.randomTint()
            } else {
                item.randomTint()
            }
        }
    }

    override fun getFooterResourceId(): Int {
        return R.layout.layout_footer
    }

    override fun getHeaderResourceId(): Int {
        return R.layout.layout_header
    }

    override fun shouldShowFooter(maxMenuHeight: Int): Boolean {
        return true
    }

    override fun shouldShowHeader(maxMenuHeight: Int): Boolean {
        return true
    }

    override fun getGroupDividerId(): Int {
        return 0
    }

    override fun shouldShowRegroupedMenu(): Boolean {
        return false
    }

    override fun destroy() {
    }

    override fun onFooterViewInflated(appMenuHandler: AppMenuHandler, view: View) {
        view.setOnClickListener {
            Toast.makeText(this, "Footer", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onHeaderViewInflated(appMenuHandler: AppMenuHandler, view: View) {
        view.setOnClickListener {
            Toast.makeText(this, "Header", Toast.LENGTH_SHORT).show()
        }
    }

    override fun shouldShowIconBeforeItem(): Boolean {
        return true
    }

    private fun onMenuItemClicked(item: MenuItem) {
        Toast.makeText(this, item.title.toString(), Toast.LENGTH_SHORT).show()
    }

    override fun getAppMenuLayoutId(): Int = R.menu.menu

    override fun getBundleForMenuItem(item: MenuItem): Bundle? = null

    override fun onMenuDismissed() = Unit
}
