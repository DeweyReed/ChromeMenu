package xyz.aprildown.chromemenu.app

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.SubMenu
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.MenuItemCompat
import kotlinx.android.synthetic.main.activity_main.*
import xyz.aprildown.chromemenu.*
import kotlin.random.Random

class MainActivity : AppCompatActivity(), AppMenuPropertiesDelegate {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnSimple.setOnTouchListener(
            AppMenuButtonHelper(
                AppMenuHandler(this, AbstractAppMenuPropertiesDelegate(), R.menu.menu_simple)
            )
        )

        val handler = AppMenuHandler(this, this, R.menu.menu)
        val helper = AppMenuButtonHelper(handler)
        btnAdvanced.setOnTouchListener(helper)
    }

    override fun shouldShowAppMenu(): Boolean = true

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

    override fun prepareMenu(menu: Menu) {

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

    override fun onHeaderViewInflated(appMenu: AppMenu, view: View) {
        view.setOnClickListener {
            Toast.makeText(this, "Header", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onFooterViewInflated(appMenu: AppMenu, view: View) {
        view.setOnClickListener {
            Toast.makeText(this, "Footer", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMenuItemClicked(item: MenuItem) {
        Toast.makeText(this, item.title.toString(), Toast.LENGTH_SHORT).show()
    }
}
