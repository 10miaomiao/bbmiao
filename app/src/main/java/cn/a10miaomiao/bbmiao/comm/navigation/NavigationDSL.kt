package cn.a10miaomiao.bbmiao.comm.navigation

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.navigation.NavController
import cn.a10miaomiao.bbmiao.MainActivity
import cn.a10miaomiao.bbmiao.activity.SearchActivity

fun NavController.tryPopBackStack(): Boolean {
    return try {
        popBackStack()
        true
    } catch (e: Exception) {
        false
    }
}

fun Activity.openSearch(view: View) {
    val searchConfig = (this as? MainActivity)?.pageConfig?.search
    if (searchConfig != null) {
        SearchActivity.launch(
            this,
            searchConfig.keyword,
            1,
            searchConfig.name,
            view,
        )
    } else {
        SearchActivity.launch(
            this,
            "",
            0,
            null,
            view,
        )
    }
}

//fun ScaffoldView.openSearchDrawer() {
//    openDrawer()
//    (drawerFragment as? StartFragment)?.openSearchView()
//    val intent = Intent(context, SearchActivity::class.java)
//    context.startActivity(intent)
//}
//
//fun ScaffoldView.closeSearchDrawer() {
//    closeDrawer()
//}