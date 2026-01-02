package cn.a10miaomiao.bbmiao.page.search.result

import android.view.View
import com.a10miaomiao.bilimiao.comm.mypage.MenuItemPropInfo
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import cn.a10miaomiao.bbmiao.comm.recycler.RecyclerViewFragment

abstract class BaseResultFragment : RecyclerViewFragment() {
    abstract val title: String
    abstract val menus: List<MenuItemPropInfo>
    abstract fun onMenuItemClick(view: View, menuItem: MenuItemPropInfo)

    fun notifyConfigChanged() {
        val pFragment = parentFragment
        if (pFragment is MyPage) {
            pFragment.pageConfig.notifyConfigChanged()
        }
    }
}