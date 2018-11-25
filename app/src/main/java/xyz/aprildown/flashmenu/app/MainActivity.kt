package xyz.aprildown.flashmenu.app

import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import xyz.aprildown.flashmenu.AppMenu
import xyz.aprildown.flashmenu.AppMenuButtonHelper
import xyz.aprildown.flashmenu.AppMenuHandler
import xyz.aprildown.flashmenu.AppMenuPropertiesDelegate

class MainActivity : AppCompatActivity(), AppMenuPropertiesDelegate {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView.setOnTouchListener(
            AppMenuButtonHelper(
                AppMenuHandler(this, this, R.menu.menu)
            )
        )
    }

    override fun shouldShowAppMenu(): Boolean = true

    override fun prepareMenu(menu: Menu?) {

    }

    override fun getFooterResourceId(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getHeaderResourceId(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun shouldShowFooter(maxMenuHeight: Int): Boolean = false

    override fun shouldShowHeader(maxMenuHeight: Int): Boolean = false

    override fun onHeaderViewInflated(appMenu: AppMenu?, view: View?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onFooterViewInflated(appMenu: AppMenu?, view: View?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
