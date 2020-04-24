package com.alimin.hwvc.screen.ui

import android.view.View
import com.alimin.hwvc.screen.MyApplication
import com.lmy.common.model.AlPreference
import com.lmy.common.ui.BaseActivity
import com.microsoft.officeuifabric.popupmenu.PopupMenu
import com.microsoft.officeuifabric.popupmenu.PopupMenuItem

abstract class BasePreferenceActivity : BaseActivity() {
    // 质量
    private var pQuality by AlPreference(MyApplication.instance.applicationContext, "Quality", 2)
    // 效率
    private var pEff by AlPreference(MyApplication.instance.applicationContext, "Eff", 2)
    // 声音
    private var pVoice by AlPreference(MyApplication.instance.applicationContext, "Voice", 0)
    // 编码方式
    private var pCodec by AlPreference(MyApplication.instance.applicationContext, "Codec", 0)

    fun getQuality(): Int = pQuality
    fun setQuality(value: Int) {
        pQuality = value
    }

    fun getEff(): Int = pEff
    fun setEff(value: Int) {
        pEff = value
    }

    fun getVoice(): Int = pVoice
    fun setVoice(value: Int) {
        pVoice = value
    }

    fun getCodec(): Int = pCodec
    fun setCodec(value: Int) {
        pCodec = value
    }

    fun showPopupMenu(
        anchorView: View,
        items: ArrayList<PopupMenuItem>,
        itemCheckableBehavior: PopupMenu.ItemCheckableBehavior,
        l: (it: PopupMenuItem) -> Unit
    ) {
        val popupMenu = PopupMenu(this, anchorView, items, itemCheckableBehavior)
        popupMenu.onItemClickListener = object : PopupMenuItem.OnClickListener {
            override fun onPopupMenuItemClicked(popupMenuItem: PopupMenuItem) {
                l.invoke(popupMenuItem)
            }
        }
        popupMenu.show()
    }
}