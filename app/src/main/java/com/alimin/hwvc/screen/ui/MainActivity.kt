package com.alimin.hwvc.screen.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import com.alimin.hwvc.screen.AlDisplayService
import com.alimin.hwvc.screen.BuildConfig
import com.alimin.hwvc.screen.R
import com.alimin.hwvc.screen.adapter.SettingsAdapter
import com.alimin.hwvc.screen.helper.PermissionHelper
import com.lmy.common.ext.setOnItemClickListener
import com.microsoft.officeuifabric.listitem.ListItemDivider
import com.microsoft.officeuifabric.popupmenu.PopupMenu
import com.microsoft.officeuifabric.popupmenu.PopupMenuItem
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : BasePreferenceActivity() {
    override val layoutResID: Int = R.layout.activity_main
    private val adapter = SettingsAdapter()

    override fun initView() {
        setDarkStatusBar()
        setSupportActionBar(toolbar)
        toolbar.subtitle = BuildConfig.VERSION_NAME
        if (!PermissionHelper.requestPermissions(this, PermissionHelper.PERMISSIONS_BASE))
            return
        recyclerView.adapter = adapter
        setup()

        recyclerView.addItemDecoration(ListItemDivider(this, DividerItemDecoration.VERTICAL))
        recyclerView.setOnItemClickListener { parent, view, position ->
            val v = view!!.findViewById<View>(R.id.valueView)
            when (position) {
                0 -> {
                    if (AlDisplayService.isRecording()) {
                        AlDisplayService.instance()?.shutdown()
//                        setup()
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(
                                this
                            )
                        ) {
                            startActivityForResult(
                                Intent(
                                    ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:$packageName")
                                ), 0x02
                            )
                        } else {
                            startService(Intent(this@MainActivity, AlDisplayService::class.java))
                        }
                    }
                }
                1 -> {
                    showPopupMenu(v, resources.getStringArray(R.array.array_quality)) {
                        (adapter.items[position] as ArrayList<String>)[2] = it.title
                        setQuality(it.id)
                        adapter.notifyDataSetChanged()
                    }
                }
                2 -> {
                    showPopupMenu(v, resources.getStringArray(R.array.array_eff)) {
                        (adapter.items[position] as ArrayList<String>)[2] = it.title
                        setEff(it.id)
                        adapter.notifyDataSetChanged()
                    }
                }
                3 -> {
                    showPopupMenu(v, resources.getStringArray(R.array.array_encode_type)) {
                        (adapter.items[position] as ArrayList<String>)[2] = it.title
                        setCodec(it.id)
                        adapter.notifyDataSetChanged()
                    }
                }
                4 -> {
                    showPopupMenu(v, resources.getStringArray(R.array.array_voice)) {
                        (adapter.items[position] as ArrayList<String>)[2] = it.title
                        setVoice(it.id)
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    private fun showPopupMenu(
        view: View,
        items: Array<String>,
        l: (it: PopupMenuItem) -> Unit
    ) {
        val its = ArrayList<PopupMenuItem>()
        items.forEachIndexed { i, it ->
            its.add(PopupMenuItem(i, it))
        }
        showPopupMenu(view, its, PopupMenu.ItemCheckableBehavior.NONE, l)
    }

    private fun setup() {
        val items = ArrayList<List<String>>()
        if (AlDisplayService.isRecording()) {
            addItem(items, "正在录屏中", "点击立即停止录屏", "")
        } else {
            addItem(items, "开始录屏", "点击立即开始录屏", "")
        }
        addItem(
            items,
            "质量",
            "质量越高，文件越大，建议设置为<中>",
            resources.getStringArray(R.array.array_quality)[getQuality()]
        )
        addItem(
            items,
            "效率",
            "如果录制出现掉帧，可以尝试更高的效率，高效意味着更大的文件，同时也会降低视频质量，建议设置为<中>",
            resources.getStringArray(R.array.array_eff)[getEff()]
        )
        addItem(
            items,
            "编码方式",
            "硬编效率最高，软编兼容性最好，如果硬编出问题可以尝试软编，否则建议设置为<硬编>",
            resources.getStringArray(R.array.array_encode_type)[getCodec()]
        )
        addItem(
            items,
            "声音来源",
            "旋转录制麦克风或者内置音频",
            resources.getStringArray(R.array.array_voice)[getVoice()]
        )
        adapter.clear()
        adapter.bindData(items)
        adapter.notifyDataSetChanged()
    }

    private fun addItem(
        items: ArrayList<List<String>>,
        title: String,
        body: String,
        value: String
    ) {
        val it = ArrayList<String>()
        it.add(title)
        it.add(body)
        it.add(value)
        items.add(it)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (null == grantResults || grantResults.isEmpty()) return
        when (requestCode) {
            PermissionHelper.REQUEST_MY -> {
                if (PermissionHelper.checkGrantResults(grantResults)) {
                    initView()
                } else {
                    showPermissionsDialog()
                }
            }
        }
    }

    private fun showPermissionsDialog() {
        AlertDialog.Builder(this)
            .setMessage("Please grant permission in the permission settings")
            .setNegativeButton("cancel") { dialog, which -> finish() }
            .setPositiveButton("enter") { dialog, which ->
                PermissionHelper.gotoPermissionManager(this@MainActivity)
                finish()
            }
            .show()
    }
}
