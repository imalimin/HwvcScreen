package com.alimin.hwvc.screen.ui

import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import com.alimin.hwvc.screen.AlDisplayService
import com.alimin.hwvc.screen.R
import com.alimin.hwvc.screen.helper.PermissionHelper
import com.lmy.common.ui.BaseActivity

class ReqActivity : BaseActivity() {
    override val layoutResID: Int = R.layout.activity_req
    private var mpm: MediaProjectionManager? = null
    private var dialog: AlertDialog? = null

    override fun initView() {
        checkPermissions()
    }

    override fun onRestart() {
        super.onRestart()
        super.onResume()
        checkPermissions()
    }

    private fun checkPermissions() {
        dialog?.dismiss()
        if (!reqPermissions()) {
            return
        }
        if (!reqOverlays()) {
            return
        }
        reqProjection()
    }

    private fun reqPermissions(): Boolean {
        if (!PermissionHelper.checkSelfPermission(this, PermissionHelper.PERMISSIONS_BASE)) {
            dialog = AlertDialog.Builder(this)
                .setMessage(R.string.msg_grant_permission_tip)
                .setNegativeButton(R.string.cancel) { dialog, which -> finish() }
                .setPositiveButton(R.string.enter) { dialog, which ->
                    if (!PermissionHelper.requestPermissions(
                            this,
                            PermissionHelper.PERMISSIONS_BASE
                        )
                    ) {
                        return@setPositiveButton
                    }
                }
                .show()
            return false
        }
        return true
    }

    private fun reqOverlays(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(
                this
            )
        ) {
            dialog = AlertDialog.Builder(this)
                .setMessage(R.string.msg_grant_overlays_permission)
                .setNegativeButton(R.string.cancel) { dialog, which -> finish() }
                .setPositiveButton(R.string.enter) { dialog, which ->
                    startActivityForResult(
                        Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:$packageName")
                        ), REQ_OVERLAYS
                    )
                }
                .show()
            return false
        }
        return true
    }

    private fun reqProjection(): Boolean {
        startService(Intent(this@ReqActivity, AlDisplayService::class.java))
        mpm = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val intent = mpm?.createScreenCaptureIntent()
        startActivityForResult(intent, REQ_PROJECTION)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (REQ_PROJECTION == requestCode) {
            if (data != null && RESULT_OK == resultCode) {
                AlDisplayService.instance()?.setup(mpm?.getMediaProjection(resultCode, data))
            } else {
                stopService(Intent(this@ReqActivity, AlDisplayService::class.java))
            }
            finish()
        } else if (REQ_OVERLAYS == requestCode) {

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isEmpty()) return
        when (requestCode) {
            PermissionHelper.REQUEST_MY -> {
                if (PermissionHelper.checkGrantResults(grantResults)) {
                    checkPermissions()
                } else {
                    showPermissionsDialog()
                }
            }
        }
    }

    private fun showPermissionsDialog() {
        dialog = AlertDialog.Builder(this)
            .setMessage(R.string.msg_grant_permission_pls)
            .setNegativeButton(R.string.cancel) { dialog, which -> finish() }
            .setPositiveButton(R.string.enter) { dialog, which ->
                PermissionHelper.gotoPermissionManager(this@ReqActivity)
                finish()
            }
            .show()
    }

    companion object {
        const val REQ_PROJECTION = 0x01
        const val REQ_OVERLAYS = 0x02
    }
}