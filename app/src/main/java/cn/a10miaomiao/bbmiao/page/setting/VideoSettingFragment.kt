package cn.a10miaomiao.bbmiao.page.setting

import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorDestinationBuilder
import androidx.recyclerview.widget.LinearLayoutManager
import cn.a10miaomiao.bbmiao.comm.connectUi
import cn.a10miaomiao.bbmiao.comm.lazyUiDi
import cn.a10miaomiao.bbmiao.comm.miaoBindingUi
import cn.a10miaomiao.bbmiao.comm.navigation.FragmentNavigatorBuilder
import cn.a10miaomiao.bbmiao.comm.preferences.toSharedPreferences
import cn.a10miaomiao.bbmiao.comm.recycler._miaoLayoutManage
import cn.a10miaomiao.bbmiao.comm.views
import cn.a10miaomiao.bbmiao.widget.scaffold.getScaffoldView
import cn.a10miaomiao.miao.binding.android.view._bottomPadding
import cn.a10miaomiao.miao.binding.android.view._leftPadding
import cn.a10miaomiao.miao.binding.android.view._rightPadding
import cn.a10miaomiao.miao.binding.android.view._topPadding
import cn.a10miaomiao.miao.binding.miaoEffect
import com.a10miaomiao.bilimiao.comm.datastore.SettingConstants
import com.a10miaomiao.bilimiao.comm.datastore.SettingPreferences
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerDelegate
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import cn.a10miaomiao.bbmiao.store.WindowStore
import de.Maxr1998.modernpreferences.PreferenceScreen
import de.Maxr1998.modernpreferences.PreferencesAdapter
import de.Maxr1998.modernpreferences.helpers.categoryHeader
import de.Maxr1998.modernpreferences.helpers.multiIntChoice
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

class VideoSettingFragment : Fragment(), DIAware, MyPage
    , SharedPreferences.OnSharedPreferenceChangeListener {

    companion object : FragmentNavigatorBuilder() {
        override val name = "setting.video"
        override fun FragmentNavigatorDestinationBuilder.init() {
            deepLink("bilimiao://setting/video")
        }

        const val PLAYER_DECODER = "player_decoder"
        const val PLAYER_FNVAL = "player_fnval"
        const val PLAYER_BACKGROUND = "player_background"
        const val PLAYER_PROXY = "player_proxy"
        const val PLAYER_PLAYING_NOTIFICATION = "player_playing_notification"
        const val PLAYER_OPEN_MODE = "player_open_mode"
        const val PLAYER_FULL_MODE = "player_full_mode"
        const val PLAYER_ORDER = "player_order"
        const val PLAYER_ORDER_RANDOM = "player_order_random"
        const val PLAYER_SCREEN_TYPE = "player_screen_type"
        const val PLAYER_AUDIO_FOCUS = "player_audio_focus"
        const val PLAYER_BOTTOM_PROGRESS_BAR_SHOW = "player_bottom_progress_bar_show"

        const val PLAYER_AUTO_STOP_DURATION = "player_auto_stop_duration"

        const val PLAYER_SUBTITLE_SHOW = "player_subtitle_show"
        const val PLAYER_AI_SUBTITLE_SHOW = "player_ai_subtitle_show"

        const val DECODER_DEFAULT = "default"
        const val DECODER_AV1 = "AV1"

        const val FNVAL_FLV = "2"
        const val FNVAL_MP4 = "2"
        const val FNVAL_DASH = "4048"

        const val PLAYER_SMALL_SHOW_AREA = "player_small_show_area"
        const val PLAYER_HOLD_SHOW_AREA = "player_hold_show_area"
        const val PLAYER_SMALL_SCREEN_DRAGGABLE = "player_small_screen_draggable"
    }

    override val pageConfig = myPageConfig {
        title = "视频设置"
    }

    override val di: DI by lazyUiDi(ui = { ui })

    private val windowStore by instance<WindowStore>()

    private val basePlayerDelegate by instance<BasePlayerDelegate>()

    private var mAdapter: PreferencesAdapter? = null

    private val scaffoldView by lazy { requireActivity().getScaffoldView() }

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
                mAdapter = adapter
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        mAdapter?.run {
            val aiSubtitlePreference = currentScreen[PLAYER_AI_SUBTITLE_SHOW] as SwitchPreference
            if (key == PLAYER_SUBTITLE_SHOW) {
                val subtitlePreference = currentScreen[PLAYER_SUBTITLE_SHOW] as SwitchPreference
                if (sharedPreferences.getBoolean(PLAYER_SUBTITLE_SHOW, true)) {
                    subtitlePreference.summary = "字幕功能已启用"
                    aiSubtitlePreference.enabled = true
                } else {
                    subtitlePreference.summary = "字幕功能已关闭"
                    aiSubtitlePreference.enabled = false
                }
                notifyDataSetChanged()
            } else if (key == PLAYER_AI_SUBTITLE_SHOW) {
                if (sharedPreferences.getBoolean(PLAYER_AI_SUBTITLE_SHOW, false)) {
                    aiSubtitlePreference.summary = "AI字幕功能已启用"
                } else {
                    aiSubtitlePreference.summary = "AI字幕功能已关闭"
                }
                notifyDataSetChanged()
            }
        }
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
                    miaoEffect(mAdapter) {
                        adapter = mAdapter
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
                scope = lifecycleScope,
                keysMap = mapOf(
                    // 播放器设置
                    PLAYER_BACKGROUND to PlayerBackground,
                    PLAYER_AUDIO_FOCUS to PlayerAudioFocus,
                    // 视频源设置
                    PLAYER_FNVAL to PlayerFnval,
                    // 播放控制设置
                    PLAYER_PLAYING_NOTIFICATION to PlayerNotification,
                    PLAYER_OPEN_MODE to PlayerOpenMode,
                    PLAYER_ORDER to PlayerOrder,
                    PLAYER_ORDER_RANDOM to PlayerOrderRandom,
                    PLAYER_FULL_MODE to PlayerFullMode,
                    PLAYER_BOTTOM_PROGRESS_BAR_SHOW to PlayerBottomProgressBarShow,
//                    PLAYER_AUTO_STOP_DURATION to PlayerA,
                    // 横屏状态小屏设置
                    PLAYER_SMALL_SHOW_AREA to PlayerSmallShowArea,
                    PLAYER_HOLD_SHOW_AREA to PlayerHoldShowArea,
                    PLAYER_SMALL_SCREEN_DRAGGABLE to PlayerSmallDraggable,
                    // 字幕显示设置
                    PLAYER_SUBTITLE_SHOW to PlayerSubtitleShow,
                    PLAYER_AI_SUBTITLE_SHOW to PlayerAiSubtitleShow,
                    // 其他
                    DanmakuSettingFragment.KEY_DANMAKU_SYS_FONT to DanmakuSysFont,
                )
            )
        }
        return screen(ctx, prefs, block)
    }

    suspend fun createRootScreen() = dataStoreScreen {
        collapseIcon = true

        categoryHeader("player") {
            title = "播放器设置"
        }

        switch(PLAYER_BACKGROUND) {
            title = "后台播放"
            summary = "遇到困难时，不要停下来."
            defaultValue = true
        }

        switch(PLAYER_AUDIO_FOCUS) {
            title = "占用音频焦点"
            summary = "关闭后可以与其它APP同时播放"
            defaultValue = true
        }

        categoryHeader("source") {
            title = "视频源设置"
        }

        val fnvalSelection = listOf(
            SelectionItem(key = FNVAL_DASH, title = "dash(支持4K)"),
            SelectionItem(key = FNVAL_MP4, title = "mp4(不支持2K及以上)"),
//            SelectionItem(key = FNVAL_FLV, title = "flv(不支持2K及以上)"),
        )
        singleChoice(PLAYER_FNVAL, fnvalSelection) {
            title = "视频获取方式选择(视频格式)"
            summary = "不能播放时，换个格式试试吧"
            initialSelection = FNVAL_DASH
        }

//        pref(PLAYER_PROXY) {
//            title = "区域限制设置"
//            summary = "滴，出差卡"
//
//            onClick {
////                val nav = findNavController()
////                nav.navigateToCompose(ProxySettingPage())
//                true
//            }
//        }

        categoryHeader("control") {
            title = "播放控制设置"
        }

        switch(PLAYER_PLAYING_NOTIFICATION) {
            title = "显示通知栏播放器控制器"
            summary = "这个家里已经没有你的位置啦！"
            defaultValue = true
        }

        val openModeSelection = listOf(
            SelectionItem(
                key = SettingConstants.PLAYER_OPEN_MODE_AUTO_PLAY,
                title = "无视频播放时，自动播放"
            ),
            SelectionItem(
                key = SettingConstants.PLAYER_OPEN_MODE_AUTO_REPLACE,
                title = "正在播放时，自动替换播放"
            ),
            SelectionItem(
                key = SettingConstants.PLAYER_OPEN_MODE_AUTO_REPLACE_PAUSE,
                title = "暂停播放时，自动替换播放"
            ),
            SelectionItem(
                key = SettingConstants.PLAYER_OPEN_MODE_AUTO_REPLACE_COMPLETE,
                title = "完成播放时，自动替换播放"
            ),
            SelectionItem(
                key = SettingConstants.PLAYER_OPEN_MODE_AUTO_CLOSE,
                title = "退出详情页时，自动关闭"
            ),
            SelectionItem(
                key = SettingConstants.PLAYER_OPEN_MODE_AUTO_FULL_SCREEN,
                title = "设备竖屏状态时，自动全屏播放"
            ),
            SelectionItem(
                key = SettingConstants.PLAYER_OPEN_MODE_AUTO_FULL_SCREEN_LANDSCAPE,
                title = "设备横屏状态时，自动全屏播放"
            ),
        )
        multiIntChoice(PLAYER_OPEN_MODE, openModeSelection) {
            title = "播放器自动控制"
            summary = "打开或关闭视频详情时自动进行的操作"
        }

        val orderSelection =  listOf(
            SelectionItem(
                key = SettingConstants.PLAYER_ORDER_LOOP,
                title = "循环播放（有勾选下列选项时为列表循环，无勾选时为单个循环）"
            ),
            SelectionItem(
                key = SettingConstants.PLAYER_ORDER_NEXT_P,
                title = "自动下一P"
            ),
            SelectionItem(
                key = SettingConstants.PLAYER_ORDER_NEXT_VIDEO,
                title = "自动下一个视频"
            ),
            SelectionItem(
                key = SettingConstants.PLAYER_ORDER_NEXT_EPISODE,
                title = "自动下一集（番剧）"
            ),
        )
        multiIntChoice(PLAYER_ORDER, orderSelection) {
            title = "播放器播放顺序"
            summary = "可以多个选项组合选择"
        }
        switch(PLAYER_ORDER_RANDOM) {
            title = "随机播放"
            summary = "播放完一个视频后，随机播放下一个视频，单个视频循环时无效"
            defaultValue = false
        }

        val fullModeSelection = listOf(
            SelectionItem(
                key = SettingConstants.PLAYER_FULL_MODE_AUTO,
                title = "跟随视频"
            ),
            SelectionItem(
                key = SettingConstants.PLAYER_FULL_MODE_UNSPECIFIED,
                title = "跟随系统"
            ),
            SelectionItem(
                key = SettingConstants.PLAYER_FULL_MODE_SENSOR_LANDSCAPE,
                title = "横向全屏(自动旋转)"
            ),
            SelectionItem(
                key = SettingConstants.PLAYER_FULL_MODE_LANDSCAPE,
                title = "横向全屏(固定方向1)"
            ),
            SelectionItem(
                key = SettingConstants.PLAYER_FULL_MODE_REVERSE_LANDSCAPE,
                title = "横向全屏(固定方向2)"
            ),
        )
        singleChoice(PLAYER_FULL_MODE, fullModeSelection) {
            title = "全屏播放设置"
            summary = "可以在播放器长按全屏按钮召唤此选项"
            initialSelection = SettingConstants.PLAYER_FULL_MODE_AUTO
        }

         val bottomProgressBarShowSelection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
             listOf(
                 SelectionItem(
                     key = SettingConstants.PLAYER_BOTTOM_PROGRESS_BAR_SHOW_IN_SMALL,
                     title = "小屏播放时，显示底部进度条"
                 ),
                 SelectionItem(
                     key = SettingConstants.PLAYER_BOTTOM_PROGRESS_BAR_SHOW_IN_FULL,
                     title = "全屏播放时，显示底部进度条"
                 ),
                 SelectionItem(
                     key = SettingConstants.PLAYER_BOTTOM_PROGRESS_BAR_SHOW_IN_PIP,
                     title = "画中画(应用外小窗)模式，显示底部进度条"
                 ),
             )
        } else {
             listOf(
                SelectionItem(
                    key = SettingConstants.PLAYER_BOTTOM_PROGRESS_BAR_SHOW_IN_SMALL,
                    title = "小屏播放时，显示底部进度条"
                ),
                SelectionItem(
                    key = SettingConstants.PLAYER_BOTTOM_PROGRESS_BAR_SHOW_IN_FULL,
                    title = "全屏播放时，显示底部进度条"
                ),
            )
        }
        multiIntChoice(PLAYER_BOTTOM_PROGRESS_BAR_SHOW, bottomProgressBarShowSelection) {
            title = "底部进度条显示控制"
            summary = ""
        }
        // TODO: 自定义倍速菜单

        seekBar(PLAYER_AUTO_STOP_DURATION) {
            title = "播放器定时关闭"
            summary = "视频播放的时长，而不是实际经过的时间"
            max = 3600000
            min = 0
            default = 0
            formatter = {
                val second = value/1000
                val minute = second/60
                if(second == 0){
                    "${value}ms"
                } else if(minute == 0){
                    "${second}s"
                } else {
                    "${minute}min${second-minute*60}s"
                }
            }
        }

        categoryHeader("small") {
            title = "横屏状态小屏设置"
        }

        switch(PLAYER_SMALL_SCREEN_DRAGGABLE) {
            title = "小屏时整个屏幕可拖拽"
            summary = "启用后，小屏状态时播放器手势无效"
            defaultValue = false
        }
        seekBar(PLAYER_SMALL_SHOW_AREA) {
            title = "横屏时小屏播放面积"
            default = 480
            max = 600
            min = 150
            formatter = { it.toString() }
        }
        seekBar(PLAYER_HOLD_SHOW_AREA) {
            title = "小屏挂起后播放面积"
            default = 130
            max = 300
            min = 100
            formatter = { it.toString() }
        }

        categoryHeader("subtitle") {
            title = "字幕显示设置"
        }

        switch(PLAYER_SUBTITLE_SHOW) {
            title = "字幕显示"
            summary = "字幕功能已打开"
            summaryDisabled = "字幕功能已关闭"
            defaultValue = true
        }

        switch(PLAYER_AI_SUBTITLE_SHOW) {
            title = "AI字幕显示"
            summary = "此AI字幕是指UP主手动生成的AI字幕，并非每个视频都有"
            summaryDisabled = "字幕功能已关闭"
            defaultValue = false
        }

        categoryHeader("other") {
            title = "其它"
        }

        switch(DanmakuSettingFragment.KEY_DANMAKU_SYS_FONT) {
            title = "弹幕使用系统字体"
            summary = "修改后需重启APP生效"
            defaultValue = false
        }



    }

}