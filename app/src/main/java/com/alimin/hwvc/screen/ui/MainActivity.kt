package com.alimin.hwvc.screen.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION
import android.support.v7.app.AppCompatActivity
import com.alimin.hwvc.screen.AlDisplayService
import com.alimin.hwvc.screen.R
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            startActivityForResult(
                Intent(
                    ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                ), 0x02
            )
            return
        } else {
//            startService(new Intent(MainActivity.this, FloatingService.class));
        }

        startBtn.setOnClickListener {
            startService(Intent(this@MainActivity, AlDisplayService::class.java))
        }
    }
}
