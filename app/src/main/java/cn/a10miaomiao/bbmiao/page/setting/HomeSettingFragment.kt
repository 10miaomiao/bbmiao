package cn.a10miaomiao.bbmiao.page.setting

import android.content.*
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.FragmentNavigatorDestinationBuilder
import androidx.recyclerview.widget.LinearLayoutManager
import cn.a10miaomiao.bbmiao.comm.connectUi
import cn.a10miaomiao.bbmiao.comm.lazyUiDi
import cn.a10miaomiao.bbmiao.comm.miaoBindingUi
import cn.a10miaomiao.bbmiao.comm.navigation.FragmentNavigatorBuilder
import cn.a10miaomiao.bbmiao.comm.preferences.toSharedPreferences
import cn.a10miaomiao.bbmiao.comm.recycler._miaoLayoutManage
import cn.a10miaomiao.bbmiao.comm.views
import cn.a10miaomiao.miao.binding.android.view._bottomPadding
import cn.a10miaomiao.miao.binding.android.view._leftPadding
import cn.a10miaomiao.miao.binding.android.view._rightPadding
import cn.a10miaomiao.miao.binding.android.view._topPadding
import cn.a10miaomiao.miao.binding.miaoEffect
import com.a10miaomiao.bilimiao.comm.datastore.SettingPreferences
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerDelegate
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import cn.a10miaomiao.bbmiao.store.WindowStore
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.Maxr1998.modernpreferences.PreferenceScreen
import de.Maxr1998.modernpreferences.PreferencesAdapter
import de.Maxr1998.modernpreferences.helpers.categoryHeader
import de.Maxr1998.modernpreferences.helpers.screen
import de.Maxr1998.modernpreferences.helpers.singleChoice
import de.Maxr1998.modernpreferences.helpers.switch
import de.Maxr1998.modernpreferences.preferences.choice.SelectionItem
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.views.dsl.core.frameLayout
import splitties.views.dsl.core.lParams
import splitties.views.dsl.core.matchParent
import splitties.views.dsl.recyclerview.recyclerView

class HomeSettingFragment : Fragment(), DIAware, MyPage
    , SharedPreferences.OnSharedPreferenceChangeListener {

    companion object : FragmentNavigatorBuilder() {
        var homeSettingVersion = 0
        override val name = "setting.home"
        override fun FragmentNavigatorDestinationBuilder.init() {
            deepLink("bilimiao://setting/home")
        }
    }

    override val pageConfig = myPageConfig {
        title = "首页设置"
    }

    override val di: DI by lazyUiDi(ui = { ui })

    private val windowStore by instance<WindowStore>()

    private val basePlayerDelegate by instance<BasePlayerDelegate>()

    private var mPreferencesAdapter: PreferencesAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ui.parentView = container
        return ui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycle.coroutineScope.launch {
            windowStore.connectUi(ui)
        }
        lifecycle.coroutineScope.launch {
            val screen = createRootScreen()
            val adapter = PreferencesAdapter(screen)
            ui.setState {
                mPreferencesAdapter = adapter
            }
        }
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key?.startsWith("home") == true) {
            homeSettingVersion++
        }
        if (key == "home_recommend_list_style") {
            showRebootAppDialog()
        }
    }

    private fun showRebootAppDialog() {
        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle("提示")
            setMessage("修改推荐列表样式，需重新打开APP后生效")
            setNeutralButton("立即重新打开") { _, _ ->
                val ctx = requireContext()
                val packageManager = ctx.packageManager
                val intent = packageManager.getLaunchIntentForPackage(ctx.packageName)!!
                val componentName = intent.component
                val mainIntent = Intent.makeRestartActivityTask(componentName)
                ctx.startActivity(mainIntent)
                Runtime.getRuntime().exit(0)
            }
            setNegativeButton("稍后手动", null)
        }.show()
    }

    val ui = miaoBindingUi {
        val insets = windowStore.getContentInsets(parentView)
        frameLayout {
            _leftPadding = insets.left
            _topPadding = insets.top
            _rightPadding = insets.right
            _bottomPadding = insets.bottom
            views {
                +recyclerView {
                    _miaoLayoutManage(LinearLayoutManager(requireContext()))
                    miaoEffect(mPreferencesAdapter) {
                        adapter = mPreferencesAdapter
                    }
                }..lParams(matchParent, matchParent)
            }
        }
    }

    suspend fun dataStoreScreen(
        block: PreferenceScreen.Builder.() -> Unit
    ): PreferenceScreen {
        val ctx = requireContext()
        val prefs = SettingPreferences.run {
            ctx.dataStore.toSharedPreferences(
                scope = lifecycle.coroutineScope,
                keysMap = mapOf(
                    "home_recommend_show" to HomeRecommendShow,
                    "home_popular_show" to HomePopularShow,
                    "home_popular_carry_token" to HomePopularCarryToken,
                    "home_recommend_list_style" to HomeRecommendListStyle,
                )
            )
        }
        return screen(ctx, prefs, block)
    }

    suspend fun createRootScreen() = dataStoreScreen {
        collapseIcon = true

        categoryHeader("top_nav") {
            title = "首页顶部设置"
        }

        switch("home_recommend_show") {
            title = "显示推荐"
            defaultValue = true
        }

        switch("home_popular_show") {
            title = "显示热门"
            defaultValue = true
        }

        categoryHeader("popular") {
            title = "热门设置"
        }

        switch("home_popular_carry_token") {
            title = "个性化热门列表"
            summary = "修改后需手动刷新列表"
            defaultValue = true
        }

        categoryHeader("recommend") {
            title = "推荐设置"
        }

        val recommendListStyleSelection = listOf(
            SelectionItem(key = 0, title = "详情为主"),
            SelectionItem(key = 1, title = "封面为主"),
        )
        singleChoice("home_recommend_list_style", recommendListStyleSelection) {
            title = "列表样式"
            summary = "修改后需重启APP"
            initialSelection = 0
        }

    }

}