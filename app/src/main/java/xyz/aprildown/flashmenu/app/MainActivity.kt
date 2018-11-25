package xyz.aprildown.flashmenu.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import xyz.aprildown.flashmenu.AbstractAppMenuPropertiesDelegate
import xyz.aprildown.flashmenu.AppMenuButtonHelper
import xyz.aprildown.flashmenu.AppMenuHandler

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView.setOnTouchListener(
            AppMenuButtonHelper(
                AppMenuHandler(this, AbstractAppMenuPropertiesDelegate(), R.menu.menu)
            )
        )
    }
}
