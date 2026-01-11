package cn.a10miaomiao.bbmiao.page.setting

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope
import androidx.recyclerview.widget.LinearLayoutManager
import cn.a10miaomiao.bbmiao.comm.connectUi
import cn.a10miaomiao.bbmiao.comm.lazyUiDi
import cn.a10miaomiao.bbmiao.comm.miaoBindingUi
import cn.a10miaomiao.bbmiao.comm.preferences.toSharedPreferences
import cn.a10miaomiao.bbmiao.comm.recycler._miaoLayoutManage
import cn.a10miaomiao.bbmiao.comm.views
import cn.a10miaomiao.miao.binding.miaoEffect
import com.a10miaomiao.bilimiao.comm.datastore.SettingPreferences
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerDelegate
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import com.a10miaomiao.bilimiao.store.WindowStore
import de.Maxr1998.modernpreferences.PreferenceScreen
import de.Maxr1998.modernpreferences.PreferencesAdapter
import de.Maxr1998.modernpreferences.helpers.categoryHeader
import de.Maxr1998.modernpreferences.helpers.screen
import de.Maxr1998.modernpreferences.helpers.seekBar
import de.Maxr1998.modernpreferences.helpers.singleChoice
import de.Maxr1998.modernpreferences.helpers.switch
import de.Maxr1998.modernpreferences.preferences.SwitchPreference
import de.Maxr1998.modernpreferences.preferences.choice.SelectionItem
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.views.dsl.core.frameLayout
import splitties.views.dsl.core.lParams
import splitties.views.dsl.core.matchParent
import splitties.views.dsl.recyclerview.recyclerView

class DanmakuSettingDetailFragment : Fragment(), DIAware, MyPage {

    companion object {
        fun newInstance(mode: Int): DanmakuSettingDetailFragment {
            val fragment = DanmakuSettingDetailFragment()
            val bundle = Bundle()
            bundle.putInt("mode", mode)
            fragment.arguments = bundle
            return fragment
        }
    }


    override val pageConfig = myPageConfig {
        title = "弹幕设置"
    }

    override val di: DI by lazyUiDi(ui = { ui })

    private val windowStore by instance<WindowStore>()

    private val basePlayerDelegate by instance<BasePlayerDelegate>()

    private val mode by lazy { requireArguments().getInt("mode") }

    private var mPreferencesAdapter: PreferencesAdapter? = null

    private val mBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            update(context)
        }
    }

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
    }



    private fun maxLineFormatter(lines: Int): String {
        return if (lines > 0) {
            "${lines}行"
        } else {
            "无限制"
        }
    }

    private fun fontsizeFormatter(size: Int): String {
        return (size / 100f).toString()
    }

    private fun speedFormatter(speed: Int): String {
        return (speed / 100f).toString()
    }

    private fun generateKey(key: String): String {
        return DanmakuSettingFragment.Companion.generateKey(key, mode)
    }

    fun update(context: Context) {
        mPreferencesAdapter?.currentScreen?.let { screen ->
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val danmakuShowSP = screen["danmaku_show"] as SwitchPreference
            danmakuShowSP.checked = prefs.getBoolean("danmaku_show", true)
        }
    }

    val ui = miaoBindingUi {
        frameLayout {
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
                )
            )
        }
        return screen(ctx, prefs, block)
    }

    suspend fun createRootScreen() = dataStoreScreen {
        collapseIcon = true

        categoryHeader("display") {
            title = "显示"
        }

        switch(
            generateKey(
                DanmakuSettingFragment.Companion.KEY_DANMAKU_SHOW
            )
        ) {
            title = "弹幕显示"
            defaultValue = true
        }

        switch(
            generateKey(
                DanmakuSettingFragment.Companion.KEY_DANMAKU_R2L_SHOW
            )
        ) {
            title = "滚动弹幕显示"
            defaultValue = true
        }

        switch(
            generateKey(
                DanmakuSettingFragment.Companion.KEY_DANMAKU_FT_SHOW
            )
        ) {
            title = "顶部弹幕显示"
            defaultValue = true
        }

        switch(
            generateKey(
                DanmakuSettingFragment.Companion.KEY_DANMAKU_FB_SHOW
            )
        ) {
            title = "底部弹幕显示"
            defaultValue = true
        }

        switch(
            generateKey(
                DanmakuSettingFragment.Companion.KEY_DANMAKU_SPECIAL_SHOW
            )
        ) {
            title = "高级弹幕显示"
            defaultValue = true
        }

        seekBar(
            generateKey(
                DanmakuSettingFragment.Companion.KEY_DANMAKU_R2L_MAX_LINE
            )
        ) {
            title = "滚动弹幕最大行数"
            default = 0
            max = 20
            min = 0
            formatter = ::maxLineFormatter
        }

        seekBar(
            generateKey(
                DanmakuSettingFragment.Companion.KEY_DANMAKU_FT_MAX_LINE
            )
        ) {
            title = "顶部弹幕最大行数"
            default = 0
            max = 20
            min = 0
            formatter = ::maxLineFormatter
        }

        seekBar(
            generateKey(
                DanmakuSettingFragment.Companion.KEY_DANMAKU_FB_MAX_LINE
            )
        ) {
            title = "底部弹幕最大行数"
            default = 0
            max = 20
            min = 0
            formatter = ::maxLineFormatter
        }

        categoryHeader("font") {
            title = "字体"
        }

        val fontsizeSelection = listOf(
            SelectionItem(key = "0.5", title = "比小更小"),
            SelectionItem(key = "0.75", title = "小"),
            SelectionItem(key = "1", title = "正常"),
            SelectionItem(key = "1.5", title = "大"),
            SelectionItem(key = "2", title = "比大更大")
        )
        singleChoice(
            generateKey(
                DanmakuSettingFragment.Companion.KEY_DANMAKU_FONTSIZE
            ), fontsizeSelection
        ) {
            title = "选择你的尺寸"
            summary = "想要多大的"
            initialSelection = "1"
        }
//        seekBar(
//            generateKey(
//                DanmakuSettingFragment.KEY_DANMAKU_FONTSIZE
//            )
//        ) {
//            title = "字体大小"
//            default = 100
//            max = 200
//            min = 50
//            showTickMarks = true
//            formatter = ::maxLineFormatter
//        }

        seekBar(
            generateKey(
                DanmakuSettingFragment.Companion.KEY_DANMAKU_TRANSPARENT
            )
        ) {
            title = "字体透明度"
            default = 100
            max = 100
            min = 0
            formatter = { "$it%" }
        }

        categoryHeader("speed") {
            title = "速度"
        }

        val speedSelection = listOf(
            SelectionItem(key = "2", title = "比慢更慢"),
            SelectionItem(key = "1.5", title = "慢"),
            SelectionItem(key = "1", title = "正常"),
            SelectionItem(key = "0.75", title = "快"),
            SelectionItem(key = "0.5", title = "比快更快")
        )
        singleChoice(
            generateKey(
                DanmakuSettingFragment.Companion.KEY_DANMAKU_SPEED
            ), speedSelection
        ) {
            title = "选择你的车速"
            summary = "想要更快吗"
            initialSelection = "1"
        }
    }

}